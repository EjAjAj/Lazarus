package org.example.lazarusplugin.models

import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ClassInheritorsSearch
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.roots.ProjectFileIndex

/**
 * Wrapper for PSI operations to abstract away direct PSI API calls.
 * Provides methods for navigating and querying the PSI tree.
 */
class PSIOperator(private val project: Project) {

    private val psiManager = PsiManager.getInstance(project)
    private val javaPsiFacade = JavaPsiFacade.getInstance(project)

    /**
     * Get all source files in the project
     */
    fun getAllSourceFiles(): List<PsiFile> {
        val files = mutableListOf<PsiFile>()

        // 1. MUST wrap in a Read Action
        ApplicationManager.getApplication().runReadAction {
            val psiManager = PsiManager.getInstance(project)
            val fileIndex = ProjectFileIndex.getInstance(project)

            // 2. Use iterateContent instead of manual recursion
            fileIndex.iterateContent { virtualFile ->
                if (!virtualFile.isDirectory) {
                    // Only process source files (exclude binary/compiled files)
                    if (fileIndex.isInSource(virtualFile) || fileIndex.isInTestSourceContent(virtualFile)) {
                        // 3. Convert VirtualFile to PsiFile
                        val psiFile = psiManager.findFile(virtualFile)
                        // Only add files that have valid text ranges
                        if (psiFile != null && psiFile.textRange != null) {
                            files.add(psiFile)
                        }
                    }
                }
                true // Return true to continue iterating
            }
        }

        return files
    }

    /**
     * Get PSI file from VirtualFile
     */
    fun getPsiFile(virtualFile: VirtualFile): PsiFile? {
        return psiManager.findFile(virtualFile)
    }

    /**
     * Get a stable ID for a PSI element (using text range and file path)
     */
    fun getElementId(element: PsiElement): String {
        val file = element.containingFile
        val textRange = element.textRange
        val offset = textRange?.startOffset ?: 0
        val filePath = file?.virtualFile?.path ?: "unknown"

        return when (element) {
            is PsiClass -> "class:${element.qualifiedName ?: element.name}:$filePath:$offset"
            is PsiMethod -> "method:${element.containingClass?.qualifiedName}.${element.name}:$filePath:$offset"
            is PsiField -> "field:${element.containingClass?.qualifiedName}.${element.name}:$filePath:$offset"
            is PsiFile -> "file:$filePath"
            else -> "element:$filePath:$offset:${element.node?.elementType}"
        }
    }

    /**
     * Get all classes in a file
     */
    fun getClassesInFile(file: PsiFile): List<PsiClass> {
        return PsiTreeUtil.collectElementsOfType(file, PsiClass::class.java).toList()
    }

    /**
     * Get all methods in a class
     */
    fun getMethodsInClass(psiClass: PsiClass): List<PsiMethod> {
        return psiClass.methods.toList()
    }

    /**
     * Get all fields in a class
     */
    fun getFieldsInClass(psiClass: PsiClass): List<PsiField> {
        return psiClass.fields.toList()
    }

    /**
     * Get superclass of a class
     */
    fun getSuperClass(psiClass: PsiClass): PsiClass? {
        return psiClass.superClass
    }

    /**
     * Get all interfaces implemented by a class
     */
    fun getInterfaces(psiClass: PsiClass): List<PsiClass> {
        return psiClass.interfaces.toList()
    }

    /**
     * Get all inheritors of a class
     */
    fun getInheritors(psiClass: PsiClass): List<PsiClass> {
        return ClassInheritorsSearch.search(psiClass, GlobalSearchScope.projectScope(project), true)
            .findAll()
            .toList()
    }

    /**
     * Find all method calls within a method
     */
    fun getMethodCalls(method: PsiMethod): List<PsiMethodCallExpression> {
        return PsiTreeUtil.collectElementsOfType(method, PsiMethodCallExpression::class.java).toList()
    }

    /**
     * Resolve a method call to its declaration
     */
    fun resolveMethodCall(methodCall: PsiMethodCallExpression): PsiMethod? {
        return methodCall.resolveMethod()
    }

    /**
     * Find all references to a PSI element
     */
    fun findReferences(element: PsiElement): List<PsiReference> {
        return ReferencesSearch.search(element, GlobalSearchScope.projectScope(project))
            .findAll()
            .toList()
    }

    /**
     * Get all imports in a Java file
     */
    fun getImports(file: PsiFile): List<PsiImportStatementBase> {
        if (file !is PsiJavaFile) return emptyList()
        return file.importList?.allImportStatements?.toList() ?: emptyList()
    }

    /**
     * Resolve an import statement to its target
     */
    fun resolveImport(importStatement: PsiImportStatementBase): PsiElement? {
        return importStatement.resolve()
    }

    /**
     * Get containing class of an element
     */
    fun getContainingClass(element: PsiElement): PsiClass? {
        return PsiTreeUtil.getParentOfType(element, PsiClass::class.java)
    }

    /**
     * Get containing method of an element
     */
    fun getContainingMethod(element: PsiElement): PsiMethod? {
        return PsiTreeUtil.getParentOfType(element, PsiMethod::class.java)
    }

    /**
     * Get package name of a file
     */
    fun getPackageName(file: PsiFile): String {
        if (file !is PsiJavaFile) return ""
        return file.packageName
    }

    /**
     * Get module containing a file
     */
    fun getModuleName(file: PsiFile): String? {
        val virtualFile = file.virtualFile ?: return null
        val moduleForFile = ProjectRootManager.getInstance(project)
            .fileIndex
            .getModuleForFile(virtualFile)
        return moduleForFile?.name
    }

    /**
     * Find field references within a method
     */
    fun getFieldReferences(method: PsiMethod): List<PsiReferenceExpression> {
        return PsiTreeUtil.collectElementsOfType(method, PsiReferenceExpression::class.java)
            .filter { it.resolve() is PsiField }
            .toList()
    }

    /**
     * Check if element is valid and accessible
     */
    fun isValid(element: PsiElement): Boolean {
        return element.isValid && element.containingFile != null
    }

    /**
     * Visit all elements in a file with a custom visitor
     */
    fun visitFile(file: PsiFile, visitor: PsiRecursiveElementWalkingVisitor) {
        file.accept(visitor)
    }

    // Helper method to recursively collect PSI files
    private fun collectPsiFiles(root: VirtualFile, files: MutableList<PsiFile>) {
        if (root.isDirectory) {
            root.children.forEach { child ->
                collectPsiFiles(child, files)
            }
        } else {
            getPsiFile(root)?.let { files.add(it) }
        }
    }
}