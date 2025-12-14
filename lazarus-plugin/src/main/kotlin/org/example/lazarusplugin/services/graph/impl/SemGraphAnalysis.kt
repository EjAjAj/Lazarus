package org.example.lazarusplugin.services.graph.impl

import com.intellij.diff.contents.FileContent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.example.lazarusplugin.services.git.api.IDiffService
import org.example.lazarusplugin.services.graph.api.AgentFileDiffInput
import org.example.lazarusplugin.services.graph.api.AgentFileInput
import org.example.lazarusplugin.services.graph.api.AgentService
import org.example.lazarusplugin.services.graph.api.GraphAnalysis
import org.example.lazarusplugin.services.graph.api.GraphReader
import org.example.lazarusplugin.utils.FileUtils

/**
 * Coordinator service that orchestrates graph reading and LLM analysis
 * This is the main entry point for using the system
 */
@Service(Service.Level.PROJECT)
class SemGraphAnalysis(
    private val project: Project
) : GraphAnalysis {

    private val graphReader: GraphReader by lazy { project.service<GraphReader>() }
    private val agentService: AgentService by lazy { project.service<AgentService>() }
    private val gitDiffService: IDiffService by lazy { project.service<IDiffService>() }
    private var projectReport: String = ""

    override suspend fun analyzeFile(filePath: String): String = withContext(Dispatchers.IO) {
        if (projectReport.isEmpty()) {
            projectReport = makeInitProjectReport()
        }

        val fileReport = graphReader.getFileFacts(filePath, graphReader.getHotFiles())
        val fileContent = FileUtils.getFileContent(project, filePath)

        agentService.getFileSummary(
            AgentFileInput(
                fileContent = fileContent, fileReport = fileReport
            ), projectReport)
    }

    override suspend fun makeInitProjectReport(): String = withContext(Dispatchers.IO) {
        if (projectReport.isEmpty()) {
            val hotFiles = graphReader.getHotFiles().take(5)
            // Generate file reports for all hot files
            val agentFileInputs = hotFiles.map { filePath ->
                val fileReport = graphReader.getFileFacts(filePath, ArrayList(hotFiles))
                val fileContent = FileUtils.getFileContent(project, filePath)

                AgentFileInput(
                    fileContent = fileContent,
                    fileReport = fileReport
                )
            }

            projectReport = agentService.generateProjectReport(ArrayList(agentFileInputs))
        }
        projectReport
    }
    override suspend fun analyzeRemoteDiff(): String = withContext(Dispatchers.IO) {
        if (projectReport.isEmpty()) {
            projectReport = makeInitProjectReport()
        }

        // Get all files that changed with diffs
        val changedFilesWithDiffs = gitDiffService.getChangedFilesWithDiffs()

        if (changedFilesWithDiffs.isEmpty()) {
            return@withContext "No changes detected"
        }

        // Create AgentFileInput array
        val agentFileDiffInputs = changedFilesWithDiffs.map { (filePath, diff) ->
            // Get file facts from GraphReader
            val fileReport = graphReader.getFileFacts(filePath, ArrayList(changedFilesWithDiffs.keys))
            // Get file content
            val fileContent = FileUtils.getFileContent(project, filePath)

            AgentFileDiffInput(
                fileContent = fileContent,
                fileDiff = diff,
                fileReport = fileReport
            )
        }
        agentService.generateDiffReport(ArrayList(agentFileDiffInputs), projectReport)
    }
}
