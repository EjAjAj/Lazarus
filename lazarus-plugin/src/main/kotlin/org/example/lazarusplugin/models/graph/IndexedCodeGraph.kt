package org.example.lazarusplugin.models.graph

class IndexedCodeGraph(
    private val _graph: CodeGraph = CodeGraph(),
    private val _semanticCache: NodeSemanticCache = NodeSemanticCache(),
    private val _nodesByDegreeDescending: ArrayList<NodeId> = ArrayList(),
    private val _filepathToNode: MutableMap<String, Node> = mutableMapOf(),
) {

    enum class DetailLevel(val percentage: Double) {
        LOW(0.01),      // 1%
        MEDIUM(0.05),   // 5%
        HIGH(0.10)      // 10%
    }

    // Graph iterators

    fun getHottestFiles(detailLevel: DetailLevel = DetailLevel.LOW) : ArrayList<String> {
        val hottestNodes = getTopDegreeNodes(detailLevel)
        val hottestFiles = mutableSetOf<String>()
        hottestNodes.forEach { node ->
            // Skip if filename already compiled
            if (node.filename in hottestFiles) {
                return@forEach
            }

            hottestFiles.add(node.filename)
        }

        return ArrayList(hottestFiles)
    }

    fun getRelevantEdges(filename: String, depth: Int): ArrayList<Edge> {
        println(filename)
        val startNode = _filepathToNode[filename] ?: return ArrayList()
        val visitedEdges = mutableSetOf<Edge>()
        getRelevantEdges(startNode, depth, visitedEdges)
        println(visitedEdges.size)
        return ArrayList(visitedEdges)
    }

    private fun getRelevantEdges(currentNode: Node, depth: Int, visitedEdges: MutableSet<Edge>) {
        // Base case: if depth is 0, stop recursion
        if (depth <= 0) {
            return
        }

        // Get all edges connected to current node
        val connectedEdges = currentNode.outgoing + currentNode.incoming

        // Process each edge
        connectedEdges.forEach { edge ->
            // Skip if already visited
            if (edge in visitedEdges) {
                return@forEach
            }

            // Mark as visited
            visitedEdges.add(edge)

            // Get the next node (the one we haven't visited yet)
            val nextNode = if (edge.from.id == currentNode.id) {
                edge.to
            } else {
                edge.from
            }

            // Recursively get edges from next node with reduced depth
            getRelevantEdges(nextNode, depth - 1, visitedEdges)
        }
    }

    // Graph accessors
    val nodes: Map<NodeId, Node>
        get() = _graph.nodes

    val edges: Map<EdgeId, Edge>
        get() = _graph.edges

    // Node operations
    fun addNode(node: Node) {
        _graph._nodes[node.id] = node
        _nodesByDegreeDescending.add(node.id)
        if (node.type == NodeType.FILE) {
            _filepathToNode[node.filename] = node
        }
        resortNodesByDegree()
    }

    fun getNode(id: NodeId): Node? {
        return _graph.nodes[id]
    }

    fun removeNode(id: NodeId): Node? {
        val node = _graph.nodes[id] ?: return null

        // Get all connected edges
        val connectedEdges = _graph.edges.values.filter { it.from.id == id || it.to.id == id }

        // Remove all connected edges
        connectedEdges.forEach { edge ->
            _graph._edges.remove(edge.id)

            val fromNode = _graph.nodes[edge.from.id]
            val toNode = _graph.nodes[edge.to.id]

            if (fromNode != null && fromNode.id != id) {
                fromNode.outgoing.remove(edge)
            }

            if (toNode != null && toNode.id != id) {
                toNode.incoming.remove(edge)
            }
        }

        // Remove node
        _graph._nodes.remove(id)
        _nodesByDegreeDescending.remove(id)
        _semanticCache.remove(id)

        // Resort since degrees changed
        if (connectedEdges.isNotEmpty()) {
            resortNodesByDegree()
        }

        return node
    }

    // Edge operations
    fun addEdge(edge: Edge) {
        _graph._edges[edge.id] = edge

        val fromNode = _graph.nodes[edge.from.id]
        val toNode = _graph.nodes[edge.to.id]

        fromNode?.outgoing?.add(edge)

        toNode?.incoming?.add(edge)

        resortNodesByDegree()
    }

    fun getEdge(id: EdgeId): Edge? {
        return _graph.edges[id]
    }

    fun removeEdge(id: EdgeId): Edge? {
        val edge = _graph.edges[id] ?: return null

        _graph._edges.remove(id)

        val fromNode = _graph.nodes[edge.from.id]
        val toNode = _graph.nodes[edge.to.id]

        fromNode?.outgoing?.remove(edge)

        toNode?.incoming?.remove(edge)

        resortNodesByDegree()

        return edge
    }

    fun getNodeEdges(nodeId: NodeId): Set<Edge> {
        val node = _graph.nodes[nodeId] ?: return emptySet()
        return node.outgoing + node.incoming
    }

    private fun resortNodesByDegree() {
        _nodesByDegreeDescending.sortWith(compareByDescending { nodeId ->
            _graph.nodes[nodeId]?.getDegree() ?: -1
        })
    }

    // Semantic cache operations
    fun cacheSemantic(nodeId: NodeId, semantic: String) {
        val node = _graph.nodes[nodeId] ?: return
        _semanticCache.put(nodeId, NodeSemanticCache.CacheEntry(node, semantic))
    }

    fun getCachedSemantic(nodeId: NodeId): String? {
        return _semanticCache.get(nodeId)?.cachedSemantic
    }

    fun hasCachedSemantic(nodeId: NodeId): Boolean {
        return _semanticCache.contains(nodeId)
    }

    fun updateCachedSemantic(nodeId: NodeId, newSemantic: String): Boolean {
        return _semanticCache.updateSemantic(nodeId, newSemantic)
    }

    fun removeCachedSemantic(nodeId: NodeId): NodeSemanticCache.CacheEntry? {
        return _semanticCache.remove(nodeId)
    }

    // Degree-based query operations
    fun getTopDegreeNodes(detailLevel: DetailLevel = DetailLevel.LOW): List<Node> {
        val totalNodes = _graph.nodes.size
        if (totalNodes == 0) return emptyList()

        val nodesToReturn = maxOf(1, (totalNodes * detailLevel.percentage).toInt())

        return _nodesByDegreeDescending
            .take(nodesToReturn)
            .mapNotNull { nodeId -> _graph.nodes[nodeId] }
    }

    fun getTopNDegreeNodes(n: Int): List<Node> {
        return _nodesByDegreeDescending
            .take(n)
            .mapNotNull { nodeId -> _graph.nodes[nodeId] }
    }

    // Utility operations
    fun clear() {
        _graph.clear()
        _semanticCache.clear()
        _nodesByDegreeDescending.clear()
    }

    fun isEmpty(): Boolean {
        return _graph.nodes.isEmpty()
    }

    fun size(): Int {
        return _graph.nodes.size
    }

    // Query operations
    fun getNodesWithCachedSemantics(): List<Node> {
        return _semanticCache.getEntries().map { it.nodeReference }
    }

    fun getNodesByType(type: NodeType): List<Node> {
        return _graph.nodes.values.filter { it.type == type }
    }

    fun getNodesByDegree(minDegree: Int): List<Node> {
        return _graph.nodes.values.filter { it.getDegree() >= minDegree }
    }
}