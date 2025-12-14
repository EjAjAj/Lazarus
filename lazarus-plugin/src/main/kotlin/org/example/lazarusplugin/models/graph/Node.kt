package org.example.lazarusplugin.models.graph

import org.example.lazarusplugin.models.graph.Edge
import org.example.lazarusplugin.models.graph.NodeType
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.psi.PsiElement

class Node (
    val id: NodeId,
    val psiElementPointer: SmartPsiElementPointer<PsiElement>,
    val name: String,
    val type: NodeType,
    val filename: String,
    val outgoing: MutableSet<Edge> = mutableSetOf(),
    val incoming: MutableSet<Edge> = mutableSetOf()
) {
    fun getDegree(): Int {
        return outgoing.size + incoming.size
    }
    override fun toString(): String {
        return buildString {
            appendLine("Node: $name, Type: $type, From file: $filename")
        }
    }
}

@JvmInline
value class NodeId(val value: String)