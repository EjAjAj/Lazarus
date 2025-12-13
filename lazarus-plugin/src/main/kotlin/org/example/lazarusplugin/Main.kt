package org.example.lazarusplugin

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.waitForSmartMode
import com.intellij.platform.backend.observation.Observation
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.startup.ProjectActivity
<<<<<<< HEAD
import com.intellij.psi.PsiManager
import org.example.lazarusplugin.services.GraphTrackingService
import org.slf4j.LoggerFactory
=======
import com.intellij.openapi.components.service
import org.example.lazarusplugin.services.api.GraphStorage
import org.example.lazarusplugin.services.api.GraphBuilder
>>>>>>> origin/develop

class Main : ProjectActivity {

    companion object {
        private val logger = LoggerFactory.getLogger(Main::class.java)
    }

    override suspend fun execute(project: Project) {
        // Wait for project to be fully initialized
        Observation.awaitConfiguration(project)
        project.waitForSmartMode()

<<<<<<< HEAD
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
=======
        // Initialize services
        val graphStorage = project.service<GraphStorage>()
        val graphBuilder = project.service<GraphBuilder>()

        // Try to load existing graph, if not found build it automatically
        if (!graphStorage.load()) {
            ApplicationManager.getApplication().executeOnPooledThread {
                println("Building semantic graph for project: ${project.name}")
                graphBuilder.buildGraph()
                println("Semantic graph build complete")
            }
        } else {
            println("Semantic graph loaded from disk")
        }
>>>>>>> origin/develop
    }
}