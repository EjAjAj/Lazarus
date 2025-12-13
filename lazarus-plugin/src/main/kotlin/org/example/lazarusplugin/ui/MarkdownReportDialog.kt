package org.example.lazarusplugin.ui

import com.intellij.openapi.fileChooser.FileChooserFactory
import com.intellij.openapi.fileChooser.FileSaverDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.*

/**
 * Dialog that displays markdown content in a scrollable text area
 */
class MarkdownReportDialog(
    private val project: Project,
    title: String,
    private val markdownContent: String
) : DialogWrapper(project) {

    init {
        this.title = title
        init()
    }

    private fun saveReport() {
        val descriptor = FileSaverDescriptor(
            "Save Semantic Report",
            "Choose location to save the markdown report",
            "md"
        )

        val fileChooserDialog = FileChooserFactory.getInstance()
            .createSaveFileDialog(descriptor, project)

        val virtualFileWrapper = fileChooserDialog.save(null as com.intellij.openapi.vfs.VirtualFile?, "semantic-report.md")

        virtualFileWrapper?.let { wrapper ->
            try {
                val file = wrapper.getFile(true)
                file.writeText(markdownContent)
                Messages.showInfoMessage(
                    project,
                    "Report saved successfully to:\n${file.absolutePath}",
                    "Report Saved"
                )
            } catch (e: Exception) {
                Messages.showErrorDialog(
                    project,
                    "Failed to save report: ${e.message}",
                    "Save Error"
                )
            }
        }
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel(BorderLayout())
        panel.border = JBUI.Borders.empty(10)

        // Create text area for markdown content
        val textArea = JTextArea(markdownContent).apply {
            isEditable = false
            lineWrap = true
            wrapStyleWord = true
            font = font.deriveFont(13f)
            border = JBUI.Borders.empty(5)
        }

        // Wrap in scroll pane
        val scrollPane = JBScrollPane(textArea).apply {
            preferredSize = Dimension(700, 500)
        }

        panel.add(scrollPane, BorderLayout.CENTER)
        return panel
    }

    override fun createActions(): Array<Action> {
        val saveAction = object : DialogWrapperAction("Save Report") {
            override fun doAction(e: java.awt.event.ActionEvent?) {
                saveReport()
            }
        }

        okAction.putValue(Action.NAME, "Close")
        return arrayOf(saveAction, okAction)
    }
}
