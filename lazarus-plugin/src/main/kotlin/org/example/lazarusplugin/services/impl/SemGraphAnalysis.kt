package org.example.lazarusplugin.services.impl

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.example.lazarusplugin.services.api.IAgentService
import org.example.lazarusplugin.services.api.IGraphAnalysis
import org.example.lazarusplugin.services.api.IGraphReader

/**
 * Coordinator service that orchestrates graph reading and LLM analysis
 * This is the main entry point for using the system
 */
@Service(Service.Level.PROJECT)
class SemGraphAnalysis(
    private val project: Project,
    private val graphReader: IGraphReader,
    private val agentService: IAgentService
) : IGraphAnalysis {

    override suspend fun analyzeFile(filePath: String): String = withContext(Dispatchers.IO) {
        // Dummy implementation
        "File analysis response (dummy)"
    }

    override suspend fun analyzeCluster(filePaths: List<String>): String = withContext(Dispatchers.IO) {
        // Dummy implementation
        "Cluster analysis response (dummy)"
    }
}
