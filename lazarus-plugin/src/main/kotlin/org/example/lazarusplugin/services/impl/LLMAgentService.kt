package org.example.lazarusplugin.services.impl

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.example.lazarusplugin.services.api.AgentFileInput
import org.example.lazarusplugin.services.api.AgentService
import org.example.lazarusplugin.utils.HttpClient


@Service(Service.Level.PROJECT)
class LLMAgentService(private val project: Project) : AgentService {

    private val httpClient = service<HttpClient>()
    private val json = Json { prettyPrint = true }

    override suspend fun getChangesSummary(file: VirtualFile, filesFacts: Array<String>, newFile: VirtualFile): String {
        // Dummy implementation
        return "Changes summary (dummy)"
    }

    override fun isAvailable(): Boolean {
        TODO("Not yet implemented")
    }
    override fun generateReport(inputs: List<AgentFileInput>): String {
        val stringList = inputs.map { input -> formatAgentFileInput(input) }
        val jsonBody = json.encodeToString(stringList)
        return httpClient.post("/generate-report", jsonBody)
    }

    private fun formatAgentFileInput(input: AgentFileInput): String {
        val connectedFilesSection = input.fileReport.connectedFiles.joinToString("; ")
        val connectionDescriptions = input.fileReport.connectionDescriptions.joinToString("; ")
        return """# connected_files:$connectedFilesSection \nFile Name: \n${input.fileReport.filePath}# Connected Files:\n$connectedFilesSection\nConnection Descriptions:\n$connectionDescriptions \nCurrent File:\n${input.fileContent} \nFile Diff:\n ${input.fileDiff}\n""".trimIndent()
    }

}
