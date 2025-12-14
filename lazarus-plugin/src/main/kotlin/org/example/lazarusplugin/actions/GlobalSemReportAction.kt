package org.example.lazarusplugin.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.components.service
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.example.lazarusplugin.services.graph.api.GraphAnalysis
import org.example.lazarusplugin.services.graph.api.GraphStorage
import org.example.lazarusplugin.ui.MarkdownReportDialog

class GlobalSemReportAction : AnAction("Generate Global Semantic Report") {

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        val graphAnalysis = project.service<GraphAnalysis>()
        println(graphAnalysis)
        CoroutineScope(Dispatchers.Default).launch {
            val markdownContent = graphAnalysis.makeInitProjectReport()

            invokeLater {
                val dialog = MarkdownReportDialog(
                    project = project,
                    title = "Global Semantic Report - ${project.name}",
                    markdownContent = markdownContent
                )
                dialog.show()
            }
        }
    }

    override fun update(e: AnActionEvent) {
        val project = e.project
        // Action is always visible but only enabled when graph is ready
        val graphReady = project?.service<GraphStorage>()?.isGraphReady() ?: false
        e.presentation.isVisible = project != null
        e.presentation.isEnabled = graphReady
    }
}
