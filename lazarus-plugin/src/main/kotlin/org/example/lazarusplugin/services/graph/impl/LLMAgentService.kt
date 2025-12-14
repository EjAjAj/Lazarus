package org.example.lazarusplugin.services.impl

import com.intellij.diff.requests.DiffRequest
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.example.lazarusplugin.services.api.AgentFileDiffInput
import org.example.lazarusplugin.services.api.AgentFileInput
import org.example.lazarusplugin.services.api.AgentService
import org.example.lazarusplugin.services.api.FileReport
import org.example.lazarusplugin.utils.HttpClient


@Serializable
data class ApiResponse(
    val success: Boolean,
    val message: String,
    val data: String?
)

@Serializable
data class FileData(
    val name: String,
    val content: String
)

@Serializable
data class DiffReq(
    val general: FileData,
    val md_list: List<FileData>
)

@Service(Service.Level.PROJECT)
class LLMAgentService(private val project: Project) : AgentService {

    private val httpClient = service<HttpClient>()
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    override fun getFileSummary(agentFileInput: AgentFileInput, generalReport: String): String{
        val files = listOf(
                FileData(
                    name = agentFileInput.fileReport.filePath,
                    content = formatAgentInput(agentFileInput)
                )
        )
        val requestBody = DiffReq(md_list = files, general = FileData(name="projectReport.md", content = generalReport))
        val jsonBody = json.encodeToString(requestBody)
        val responseJson = httpClient.post("/incremental", jsonBody)
        val apiResponse = json.decodeFromString<ApiResponse>(responseJson)
        return apiResponse.data ?: ""

    }

    override fun generateProjectReport(agentFileInputs: ArrayList<AgentFileInput>): String{
        val files = agentFileInputs.map { agentFileInput ->
            mapOf(
                "name" to agentFileInput.fileReport.filePath,
                "content" to formatAgentInput(agentFileInput)
            )
        }
        val requestBody = mapOf("files" to files)
        val jsonBody = json.encodeToString(requestBody)
        val responseJson = httpClient.post("/general", jsonBody)
        val apiResponse = json.decodeFromString<ApiResponse>(responseJson)
        return apiResponse.data ?: ""
    }

    override fun generateDiffReport(inputs: ArrayList<AgentFileDiffInput>, generalReport: String): String {
        val files = inputs.map { input ->
            FileData(
                name = input.fileReport.filePath,
                content = formatAgentDiffInput(input)
            )
        }
        val requestBody = DiffReq(md_list = files, general = FileData(name="projectReport.md", content = generalReport))
        val jsonBody = json.encodeToString(requestBody)
        val responseJson = httpClient.post("/incremental", jsonBody)
        val apiResponse = json.decodeFromString<ApiResponse>(responseJson)
        return apiResponse.data ?: ""
    }


    private fun formatAgentInput(agentFileInput: AgentFileInput): String {
        val connectedFilesSection = agentFileInput.fileReport.connectedFiles.joinToString("; ")
        val connectionDescriptions = agentFileInput.fileReport.connectionDescriptions.joinToString("; ")
        return """# connected_files:$connectedFilesSection \nFile Name: \n${agentFileInput.fileReport.filePath}\nConnection Descriptions:\n$connectionDescriptions \nCurrent File:\n${agentFileInput.fileContent}""".trimIndent()
    }

    private fun formatAgentDiffInput(input: AgentFileDiffInput): String {
        val connectedFilesSection = input.fileReport.connectedFiles.joinToString("; ")
        val connectionDescriptions = input.fileReport.connectionDescriptions.joinToString("; ")
        return """# connected_files:$connectedFilesSection \nFile Name: \n${input.fileReport.filePath}\nConnection Descriptions:\n$connectionDescriptions \nCurrent File:\n${input.fileContent} \nFile Diff:\n ${input.fileDiff}\n""".trimIndent()
    }

}
