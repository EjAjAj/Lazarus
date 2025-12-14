package org.example.lazarusplugin.services.impl

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import org.example.lazarusplugin.models.*
import org.example.lazarusplugin.models.graph.Edge
import org.example.lazarusplugin.services.api.FileReport
import org.example.lazarusplugin.services.api.GraphReader
import org.example.lazarusplugin.services.api.GraphStorage
import org.intellij.markdown.lexer.push

/**
 * Default implementation of graph reader
 * Uses indexed queries for fast lookups
 */
@Service(Service.Level.PROJECT)
class SemGraphReader(
    private val project: Project
) : GraphReader {

    private val storage: GraphStorage
        get() = project.service<GraphStorage>()

    override fun getFileFacts(
        filePath: String,
        relevantFiles: ArrayList<String>
    ): FileReport {
        val connectedFiles: MutableSet<String> = mutableSetOf()
        val connectionDescriptions: ArrayList<String> = ArrayList()
        val relativeEdges: ArrayList<Edge> = storage.getGraph().getRelevantEdges(filePath, 5)

        for (edge in relativeEdges) {
            connectionDescriptions.add(edge.toString())
            if (edge.from.filename in relevantFiles) {
                connectedFiles.add(edge.from.filename)
            }
            if (edge.to.filename in relevantFiles) {
                connectedFiles.add(edge.to.filename)
            }
        }

        return FileReport(filePath=filePath,
            connectedFiles=ArrayList(connectedFiles),
            connectionDescriptions=connectionDescriptions )
    }
    override fun getHotFiles(): ArrayList<String>{
        return storage.getGraph().getHottestFiles()
    }

}
