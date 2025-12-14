package org.example.lazarusplugin.services.impl

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.example.lazarusplugin.models.*
import org.example.lazarusplugin.services.api.GraphBuilder
import org.example.lazarusplugin.services.api.GraphStorage
import org.example.lazarusplugin.models.graph.IndexedCodeGraph

@Service(Service.Level.PROJECT)
class PsiGraphBuilder(
    private val project: Project
) : GraphBuilder {

    private val storage: GraphStorage
        get() = project.service<GraphStorage>()

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
