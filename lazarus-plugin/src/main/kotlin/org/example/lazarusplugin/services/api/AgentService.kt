package org.example.lazarusplugin.services.api

import com.intellij.openapi.vfs.VirtualFile

/**
 * Interface for communicating with external LLM agent
 */
data class AgentFileInput(
    val fileContent: String,
    val fileDiff: String,
    val fileReport: FileReport
)

interface AgentService {

    suspend fun getFileSummary(fileReport: FileReport, fileContent: String): String

    fun generateReport(inputs: List<AgentFileInput>): String
}
