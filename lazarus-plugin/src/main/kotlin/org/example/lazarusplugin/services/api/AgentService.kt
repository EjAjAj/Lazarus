package org.example.lazarusplugin.services.api

import com.intellij.openapi.vfs.VirtualFile

/**
 * Interface for communicating with external LLM agent
 */
interface AgentService {

    /**
     * Get summary of changes between old file and new file
     */
    suspend fun getChangesSummary(file: VirtualFile, fileFacts: FileFacts, newFile: VirtualFile): String

    /**
     * Check if agent is available
     */
    fun isAvailable(): Boolean
}
