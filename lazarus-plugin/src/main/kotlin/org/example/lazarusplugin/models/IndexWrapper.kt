package org.example.lazarusplugin.models
import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPsiElementPointer

class IndexWrapper(
    private val graph: DependencyGraph = DependencyGraph(),
    private val index: GraphIndex = GraphIndex()
) {

    private val psiToNodeId: MutableMap<SmartPsiElementPointer<PsiElement>, NodeId> = mutableMapOf()

    private val nodeWeightManager: NodeWeightManager = NodeWeightManager(graph)


    fun registerNode(
        id: NodeId,
        kind: NodeKind,
        psiRef: PsiRef?,
        context: String,
        metadata: Map<String, String> = emptyMap()
    ) {
        val node = GraphNode(
            id = id,
            kind = kind,
            psiRef = psiRef,
            context = context,
            metadata = metadata
        )

        graph.addNode(node)

        index.put(
            id,
            GraphIndex.IndexEntry(
                psiRef = psiRef,
                context = context,
                nodeRef = node
            )
        )

        psiRef?.pointer?.let { psiToNodeId[it] = id }

    }

    fun unregisterNode(id: NodeId) {
        val node = graph.getNode(id) ?: return

        // Remove from psiToNodeId map
        node.psiRef?.pointer?.let { psiToNodeId.remove(it) }

        // Remove edges
        node.incoming.forEach { edge ->
            graph.removeEdge(edge)
            nodeWeightManager.decrementWeight(edge.from)
        }
        node.outgoing.forEach { edge ->
            graph.removeEdge(edge)
            nodeWeightManager.decrementWeight(edge.to)
        }

        // Remove node
        graph.removeNode(id)
    }

    fun registerEdge(from: NodeId, to: NodeId, kind: EdgeKind) {
        graph.addEdge(GraphEdge(from, to, kind))
        nodeWeightManager.incrementWeight(from)
        nodeWeightManager.incrementWeight(to)
    }

    fun getNodeByPsi(psi: SmartPsiElementPointer<PsiElement>): GraphNode? {
        val nodeId = psiToNodeId[psi] ?: return null
        return graph.getNode(nodeId)
    }

    fun getNode(id: NodeId): GraphNode? =
        graph.getNode(id)

    fun neighbors(id: NodeId): Set<GraphNode> =
        graph.neighbors(id)
            .mapNotNull { graph.getNode(it) }
            .toSet()

    fun bfs(start: NodeId, depth: Int): Set<GraphNode> {
        val ids = graph.bfs(start, depth)
        return ids.mapNotNull { graph.getNode(it) }.toSet()
    }

}

