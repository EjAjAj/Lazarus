package org.example.lazarusplugin.services.impl

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.example.lazarusplugin.git.domain.IDiffService
import org.example.lazarusplugin.services.api.AgentFileInput
import org.example.lazarusplugin.services.api.AgentService
import org.example.lazarusplugin.services.api.GraphAnalysis
import org.example.lazarusplugin.services.api.GraphReader
import org.example.lazarusplugin.utils.FileUtils

/**
 * Coordinator service that orchestrates graph reading and LLM analysis
 * This is the main entry point for using the system
 */
@Service(Service.Level.PROJECT)
class SemGraphAnalysis(
    private val project: Project,
    private val graphReader: GraphReader,
    private val agentService: AgentService
) : GraphAnalysis {
    private val gitDiffService: IDiffService = project.service()

    override suspend fun analyzeFile(filePath: String): String = withContext(Dispatchers.IO) {
        // Dummy implementation
        "File analysis response (dummy)"
    }

    override suspend fun analyzeRemoteDiff(): String = withContext(Dispatchers.IO) {
        // Get all files that changed with diffs
        val changedFilesWithDiffs = gitDiffService.getChangedFilesWithDiffs()

        if (changedFilesWithDiffs.isEmpty()) {
            return@withContext "No changes detected"
        }

        // Create AgentFileInput array
        val agentInputs = changedFilesWithDiffs.map { (filePath, diff) ->
            // Get file facts from GraphReader
            val fileReport = graphReader.getFileFacts(filePath, emptyArray())

            // Get file content
            val fileContent = FileUtils.getFileContent(project, filePath)

            AgentFileInput(
                fileContent = fileContent,
                fileDiff = diff,
                fileReport = fileReport
            )
        }

        // Send to agent service and return result
        agentService.generateReport(agentInputs)
    }
}
