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

    /**
     * Get summary of changes between old file and new file
     */
    suspend fun getChangesSummary(file: VirtualFile, filesFacts: Array<String>, newFile: VirtualFile): String

    /**
     * Check if agent is available
     */
    fun generateReport(inputs: List<AgentFileInput>): String
    fun isAvailable(): Boolean
}
