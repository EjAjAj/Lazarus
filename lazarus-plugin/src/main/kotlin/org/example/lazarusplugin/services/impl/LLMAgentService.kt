package org.example.lazarusplugin.services.impl

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.example.lazarusplugin.services.api.IAgentService

/**
 * Service for communicating with external LLM
 * TODO: Implement actual HTTP client to call your LLM API
 */
@Service(Service.Level.PROJECT)
class LLMAgentService(private val project: Project) : IAgentService {

    override suspend fun getChangesSummary(file: VirtualFile, filesFacts: Array<String>, newFile: VirtualFile): String {
        // Dummy implementation
        return "Changes summary (dummy)"
    }

    override fun isAvailable(): Boolean {
        TODO("Not yet implemented")
    }

}
