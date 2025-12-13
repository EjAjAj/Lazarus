package org.example.lazarusplugin

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.waitForSmartMode
import com.intellij.platform.backend.observation.Observation
import kotlinx.coroutines.runBlocking
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.components.service
import org.example.lazarusplugin.services.api.GraphStorage
import org.example.lazarusplugin.services.api.GraphBuilder

class Main : ProjectActivity {
    
    private val log = Logger.getInstance(Main::class.java)
    
    override suspend fun execute(project: Project) {
        // Wait for project to be fully initialized
        Observation.awaitConfiguration(project)
        project.waitForSmartMode()

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
    }
}