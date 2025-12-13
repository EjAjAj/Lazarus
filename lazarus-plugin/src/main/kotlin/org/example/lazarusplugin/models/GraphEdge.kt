package org.example.lazarusplugin.models

data class GraphEdge(
    val from: NodeId,
    val to: NodeId,
    val kind: EdgeKind
    val psiRef: PsiReferene?
)