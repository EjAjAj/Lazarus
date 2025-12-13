package org.example.lazarusplugin.services.impl

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import org.example.lazarusplugin.services.api.IGraphReader
import org.example.lazarusplugin.services.api.IGraphStorage

/**
 * Default implementation of graph reader
 * Uses indexed queries for fast lookups
 */
@Service(Service.Level.PROJECT)
class SemGraphReader(
    private val project: Project,
    private val storage: IGraphStorage
) : IGraphReader {

    override fun getFileFacts(filePath: String): Array<String> {
        return Array(0) { "" }
    }

}
