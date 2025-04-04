package com.vlnabatov.alabaster.extensions.annotation

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity.INFORMATION
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.BRACES
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.NUMBER
import com.intellij.psi.JavaTokenType.TEXT_BLOCK_LITERAL
import com.intellij.psi.JavaTokenType.TRUE_KEYWORD
import com.intellij.psi.JavaTokenType.FALSE_KEYWORD
import com.intellij.psi.JavaTokenType.NULL_KEYWORD
import com.intellij.psi.JavaTokenType.STRING_LITERAL
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import com.vlnabatov.alabaster.annotateSeparationMarks


class JavaAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        try {
            when (element.elementType) {
                TRUE_KEYWORD, FALSE_KEYWORD, NULL_KEYWORD ->
                    holder.newSilentAnnotation(INFORMATION).textAttributes(NUMBER).create()

                STRING_LITERAL -> annotateSeparationMarks(element, holder, BRACES)
                TEXT_BLOCK_LITERAL -> annotateSeparationMarks(element, holder, BRACES, 3)
            }
        } catch (e: Exception) {
            /* Should not happen */
        }
    }
}
