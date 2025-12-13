package org.example.lazarusplugin.models

import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.openapi.util.TextRange

data class PsiRef(
    val pointer: SmartPsiElementPointer<PsiElement>,
    val filePath: String,
    val textRange: TextRange
)