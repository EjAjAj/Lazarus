package org.example.lazarusplugin.models.graph

import org.example.lazarusplugin.models.graph.Edge
import org.example.lazarusplugin.models.graph.Node
import org.example.lazarusplugin.models.graph.NodeId
import kotlin.collections.get

class CodeGraph(
    internal val _nodes: MutableMap<NodeId, Node> = mutableMapOf(),
    internal val _edges: MutableMap<EdgeId, Edge> = mutableMapOf()
) {
    val nodes: Map<NodeId, Node>
        get() = _nodes

    val edges: Map<EdgeId, Edge>
        get() = _edges

    fun clear() {
        _nodes.clear()
        _edges.clear()
    }
}