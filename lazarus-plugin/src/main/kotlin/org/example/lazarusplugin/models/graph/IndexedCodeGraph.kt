package org.example.lazarusplugin.models.graph

class IndexedCodeGraph(
    private val _graph: CodeGraph = CodeGraph(),
    private val _semanticCache: NodeSemanticCache = NodeSemanticCache(),
    private val _nodesByDegreeDescending: ArrayList<NodeId> = ArrayList(),
) {

}