package org.example.lazarusplugin.models

class GraphNode(
    val id: NodeId,
    val kind: NodeKind,
    val psiRef: PsiRef?,
    val context: String,
    val metadata: Map<String, String> = emptyMap(),
    val outgoing: MutableSet<GraphEdge> = mutableSetOf(),
    val incoming: MutableSet<GraphEdge> = mutableSetOf()
)
{
    override fun toString(): String {
        return "GraphNode(id=$id, kind=$kind, context='$context', metadata=$metadata)"
    }

}
