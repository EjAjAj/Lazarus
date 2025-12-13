package org.example.lazarusplugin.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * Dialog that asks user if they want to generate the semantic graph
 */
class GraphGenerationDialog(project: Project) : DialogWrapper(project) {

    init {
        title = "Generate Semantic Graph"
        init()
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel(BorderLayout())
        panel.border = JBUI.Borders.empty(10)

        val message = JBLabel(
            "<html>No semantic graph found for this project.<br/><br/>" +
                    "Would you like to generate it now?<br/><br/>" +
                    "This will analyze your codebase and create an indexed graph<br/>" +
                    "for better code navigation and understanding.</html>"
        )

        panel.add(message, BorderLayout.CENTER)
        return panel
    }

    override fun createActions() = arrayOf(okAction, cancelAction)

    init {
        okAction.putValue(javax.swing.Action.NAME, "Generate")
        cancelAction.putValue(javax.swing.Action.NAME, "Skip")
    }
}
