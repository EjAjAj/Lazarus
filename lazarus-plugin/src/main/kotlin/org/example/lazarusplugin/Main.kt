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
import com.intellij.openapi.components.service
import com.intellij.openapi.ui.Messages
import org.example.lazarusplugin.services.api.GraphStorage
import org.example.lazarusplugin.services.api.GraphBuilder
import org.example.lazarusplugin.ui.GraphGenerationDialog

class Main : ProjectActivity {
    override suspend fun execute(project: Project) {
        runBlocking {
            Observation.awaitConfiguration(project)
            project.waitForSmartMode()
        }

        // Initialize services
        val graphStorage = project.service<GraphStorage>()
        val graphBuilder = project.service<GraphBuilder>()

        if (!graphStorage.load()) {
            ApplicationManager.getApplication().invokeLater {
                val dialog = GraphGenerationDialog(project)

                if (dialog.showAndGet()) {
                    // User clicked "Generate"
                    ApplicationManager.getApplication().executeOnPooledThread {
                        graphBuilder.buildGraph()

                        ApplicationManager.getApplication().invokeLater {
                            Messages.showInfoMessage(
                                project,
                                "Semantic graph has been generated successfully!",
                                "Graph Generation Complete"
                            )
                        }
                    }
                }
            }
        }
    }
}