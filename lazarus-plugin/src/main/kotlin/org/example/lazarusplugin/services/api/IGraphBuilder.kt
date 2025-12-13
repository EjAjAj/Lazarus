package org.example.lazarusplugin.services.api

/**
 * Interface for building and updating the code graph
 */
interface IGraphBuilder {
    /**
     * Build a complete graph from project files
     */
    fun buildGraph()

    /**
     * Update graph when a file changes
     */
    fun updateGraphForFile(file: String)
}
