package org.example.lazarusplugin

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.waitForSmartMode
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent
import com.intellij.platform.backend.observation.Observation
import kotlinx.coroutines.runBlocking
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.psi.PsiManager
import org.example.lazarusplugin.services.GraphTrackingService
import org.slf4j.LoggerFactory

class Main : ProjectActivity {

    companion object {
        private val logger = LoggerFactory.getLogger(Main::class.java)
    }

    override suspend fun execute(project: Project) {

        NotificationGroupManager.getInstance()
            .getNotificationGroup("Graph Tracker")
            .createNotification("Initializing Graph Tracker", "Starting the Graph Building Tool...", NotificationType.INFORMATION)
            .notify(project)

        // Wait for project to be ready
        runBlocking {
            Observation.awaitConfiguration(project)
            project.waitForSmartMode()
        }

        NotificationGroupManager.getInstance()
            .getNotificationGroup("Graph Tracker")
            .createNotification("Graph Tracker Ready", "Tool is mounted and ready.", NotificationType.INFORMATION)
            .notify(project)

        // Get the graph tracking service
        val graphService = project.getService(GraphTrackingService::class.java)

        // Build initial graph
        ApplicationManager.getApplication().executeOnPooledThread {
            ApplicationManager.getApplication().runReadAction {
                try {
                    logger.info("=== Starting Initial Graph Build ===")
                    graphService.buildInitialGraph()
                    logger.info("=== Initial Graph Build Complete ===")

                    // Print the graph structure
                    graphService.printGraphStructure()

                    NotificationGroupManager.getInstance()
                        .getNotificationGroup("Graph Tracker")
                        .createNotification(
                            "Graph Built Successfully",
                            "Graph contains ${graphService.getNodeCount()} nodes and ${graphService.getEdgeCount()} edges",
                            NotificationType.INFORMATION
                        )
                        .notify(project)

                } catch (e: Exception) {
                    logger.error("Error building initial graph", e)
                    NotificationGroupManager.getInstance()
                        .getNotificationGroup("Graph Tracker")
                        .createNotification(
                            "Graph Building Failed",
                            "Error: ${e.message}",
                            NotificationType.ERROR
                        )
                        .notify(project)
                }
            }
        }

        // Subscribe to file changes for incremental updates
        val connection = project.messageBus.connect()
        connection.subscribe(VirtualFileManager.VFS_CHANGES, object : BulkFileListener {
            override fun after(events: MutableList<out VFileEvent>) {
                val changedFiles = events.filterIsInstance<VFileContentChangeEvent>()
                    .map { it.file }
                    .filter { it.name.endsWith(".java") || it.name.endsWith(".kt") }

                if (changedFiles.isNotEmpty()) {
                    ApplicationManager.getApplication().executeOnPooledThread {
                        ApplicationManager.getApplication().runReadAction {
                            try {
                                logger.info("=== File Changes Detected: ${changedFiles.size} files ===")
                                changedFiles.forEach { file ->
                                    logger.info("Changed file: ${file.path}")
                                }

                                // Update graph for changed files
                                val psiFiles = changedFiles.mapNotNull { vFile ->
                                    PsiManager.getInstance(project).findFile(vFile)
                                }

                                graphService.updateGraphForFiles(psiFiles)

                                logger.info("=== Graph Update Complete ===")
                                graphService.printGraphStructure()

                            } catch (e: Exception) {
                                logger.error("Error updating graph for changed files", e)
                            }
                        }
                    }
                }
            }
        })
    }
}