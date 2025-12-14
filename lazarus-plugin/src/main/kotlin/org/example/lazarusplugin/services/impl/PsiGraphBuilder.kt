package org.example.lazarusplugin.services.impl

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import org.example.lazarusplugin.models.graph.*
import org.example.lazarusplugin.services.api.GraphBuilder
import org.example.lazarusplugin.services.api.GraphStorage
import org.example.lazarusplugin.models.graph.IndexedCodeGraph

@Service(Service.Level.PROJECT)
class PsiGraphBuilder(
    private val project: Project
) : GraphBuilder {

    private val storage: GraphStorage
        get() = project.service<GraphStorage>()

    private val psiManager: PsiManager
        get() = PsiManager.getInstance(project)

    override fun buildGraph() {
        val newGraph = IndexedCodeGraph()

        ApplicationManager.getApplication().runReadAction {
            // Get all files in project
            val allFiles = collectAllSourceFiles()

            allFiles.forEach { file ->
                processFile(file, newGraph)
            }
        }

        storage.setGraph(newGraph)
        storage.saveToDisk()
    }

    override fun updateGraphForFile(file: String) {
        val graph = storage.getGraph()

        ApplicationManager.getApplication().runReadAction {
            val virtualFile = project.baseDir?.findFileByRelativePath(file) ?: return@runReadAction
            val psiFile = psiManager.findFile(virtualFile) ?: return@runReadAction

            // Remove all nodes associated with this file
            removeNodesForFile(psiFile, graph)

            // Re-process the file
            processFile(psiFile, graph)
        }

        storage.saveToDisk()
    }

    private fun collectAllSourceFiles(): List<PsiFile> {
        val files = mutableListOf<PsiFile>()

        PsiManager.getInstance(project).findDirectory(project.baseDir!!)?.let { rootDir ->
            collectFilesRecursively(rootDir, files)
        }

        return files
    }

    private fun collectFilesRecursively(directory: PsiDirectory, files: MutableList<PsiFile>) {
        directory.files.forEach { file ->
            if (file.name.endsWith(".java") || file.name.endsWith(".kt")) {
                files.add(file)
            }
        }

        directory.subdirectories.forEach { subDir ->
            collectFilesRecursively(subDir, files)
        }
    }

    private fun processFile(psiFile: PsiFile, graph: IndexedCodeGraph) {
        val filePath = psiFile.virtualFile?.path ?: return
        val fileName = psiFile.virtualFile?.path ?: psiFile.name

        // Create file node
        val fileId = NodeId("file:$filePath")
        val fileNode = Node(
            id = fileId,
            psiElementPointer = SmartPointerManager.getInstance(project).createSmartPsiElementPointer(psiFile),
            name = fileName,
            type = NodeType.FILE,
            filename = fileName
        )
        graph.addNode(fileNode)

        // Process classes in file
        val classes = PsiTreeUtil.findChildrenOfType(psiFile, PsiClass::class.java)
        classes.forEach { psiClass ->
            processClass(psiClass, fileNode, graph)
        }
    }

    private fun processClass(psiClass: PsiClass, fileNode: Node, graph: IndexedCodeGraph) {
        val className = psiClass.qualifiedName ?: psiClass.name ?: return
        val classId = NodeId("class:$className")

        // Create class node
        val classNode = Node(
            id = classId,
            psiElementPointer = SmartPointerManager.getInstance(project).createSmartPsiElementPointer(psiClass),
            name = psiClass.name ?: "Anonymous",
            type = NodeType.CLASS,
            filename = fileNode.name
        )
        graph.addNode(classNode)

        // File contains class
        val fileContainsClassEdge = Edge(
            id = EdgeId("contains:${fileNode.id.value}->${classNode.id.value}"),
            from = fileNode,
            to = classNode,
            type = EdgeType.CONTAINS,
            psiReference = null
        )
        graph.addEdge(fileContainsClassEdge)

        // Process inheritance
        psiClass.superClass?.let { superClass ->
            if (superClass.qualifiedName != "java.lang.Object") {
                val superClassId = NodeId("class:${superClass.qualifiedName}")

                // Create super class node if it doesn't exist
                if (graph.getNode(superClassId) == null) {
                    val superClassNode = Node(
                        id = superClassId,
                        psiElementPointer = SmartPointerManager.getInstance(project).createSmartPsiElementPointer(superClass),
                        name = superClass.name ?: "Unknown",
                        type = NodeType.CLASS,
                        filename = superClass.containingFile?.name ?: "Unknown"
                    )
                    graph.addNode(superClassNode)
                }

                val inheritsEdge = Edge(
                    id = EdgeId("inherits:${classNode.id.value}->${superClassId.value}"),
                    from = classNode,
                    to = graph.getNode(superClassId)!!,
                    type = EdgeType.INHERITS,
                    psiReference = null
                )
                graph.addEdge(inheritsEdge)
            }
        }

        // Process methods
        psiClass.methods.forEach { method ->
            processMethod(method, classNode, graph)
        }

        // Process fields
        psiClass.fields.forEach { field ->
            processField(field, classNode, graph)
        }

        // Process inner classes
        psiClass.innerClasses.forEach { innerClass ->
            processClass(innerClass, fileNode, graph)
        }
    }

    private fun processMethod(psiMethod: PsiMethod, classNode: Node, graph: IndexedCodeGraph) {
        val methodName = psiMethod.name
        val methodId = NodeId("method:${classNode.id.value}::$methodName")

        // Create method node
        val methodNode = Node(
            id = methodId,
            psiElementPointer = SmartPointerManager.getInstance(project).createSmartPsiElementPointer(psiMethod),
            name = methodName,
            type = NodeType.METHOD,
            filename = classNode.filename
        )
        graph.addNode(methodNode)

        // Class contains method
        val classContainsMethodEdge = Edge(
            id = EdgeId("contains:${classNode.id.value}->${methodNode.id.value}"),
            from = classNode,
            to = methodNode,
            type = EdgeType.CONTAINS,
            psiReference = null
        )
        graph.addEdge(classContainsMethodEdge)

        // Process method calls
        psiMethod.body?.let { body ->
            PsiTreeUtil.findChildrenOfType(body, PsiMethodCallExpression::class.java).forEach { callExpr ->
                val resolvedMethod = callExpr.resolveMethod()
                if (resolvedMethod != null) {
                    val calledMethodId = NodeId("method:${resolvedMethod.containingClass?.qualifiedName}::${resolvedMethod.name}")

                    // Create called method node if doesn't exist
                    if (graph.getNode(calledMethodId) == null) {
                        val calledMethodNode = Node(
                            id = calledMethodId,
                            psiElementPointer = SmartPointerManager.getInstance(project).createSmartPsiElementPointer(resolvedMethod),
                            name = resolvedMethod.name,
                            type = NodeType.METHOD,
                            filename = resolvedMethod.containingFile?.name ?: "Unknown"
                        )
                        graph.addNode(calledMethodNode)
                    }

                    val callsEdge = Edge(
                        id = EdgeId("calls:${methodNode.id.value}->${calledMethodId.value}"),
                        from = methodNode,
                        to = graph.getNode(calledMethodId)!!,
                        type = EdgeType.CALLS,
                        psiReference = callExpr.methodExpression.reference
                    )
                    graph.addEdge(callsEdge)
                }
            }

            // Process field accesses
            PsiTreeUtil.findChildrenOfType(body, PsiReferenceExpression::class.java).forEach { refExpr ->
                val resolved = refExpr.resolve()
                if (resolved is PsiField) {
                    val fieldId = NodeId("field:${resolved.containingClass?.qualifiedName}::${resolved.name}")

                    if (graph.getNode(fieldId) != null) {
                        val accessesEdge = Edge(
                            id = EdgeId("accesses:${methodNode.id.value}->${fieldId.value}"),
                            from = methodNode,
                            to = graph.getNode(fieldId)!!,
                            type = EdgeType.ACCESSES,
                            psiReference = refExpr.reference
                        )
                        graph.addEdge(accessesEdge)
                    }
                }
            }
        }
    }

    private fun processField(psiField: PsiField, classNode: Node, graph: IndexedCodeGraph) {
        val fieldName = psiField.name
        val fieldId = NodeId("field:${classNode.id.value}::$fieldName")

        // Create field node
        val fieldNode = Node(
            id = fieldId,
            psiElementPointer = SmartPointerManager.getInstance(project).createSmartPsiElementPointer(psiField),
            name = fieldName,
            type = NodeType.VARIABLE,
            filename = classNode.filename
        )
        graph.addNode(fieldNode)

        // Class contains field
        val classContainsFieldEdge = Edge(
            id = EdgeId("contains:${classNode.id.value}->${fieldNode.id.value}"),
            from = classNode,
            to = fieldNode,
            type = EdgeType.CONTAINS,
            psiReference = null
        )
        graph.addEdge(classContainsFieldEdge)
    }

    private fun removeNodesForFile(psiFile: PsiFile, graph: IndexedCodeGraph) {
        val filePath = psiFile.virtualFile?.path ?: return

        // Find all nodes associated with this file
        val nodesToRemove = graph.nodes.values
            .filter { node ->
                val psiElement = node.psiElementPointer.element
                psiElement?.containingFile?.virtualFile?.path == filePath
            }
            .map { it.id }

        // Remove all nodes
        nodesToRemove.forEach { nodeId ->
            graph.removeNode(nodeId)
        }
    }
}