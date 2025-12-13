package org.example.lazarusplugin.services

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import org.example.lazarusplugin.models.GraphBuilder
import org.example.lazarusplugin.models.IndexedGraph
import org.example.lazarusplugin.models.NodeType
import org.example.lazarusplugin.models.EdgeType
import org.slf4j.LoggerFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

@Service(Service.Level.PROJECT)
class GraphTrackingService(private val project: Project) : Disposable {

    private val graphBuilder = GraphBuilder(project)
    private var graph: IndexedGraph? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    companion object {
        private val logger = LoggerFactory.getLogger(GraphTrackingService::class.java)
    }

    /**
     * Build the initial graph from all source files
     */
    fun buildInitialGraph() {
        logger.info("Building initial graph from all source files...")
        graph = graphBuilder.buildInitialGraph()
        logger.info("Graph built successfully!")
    }

    /**
     * Update graph for changed files
     */
    fun updateGraphForFiles(changedFiles: List<PsiFile>) {
        if (graph == null) {
            logger.warn("Graph not initialized. Building from scratch...")
            buildInitialGraph()
            return
        }

        logger.info("Updating graph for ${changedFiles.size} changed files...")
        graphBuilder.updateGraphForFiles(changedFiles)
        logger.info("Graph updated successfully!")
    }

    /**
     * Get the current graph
     */
    fun getGraph(): IndexedGraph? = graph

    /**
     * Get number of nodes in the graph
     */
    fun getNodeCount(): Int = graph?.registry?.size ?: 0

    /**
     * Get number of edges in the graph
     */
    fun getEdgeCount(): Int = graph?.edges?.size ?: 0

