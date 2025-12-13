package org.example.lazarusplugin.ui

import com.intellij.openapi.fileChooser.FileChooserFactory
import com.intellij.openapi.fileChooser.FileSaverDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.AlphaComposite
import java.awt.Graphics
import java.awt.Graphics2D
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
        isModal = false
        init()

        // Animate dialog appearance
        SwingUtilities.invokeLater {
            animateDialog()
        }
    }

    private fun animateDialog() {
        val dialog = window ?: return

        // Store original location and size
        val finalLocation = dialog.location
        val finalSize = dialog.size

        // Start from smaller size and slightly below
        val startOffset = 30
        val startScale = 0.80f
        dialog.setLocation(finalLocation.x, finalLocation.y + startOffset)
        dialog.setSize(
            (finalSize.width * startScale).toInt(),
            (finalSize.height * startScale).toInt()
        )

        var progress = 0f
        val timer = Timer(8) { e ->
            progress += 0.035f

            if (progress >= 1f) {
                progress = 1f
                dialog.setLocation(finalLocation.x, finalLocation.y)
                dialog.setSize(finalSize)
                (e.source as Timer).stop()
            } else {
                // Ease-out cubic for smooth deceleration
                val eased = 1f - (1f - progress).let { it * it * it }

                // Interpolate position
                val currentOffset = (startOffset * (1f - eased)).toInt()
                dialog.setLocation(finalLocation.x, finalLocation.y + currentOffset)

                // Interpolate size
                val currentScale = startScale + (1f - startScale) * eased
                dialog.setSize(
                    (finalSize.width * currentScale).toInt(),
                    (finalSize.height * currentScale).toInt()
                )
            }
        }

        timer.start()
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
                val file = wrapper.file
                file.writeText(markdownContent)
                Messages.showInfoMessage(
                    project,
                    "Report saved successfully to:\n${file.absolutePath}",
                    "Report Saved"
                )
                close(OK_EXIT_CODE)
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
        // Convert markdown to HTML
        val flavour = CommonMarkFlavourDescriptor()
        val parsedTree = MarkdownParser(flavour).buildMarkdownTreeFromString(markdownContent)
        val html = HtmlGenerator(markdownContent, parsedTree, flavour).generateHtml()

        // Load CSS from resources
        val cssContent = javaClass.getResourceAsStream("/html/markdown-report-style.css")
            ?.bufferedReader()
            ?.use { it.readText() }
            ?: ""

        // Create styled HTML with CSS from resources
        val styledHtml = """
            <html>
            <head>
                <style>
                    $cssContent
                </style>
            </head>
            <body>$html</body>
            </html>
        """.trimIndent()

        // Create JEditorPane to render HTML
        val editorPane = JEditorPane("text/html", styledHtml).apply {
            isEditable = false
            background = UIUtil.getPanelBackground()
            border = JBUI.Borders.empty(5)
        }

        // Wrap in scroll pane with no horizontal scrollbar
        val scrollPane = JBScrollPane(editorPane).apply {
            preferredSize = Dimension(900, 600)
            horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        }

        val panel = JPanel(BorderLayout())
        panel.border = JBUI.Borders.empty(10)
        panel.add(scrollPane, BorderLayout.CENTER)

        return panel
    }

    override fun createActions(): Array<Action> {
        val saveAction = object : DialogWrapperAction("Save Report") {
            override fun doAction(e: java.awt.event.ActionEvent?) {
                saveReport()
            }
        }
        saveAction.putValue(DialogWrapper.DEFAULT_ACTION, true)

        val closeAction = object : DialogWrapperAction("Close") {
            override fun doAction(e: java.awt.event.ActionEvent?) {
                close(OK_EXIT_CODE)
            }
        }

        return arrayOf(saveAction, closeAction)
    }
}
