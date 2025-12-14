package org.example.lazarusplugin.services.impl

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import org.example.lazarusplugin.models.*
import org.example.lazarusplugin.services.api.GraphReader
import org.example.lazarusplugin.services.api.GraphStorage

/**
 * Default implementation of graph reader
 * Uses indexed queries for fast lookups
 */
@Service(Service.Level.PROJECT)
class SemGraphReader(
    private val project: Project,
    private val storage: GraphStorage
) : GraphReader {

    override fun getFileFacts(
        filePath: String,
        relevantFiles: Array<String>
    ): FileReport {
        TODO("Not yet implemented")
    }

}