    /**
     * Print detailed graph structure to console
     */
    fun printGraphStructure() {
        val currentGraph = graph ?: run {
            logger.warn("Graph is not initialized yet")
            return
        }

        logger.info("\n" + "=".repeat(80))
        logger.info("GRAPH STRUCTURE OVERVIEW")
        logger.info("=".repeat(80))
        logger.info("Total Nodes: ${currentGraph.registry.size}")
        logger.info("Total Edges: ${currentGraph.edges.size}")
        logger.info("")

        // Count nodes by type
        val nodesByType = currentGraph.registry.values.groupBy { it.type }
        logger.info("Nodes by Type:")
        NodeType.values().forEach { type ->
            val count = nodesByType[type]?.size ?: 0
            logger.info("  ${type.name}: $count")
        }
        logger.info("")

        // Count edges by type
        val edgesByType = currentGraph.edges.values.groupBy { it.type }
        logger.info("Edges by Type:")
        EdgeType.values().forEach { type ->
            val count = edgesByType[type]?.size ?: 0
            logger.info("  ${type.name}: $count")
        }
        logger.info("")

        // Print sample modules
        val modules = nodesByType[NodeType.MODULE] ?: emptyList()
        if (modules.isNotEmpty()) {
            logger.info("Modules (${modules.size}):")
            modules.take(5).forEach { module ->
                logger.info("  - ${module.name} (id: ${module.id})")
                logger.info("    Outgoing edges: ${module.outgoingEdges.size}")
                logger.info("    Incoming edges: ${module.incomingEdges.size}")
            }
            if (modules.size > 5) {
                logger.info("  ... and ${modules.size - 5} more")
            }
            logger.info("")
        }

        // Print sample files
        val files = nodesByType[NodeType.FILE] ?: emptyList()
        if (files.isNotEmpty()) {
            logger.info("Files (${files.size}):")
            files.take(5).forEach { file ->
                logger.info("  - ${file.name} (id: ${file.id})")
                logger.info("    Outgoing edges: ${file.outgoingEdges.size}")
                logger.info("    Incoming edges: ${file.incomingEdges.size}")
            }
            if (files.size > 5) {
                logger.info("  ... and ${files.size - 5} more")
            }
            logger.info("")
        }

        // Print sample classes
        val classes = nodesByType[NodeType.CLASS] ?: emptyList()
        if (classes.isNotEmpty()) {
            logger.info("Classes (${classes.size}):")
            classes.take(10).forEach { clazz ->
                logger.info("  - ${clazz.name} (id: ${clazz.id})")

                // Show what this class contains
                val containsEdges = clazz.outgoingEdges
                    .mapNotNull { currentGraph.edges[it] }
                    .filter { it.type == EdgeType.CONTAINS }
                logger.info("    Contains: ${containsEdges.size} elements")

                // Show inheritance
                val inheritsEdges = clazz.outgoingEdges
                    .mapNotNull { currentGraph.edges[it] }
                    .filter { it.type == EdgeType.INHERITS }
                if (inheritsEdges.isNotEmpty()) {
                    inheritsEdges.forEach { edge ->
                        val superClass = currentGraph.registry[edge.toNodeId]
                        logger.info("    Inherits from: ${superClass?.name}")
                    }
                }

                // Show implements
                val implementsEdges = clazz.outgoingEdges
                    .mapNotNull { currentGraph.edges[it] }
                    .filter { it.type == EdgeType.IMPLEMENTS }
                if (implementsEdges.isNotEmpty()) {
                    logger.info("    Implements: ${implementsEdges.size} interface(s)")
                }
            }
            if (classes.size > 10) {
                logger.info("  ... and ${classes.size - 10} more")
            }
            logger.info("")
        }

        // Print sample methods
        val methods = nodesByType[NodeType.METHOD] ?: emptyList()
        if (methods.isNotEmpty()) {
            logger.info("Methods (${methods.size}):")
            methods.take(10).forEach { method ->
                logger.info("  - ${method.name} (id: ${method.id})")

                // Show method calls
                val callEdges = method.outgoingEdges
                    .mapNotNull { currentGraph.edges[it] }
                    .filter { it.type == EdgeType.CALLS }
                if (callEdges.isNotEmpty()) {
                    logger.info("    Calls ${callEdges.size} method(s)")
                    callEdges.take(3).forEach { edge ->
                        val calledMethod = currentGraph.registry[edge.toNodeId]
                        logger.info("      -> ${calledMethod?.name}")
                    }
                }

                // Show field accesses
                val accessEdges = method.outgoingEdges
                    .mapNotNull { currentGraph.edges[it] }
                    .filter { it.type == EdgeType.ACCESSES }
                if (accessEdges.isNotEmpty()) {
                    logger.info("    Accesses ${accessEdges.size} field(s)")
                }
            }
            if (methods.size > 10) {
                logger.info("  ... and ${methods.size - 10} more")
            }
            logger.info("")
        }

        // Print sample attributes
        val attributes = nodesByType[NodeType.ATTRIBUTE] ?: emptyList()
        if (attributes.isNotEmpty()) {
            logger.info("Attributes (${attributes.size}):")
            attributes.take(10).forEach { attr ->
                logger.info("  - ${attr.name} (id: ${attr.id})")
            }
            if (attributes.size > 10) {
                logger.info("  ... and ${attributes.size - 10} more")
            }
            logger.info("")
        }

        logger.info("=".repeat(80))
        logger.info("")
    }

    /**
     * Print detailed information about a specific node
     */
    fun printNodeDetails(nodeId: String) {
        val currentGraph = graph ?: return
        val node = currentGraph.registry[nodeId] ?: run {
            logger.warn("Node not found: $nodeId")
            return
        }

        logger.info("\n" + "-".repeat(80))
        logger.info("NODE DETAILS: ${node.name}")
        logger.info("-".repeat(80))
        logger.info("ID: ${node.id}")
        logger.info("Type: ${node.type}")
        logger.info("")

        logger.info("Outgoing Edges (${node.outgoingEdges.size}):")
        node.outgoingEdges.forEach { edgeId ->
            val edge = currentGraph.edges[edgeId]
            if (edge != null) {
                val targetNode = currentGraph.registry[edge.toNodeId]
                logger.info("  ${edge.type}: ${node.name} -> ${targetNode?.name} (${targetNode?.type})")
            }
        }
        logger.info("")

        logger.info("Incoming Edges (${node.incomingEdges.size}):")
        node.incomingEdges.forEach { edgeId ->
            val edge = currentGraph.edges[edgeId]
            if (edge != null) {
                val sourceNode = currentGraph.registry[edge.fromNodeId]
                logger.info("  ${edge.type}: ${sourceNode?.name} (${sourceNode?.type}) -> ${node.name}")
            }
        }
        logger.info("-".repeat(80))
        logger.info("")
    }

    override fun dispose() {
        serviceScope.cancel()
        graph = null
    }
}