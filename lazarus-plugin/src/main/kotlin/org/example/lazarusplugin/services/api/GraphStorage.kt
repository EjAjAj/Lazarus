package org.example.lazarusplugin.services.api

import org.example.lazarusplugin.models.graph.IndexedCodeGraph

/**
 * Interface for managing and persisting the indexed code graph
 */
interface GraphStorage {

    /**
     * Get the current graph instance
     */
    fun getGraph(): IndexedCodeGraph

    /**
     * Set a new graph instance (e.g., after building)
     */
    fun setGraph(graph: IndexedCodeGraph)

    /**
     * Save the current graph to disk
     * Returns a list of messages/logs about the save operation
     */
    fun saveToDisk()

    /**
     * Load graph from disk
     * Returns true if graph was loaded, false if no saved graph exists
     */
    fun load(): Boolean

    /**
     * Check if graph is ready to be used
     */
    fun isGraphReady(): Boolean
}
