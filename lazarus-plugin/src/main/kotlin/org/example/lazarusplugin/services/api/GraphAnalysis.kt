package org.example.lazarusplugin.services.api

/**
 * Coordinator service that orchestrates graph reading and agent analysis
 * This is the main entry point for analyzing code using the graph + LLM
 */
interface GraphAnalysis {
    /**
     * Analyze a cluster of files
     */
    suspend fun analyzeCluster(filePaths: List<String>): String

    suspend fun  analyzeRemoteDiff(): String
}
