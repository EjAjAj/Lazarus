package org.example.lazarusplugin.models

data class GraphNode(
    val id: NodeId,
    val kind: NodeKind,
    val psiRef: PsiRef?,
    val context: String,
    val metadata: Map<String, String> = emptyMap(),
    val outgoing: MutableSet<GraphEdge> = mutableSetOf(),
    val incoming: MutableSet<GraphEdge> = mutableSetOf()
)