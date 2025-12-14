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

                // Get the built graph
                val graph = graphStorage.getGraph()

                // Print graph statistics
                println("=== Code Graph Built ===")
                println("Total Nodes: ${graph.size()}")
                println("Total Edges: ${graph.edges.size}")
                println()

                // Print first 20 edges
                println("=== Edges (up to 20) ===")
                graph.edges.values.take(20).forEachIndexed { index, edge ->
                    println("Edge ${index + 1}:")
                    println(edge.toString())
                    println()
                }

                // Print graph statistics by type
                println("=== Node Statistics ===")
                val nodesByType = graph.nodes.values.groupBy { it.type }
                nodesByType.forEach { (type, nodes) ->
                    println("$type: ${nodes.size} nodes")
                }
                println()

                // Print edge statistics by type
                println("=== Edge Statistics ===")
                val edgesByType = graph.edges.values.groupBy { it.type }
                edgesByType.forEach { (type, edges) ->
                    println("$type: ${edges.size} edges")
                }
                println()

                // Print top 10 nodes by degree
                println("=== Top 10 Nodes by Degree ===")
                graph.getTopNDegreeNodes(10).forEachIndexed { index, node ->
                    println("${index + 1}. ${node.name} (${node.type}) - Degree: ${node.getDegree()}")
                }
            }
        } else {
            println("Semantic graph loaded from disk")
        }
    }
}