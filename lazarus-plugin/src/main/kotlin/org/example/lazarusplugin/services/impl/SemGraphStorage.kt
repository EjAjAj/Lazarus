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

    private lateinit var _graph: IndexedCodeGraph

    // This will be injected from plugin.xml via constructor parameter
    var storagePath: String = ".lazarus/semantic-graph.json"

    private val storageFile: File
        get() = File(project.basePath, storagePath)
    
    override fun getGraph(): IndexedCodeGraph {
        return _graph
    }
    
    override fun setGraph(graph: IndexedCodeGraph) {
        this._graph = graph
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
        return false
    }
}
