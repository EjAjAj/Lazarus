package org.example.lazarusplugin.models

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiField
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMethod
import java.util.concurrent.ConcurrentHashMap
import com.intellij.openapi.diagnostic.Logger

/**
 * Node types in the indexed graph
 */
enum class NodeType {
    MODULE, FILE, CLASS, METHOD, ATTRIBUTE
}

/**
 * Edge types representing semantic relationships
 */
enum class EdgeType {
    CALLS, INHERITS, IMPLEMENTS, IMPORTS, CONTAINS, ACCESSES
}

/**
 * Node in the indexed graph
 */
data class GraphNode(
    val id: String,
    val type: NodeType,
    val name: String,
    val outgoingEdges: MutableSet<String> = mutableSetOf(),
    val incomingEdges: MutableSet<String> = mutableSetOf()
)

/**
 * Edge in the indexed graph
 */
data class GraphEdge(
    val id: String,
    val type: EdgeType,
    val fromNodeId: String,
    val toNodeId: String
)

/**
 * Lightweight representation of PSI structure
 */
class IndexedGraph {
    // Registry mapping PSI element IDs to graph nodes
    val registry: MutableMap<String, GraphNode> = ConcurrentHashMap()

    // All edges in the graph
    val edges: MutableMap<String, GraphEdge> = ConcurrentHashMap()

    // Reverse mapping: file path -> node IDs contained in that file
    private val fileToNodes: MutableMap<String, MutableSet<String>> = ConcurrentHashMap()

    fun addNode(node: GraphNode) {
        registry[node.id] = node
    }

    fun addEdge(edge: GraphEdge) {
        edges[edge.id] = edge
        registry[edge.fromNodeId]?.outgoingEdges?.add(edge.id)
        registry[edge.toNodeId]?.incomingEdges?.add(edge.id)
    }

    fun removeNode(nodeId: String) {
        val node = registry.remove(nodeId) ?: return

        // Remove all edges connected to this node
        (node.incomingEdges + node.outgoingEdges).forEach { edgeId ->
            edges.remove(edgeId)
        }

        // Clean up references in other nodes
        node.outgoingEdges.forEach { edgeId ->
            edges[edgeId]?.let { edge ->
                registry[edge.toNodeId]?.incomingEdges?.remove(edgeId)
            }
        }
        node.incomingEdges.forEach { edgeId ->
            edges[edgeId]?.let { edge ->
                registry[edge.fromNodeId]?.outgoingEdges?.remove(edgeId)
            }
        }
    }

    fun registerFileNode(filePath: String, nodeId: String) {
        fileToNodes.computeIfAbsent(filePath) { mutableSetOf() }.add(nodeId)
    }

    fun getNodesForFile(filePath: String): Set<String> {
        return fileToNodes[filePath] ?: emptySet()
    }

    fun removeFileNodes(filePath: String) {
        fileToNodes[filePath]?.forEach { nodeId ->
            removeNode(nodeId)
        }
        fileToNodes.remove(filePath)
    }
}

/**
 * Builder for constructing and updating the indexed graph from PSI
 */
class GraphBuilder(private val project: Project) {

    private val psiOperator = PSIOperator(project)
    private val graph = IndexedGraph()

    companion object {
        private val logger = Logger.getInstance(GraphBuilder::class.java)
    }

    /**
     * Build the complete graph from scratch by traversing all source files
     */
    fun buildInitialGraph(): IndexedGraph {
        val allFiles = psiOperator.getAllSourceFiles()

        allFiles.forEach { file ->
            processFile(file)
        }

        return graph
    }

    /**
     * Update the graph for specific files that have been modified
     */
    fun updateGraphForFiles(changedFiles: List<PsiFile>) {
        changedFiles.forEach { file ->
            updateGraphForFile(file)
        }
    }

    /**
     * Update graph for a single file
     */
    private fun updateGraphForFile(file: PsiFile) {
        val filePath = file.virtualFile?.path ?: return

        // Remove all existing nodes for this file
        graph.removeFileNodes(filePath)

        // Rebuild nodes and edges for this file
        processFile(file)
    }

    /**
     * Process a single file and extract all nodes and edges
     */
    private fun processFile(file: PsiFile) {
        // Get the file path, assigning "non" if virtualFile or path is null
        val filePath = file.virtualFile?.path ?: "non"

        // Get the file ID
        val fileId = psiOperator.getElementId(file)

        // Log the fileId and filePath
        logger.info("Processing file - ID: $fileId, Path: $filePath")

        // Create file node
        val fileNode = GraphNode(
            id = fileId,
            type = NodeType.FILE,
            name = file.name
        )
        graph.addNode(fileNode)
        graph.registerFileNode(filePath, fileId)

        // Create module node and link module to file (reversed: module CONTAINS file)
        val moduleName = psiOperator.getModuleName(file)
        if (moduleName != null) {
            val moduleId = "module:$moduleName"
            if (!graph.registry.containsKey(moduleId)) {
                val moduleNode = GraphNode(
                    id = moduleId,
                    type = NodeType.MODULE,
                    name = moduleName
                )
                graph.addNode(moduleNode)
            }

            // Module contains file (reversed from BELONGS_TO)
            createEdge(moduleId, fileId, EdgeType.CONTAINS, "module-contains-file")
        }

        // Process imports
        processImports(file, fileId, filePath)

        // Process classes in file
        val classes = psiOperator.getClassesInFile(file)
        classes.forEach { psiClass ->
            processClass(psiClass, fileId, filePath)
        }
    }

