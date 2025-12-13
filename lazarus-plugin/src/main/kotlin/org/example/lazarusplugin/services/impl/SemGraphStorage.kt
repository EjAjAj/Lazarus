package org.example.lazarusplugin.services.impl

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import org.example.lazarusplugin.models.IndexedCodeGraph
import org.example.lazarusplugin.services.api.GraphStorage
import java.io.File

/**
 * Service for managing the indexed code graph with disk persistence
 */
@Service(Service.Level.PROJECT)
class SemGraphStorage(
    private val project: Project
) : GraphStorage {

    private var _graph: IndexedCodeGraph? = null
    private var _graphReady: Boolean = false

    // This will be injected from plugin.xml via constructor parameter
    var storagePath: String = ".lazarus/semantic-graph.json"

    private val storageFile: File
        get() = File(project.basePath, storagePath)

    override fun getGraph(): IndexedCodeGraph {
        return _graph ?: IndexedCodeGraph().also { _graph = it }
    }

    override fun setGraph(graph: IndexedCodeGraph) {
        this._graph = graph
        this._graphReady = true
    }

    override fun isGraphReady(): Boolean {
        return _graphReady
    }
    
    override fun saveToDisk() {
        storageFile.parentFile?.mkdirs()
        println("TODO: Save graph to ${storageFile.absolutePath}")
    }
    
    override fun load(): Boolean {
        if (!storageFile.exists()) {
            println("No saved graph found at ${storageFile.absolutePath}")
            return false
        }

        println("TODO: Load graph from ${storageFile.absolutePath}")
        _graphReady = true
        return true
    }
}
