package org.example.lazarusplugin.services.api

import com.intellij.openapi.vfs.VirtualFile

/**
 * Interface for communicating with external LLM agent
 */
data class AgentFileDiffInput(
    val fileContent: String,
    val fileDiff: String,
    val fileReport: FileReport
)
data class AgentFileInput(
    val fileContent: String,
    val fileReport: FileReport,
)
interface AgentService {

    fun getFileSummary(agentFileInput: AgentFileInput, generalReport: String): String

    fun generateProjectReport(agentFileInputs: ArrayList<AgentFileInput>): String

    fun generateDiffReport(inputs: ArrayList<AgentFileDiffInput>, generalReport: String): String
}