    /**
     * Process imports in a file
     */
    private fun processImports(file: PsiFile, fileId: String, filePath: String) {
        val imports = psiOperator.getImports(file)

        imports.forEach { importStatement ->
            val importedElement = psiOperator.resolveImport(importStatement)
            if (importedElement is PsiClass) {
                val importedClassId = psiOperator.getElementId(importedElement)
                createEdge(fileId, importedClassId, EdgeType.IMPORTS, "import")
            }
        }
    }

    /**
     * Process a class and its members
     */
    private fun processClass(psiClass: PsiClass, fileId: String, filePath: String) {
        if (!psiOperator.isValid(psiClass)) return

        val classId = psiOperator.getElementId(psiClass)
        val className = psiClass.name ?: "Anonymous"

        // Create class node
        val classNode = GraphNode(
            id = classId,
            type = NodeType.CLASS,
            name = className
        )
        graph.addNode(classNode)
        graph.registerFileNode(filePath, classId)

        // File contains class (reversed from BELONGS_TO)
        createEdge(fileId, classId, EdgeType.CONTAINS, "file-contains-class")

        // Process inheritance
        val superClass = psiOperator.getSuperClass(psiClass)
        if (superClass != null && psiOperator.isValid(superClass)) {
            val superClassId = psiOperator.getElementId(superClass)
            createEdge(classId, superClassId, EdgeType.INHERITS, "inheritance")
        }

        // Process interfaces
        val interfaces = psiOperator.getInterfaces(psiClass)
        interfaces.forEach { interfaceClass ->
            if (psiOperator.isValid(interfaceClass)) {
                val interfaceId = psiOperator.getElementId(interfaceClass)
                createEdge(classId, interfaceId, EdgeType.IMPLEMENTS, "implements")
            }
        }

        // Process methods
        val methods = psiOperator.getMethodsInClass(psiClass)
        methods.forEach { method ->
            processMethod(method, classId, filePath)
        }

        // Process fields (attributes)
        val fields = psiOperator.getFieldsInClass(psiClass)
        fields.forEach { field ->
            processField(field, classId, filePath)
        }
    }

    /**
     * Process a method
     */
    private fun processMethod(method: PsiMethod, classId: String, filePath: String) {
        if (!psiOperator.isValid(method)) return

        val methodId = psiOperator.getElementId(method)
        val methodName = method.name

        // Create method node
        val methodNode = GraphNode(
            id = methodId,
            type = NodeType.METHOD,
            name = methodName
        )
        graph.addNode(methodNode)
        graph.registerFileNode(filePath, methodId)

        // Class contains method (reversed from BELONGS_TO)
        createEdge(classId, methodId, EdgeType.CONTAINS, "class-contains-method")

        // Process method calls
        val methodCalls = psiOperator.getMethodCalls(method)
        methodCalls.forEach { call ->
            val calledMethod = psiOperator.resolveMethodCall(call)
            if (calledMethod != null && psiOperator.isValid(calledMethod)) {
                val calledMethodId = psiOperator.getElementId(calledMethod)
                createEdge(methodId, calledMethodId, EdgeType.CALLS, "method-call")
            }
        }

        // Process field accesses
        val fieldRefs = psiOperator.getFieldReferences(method)
        fieldRefs.forEach { ref ->
            val field = ref.resolve()
            if (field is PsiField && psiOperator.isValid(field)) {
                val fieldId = psiOperator.getElementId(field)
                createEdge(methodId, fieldId, EdgeType.ACCESSES, "field-access")
            }
        }
    }

    /**
     * Process a field (attribute)
     */
    private fun processField(field: PsiField, classId: String, filePath: String) {
        if (!psiOperator.isValid(field)) return

        val fieldId = psiOperator.getElementId(field)
        val fieldName = field.name

        // Create field node
        val fieldNode = GraphNode(
            id = fieldId,
            type = NodeType.ATTRIBUTE,
            name = fieldName
        )
        graph.addNode(fieldNode)
        graph.registerFileNode(filePath, fieldId)

        // Class contains field (reversed from BELONGS_TO)
        createEdge(classId, fieldId, EdgeType.CONTAINS, "class-contains-field")
    }

    /**
     * Create an edge between two nodes
     */
    private fun createEdge(fromId: String, toId: String, type: EdgeType, prefix: String) {
        val edgeId = "$prefix:$fromId->$toId"
        val edge = GraphEdge(
            id = edgeId,
            type = type,
            fromNodeId = fromId,
            toNodeId = toId
        )
        graph.addEdge(edge)
    }

    fun getGraph(): IndexedGraph = graph
}