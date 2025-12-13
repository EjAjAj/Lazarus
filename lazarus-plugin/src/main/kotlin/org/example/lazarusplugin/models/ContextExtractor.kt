package org.example.lazarusplugin.models

import com.intellij.psi.PsiElement

object ContextExtractor {

    fun extract(element: PsiElement, maxLines: Int = 5): String {
        return element.text
            .lineSequence()
            .take(maxLines)
            .joinToString("\n")
    }
}