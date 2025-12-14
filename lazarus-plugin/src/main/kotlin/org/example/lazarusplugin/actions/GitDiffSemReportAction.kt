package org.example.lazarusplugin.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.components.service
import kotlinx.coroutines.runBlocking
import org.example.lazarusplugin.services.api.GraphAnalysis
import org.example.lazarusplugin.services.api.GraphStorage
import org.example.lazarusplugin.ui.MarkdownReportDialog

class GitDiffSemReportAction(
    private val graphAnalysis: GraphAnalysis
) : AnAction("Fetch & Compare Semantics") {

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        // Get analysis result from analyzeRemoteDiff
        val markdownContent = runBlocking {
            graphAnalysis.analyzeRemoteDiff()
        }

        // Show dialog with markdown content and merge button
        val dialog = MarkdownReportDialog(
            project = project,
            title = "Git Semantic Diff Report",
            markdownContent = markdownContent
        )
        dialog.show()
    }

    override fun update(e: AnActionEvent) {
        val project = e.project
        // Action is always visible but only enabled when graph is ready
        val graphReady = project?.service<GraphStorage>()?.isGraphReady() ?: false
        e.presentation.isVisible = project != null
        e.presentation.isEnabled = graphReady
    }
}
