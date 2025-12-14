package org.example.lazarusplugin.services.graph.api

/**
 * Coordinator service that orchestrates graph reading and agent analysis
 * This is the main entry point for analyzing code using the graph + LLM
 */
interface GraphAnalysis {
    /**
     * Analyze a cluster of files
     */
    suspend fun analyzeFile(filePath: String): String

    suspend fun  analyzeRemoteDiff(): String

    suspend fun makeInitProjectReport(): String
}
