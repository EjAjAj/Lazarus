package org.example.lazarusplugin.models.graph

class IndexedCodeGraph(
    private val _graph: CodeGraph = CodeGraph(),
    private val _semanticCache: NodeSemanticCache = NodeSemanticCache(),
    private val _nodesByDegreeDescending: ArrayList<NodeId> = ArrayList(),
) {
    fun getRelativeEdges(filename: String, depth: Int): ArrayList<Edge> {
        // Create mock nodes
        val node1 = Node(
            id = NodeId("node1"),
            psiElementPointer = null as com.intellij.psi.SmartPsiElementPointer<com.intellij.psi.PsiElement>,
            name = "ExampleClass",
            type = NodeType.CLASS,
            filename = "Example1.kt"
        )

        val node2 = Node(
            id = NodeId("node2"),
            psiElementPointer = null as com.intellij.psi.SmartPsiElementPointer<com.intellij.psi.PsiElement>,
            name = "exampleMethod",
            type = NodeType.METHOD,
            filename = "Example2.kt"
        )

        // Create 2 hardcoded edges
        val edge1 = Edge(
            id = EdgeId("edge1"),
            from = node1,
            to = node2,
            type = EdgeType.CONTAINS,
            psiReference = null
        )

        val edge2 = Edge(
            id = EdgeId("edge2"),
            from = node2,
            to = node1,
            type = EdgeType.DEPENDS_ON,
            psiReference = null
        )

        return arrayListOf(edge1, edge2)
    }
}