package org.example.lazarusplugin.models.graph

import org.example.lazarusplugin.models.graph.EdgeType
import org.example.lazarusplugin.models.graph.NodeId
import com.intellij.psi.PsiReference

class Edge (
    val id: EdgeId,
    val from: Node,
    val to: Node,
    val type: EdgeType,
    val psiReference: PsiReference?
)  {
    override fun toString(): String {
        return buildString {
            appendLine("($from) $type ($to)")
        }
    }
}

@JvmInline
value class EdgeId(val value: String)