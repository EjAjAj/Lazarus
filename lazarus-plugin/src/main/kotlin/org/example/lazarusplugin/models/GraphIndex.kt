package org.example.lazarusplugin.models

class GraphIndex {


    data class IndexEntry(
        val psiRef: PsiRef?,
        val context: String,
        val nodeRef : GraphNode
    )


    private val index: MutableMap<NodeId, IndexEntry> = mutableMapOf()


    fun put(id: NodeId, entry: IndexEntry) {
        index[id] = entry
    }


    fun get(id: NodeId): IndexEntry? = index[id]


    fun contains(id: NodeId): Boolean = index.containsKey(id)
}