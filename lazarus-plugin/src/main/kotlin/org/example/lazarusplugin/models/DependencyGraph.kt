package org.example.lazarusplugin.models

class DependencyGraph {


    private val nodes: MutableMap<NodeId, GraphNode> = mutableMapOf()


    fun addNode(node: GraphNode) {
        nodes[node.id] = node
    }


    fun addEdge(edge: GraphEdge) {
        val fromNode = nodes[edge.from]
            ?: error("From node ${edge.from} not found")
        val toNode = nodes[edge.to]
            ?: error("To node ${edge.to} not found")


        fromNode.outgoing.add(edge)
        toNode.incoming.add(edge)
    }


    fun getNode(id: NodeId): GraphNode? = nodes[id]


    fun allNodes(): Collection<GraphNode> = nodes.values


    fun neighbors(id: NodeId): Set<NodeId> {
        val node = nodes[id] ?: return emptySet()
        return buildSet {
            node.outgoing.forEach { add(it.to) }
            node.incoming.forEach { add(it.from) }
        }
    }


    fun bfs(start: NodeId, depth: Int): Set<NodeId> {
        val visited = mutableSetOf<NodeId>()
        val queue = ArrayDeque<Pair<NodeId, Int>>()


        queue.add(start to 0)
        visited.add(start)


        while (queue.isNotEmpty()) {
            val (current, d) = queue.removeFirst()
            if (d == depth) continue


            val node = nodes[current] ?: continue
            node.outgoing.forEach {
                if (visited.add(it.to)) {
                    queue.add(it.to to d + 1)
                }
            }
        }
        return visited
    }

}
