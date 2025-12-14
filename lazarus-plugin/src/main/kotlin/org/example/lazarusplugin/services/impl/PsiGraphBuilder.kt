package org.example.lazarusplugin.services.impl

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import org.example.lazarusplugin.models.*
import org.example.lazarusplugin.services.api.IGraphStorage

@Service(Service.Level.PROJECT)
class GraphBuilder(
    private val project: Project
) : IGraphBuilder {

    private val storage: IGraphStorage
        get() = project.service<IGraphStorage>()

    override fun buildGraph() {
        val newGraph = IndexedCodeGraph()

        storage.setGraph(newGraph)
        storage.saveToDisk()
    }

    override fun updateGraphForFile(file: String) {
        // TODO: Parse file and update corresponding nodes/edges
        val graph = storage.getGraph()
        println("Updating graph for file: ${file}")
        // ... update logic ...
        storage.saveToDisk()
    }

}
