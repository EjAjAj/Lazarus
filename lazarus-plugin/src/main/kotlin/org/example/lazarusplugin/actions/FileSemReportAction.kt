package org.example.lazarusplugin.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.components.service
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.example.lazarusplugin.services.graph.api.GraphAnalysis
import org.example.lazarusplugin.services.graph.api.GraphStorage
import org.example.lazarusplugin.ui.MarkdownReportDialog

class FileSemReportAction : AnAction("Generate Semantic Report") {

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR)
        val psiFile = e.getData(CommonDataKeys.PSI_FILE)
        val project = e.project

        if (editor == null || psiFile == null || project == null) {
            return
        }

        val graphAnalysis = project.service<GraphAnalysis>()
        val filePath = psiFile.virtualFile.path

        CoroutineScope(Dispatchers.Default).launch {
            val markdownContent = graphAnalysis.analyzeFile(filePath)
            println(markdownContent)
            invokeLater {
                val dialog = MarkdownReportDialog(
                    project = project,
                    title = "Semantic Report - ${psiFile.name}",
                    markdownContent = markdownContent
                )
                dialog.show()
            }
        }
    }

    override fun update(e: AnActionEvent) {
        val project = e.project
        val editor = e.getData(CommonDataKeys.EDITOR)
        val psiFile = e.getData(CommonDataKeys.PSI_FILE)

        // Action is enabled only if file is open AND graph is ready
        val graphReady = project?.service<GraphStorage>()?.isGraphReady() ?: false
        e.presentation.isEnabledAndVisible = editor != null && psiFile != null && graphReady
    }
}
