package org.example.lazarusplugin.models

import java.util.PriorityQueue


class NodeWeightManager(
    private val graph: DependencyGraph
) {

    private val weightMap: MutableMap<NodeId, Int> = mutableMapOf()

    private val hotQueue: PriorityQueue<WeightedNode> = PriorityQueue(Comparator { a, b ->
        val cmp = b.weight.compareTo(a.weight) // max-heap by weight
        if (cmp != 0) cmp else a.id.toString().compareTo(b.id.toString())
    })

    fun rebuildAll() {
        weightMap.clear()
        hotQueue.clear()

        graph.allNodes().forEach { node ->
            val weight = node.incoming.size + node.outgoing.size
            weightMap[node.id] = weight
            hotQueue.add(WeightedNode(node.id, weight))
        }
    }


    fun incrementWeight(nodeId: NodeId, delta: Int = 1) {
        val newWeight = (weightMap[nodeId] ?: 0) + delta
        weightMap[nodeId] = newWeight
        hotQueue.add(WeightedNode(nodeId, newWeight))
    }

    fun decrementWeight(nodeId: NodeId, delta: Int = 1) {
        val newWeight = (weightMap[nodeId] ?: 0) - delta
        weightMap[nodeId] = newWeight
        hotQueue.add(WeightedNode(nodeId, newWeight))
    }

    fun getWeight(nodeId: NodeId): Int =
        weightMap[nodeId] ?: 0


    fun getTopK(k: Int): List<NodeId> {
        val result = mutableListOf<NodeId>()
        val seen = mutableSetOf<NodeId>()

        while (hotQueue.isNotEmpty() && result.size < k) {
            val candidate = hotQueue.poll()
            val currentWeight = weightMap[candidate.id]

            if (currentWeight == candidate.weight && candidate.id !in seen) {
                result.add(candidate.id)
                seen.add(candidate.id)
            }
        }

        return result
    }

    fun getAllSorted(): List<NodeId> =
        weightMap.entries
            .sortedByDescending { it.value }
            .map { it.key }
}
