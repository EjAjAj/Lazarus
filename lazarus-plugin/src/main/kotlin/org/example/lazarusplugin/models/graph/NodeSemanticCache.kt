package org.example.lazarusplugin.models.graph

class NodeSemanticCache(
    private val _cache: MutableMap<NodeId, CacheEntry> = mutableMapOf()
) {
    data class CacheEntry(
        val nodeReference: Node,
        val cachedSemantic: String
    )

    val isEmpty: Boolean
        get() = _cache.isEmpty()

    fun put(id: NodeId, entry: CacheEntry) {
        _cache[id] = entry
    }

    fun get(id: NodeId): CacheEntry? = _cache[id]

    fun contains(id: NodeId): Boolean = _cache.containsKey(id)

    fun remove(id: NodeId): CacheEntry? = _cache.remove(id)

    fun getEntries(): List<CacheEntry> = _cache.values.toList()

    fun clear() {
        _cache.clear()
    }

    fun updateSemantic(id: NodeId, newSemantic: String): Boolean {
        val entry = _cache[id] ?: return false
        _cache[id] = entry.copy(cachedSemantic = newSemantic)
        return true
    }
}