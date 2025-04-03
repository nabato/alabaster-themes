package com.vlnabatov.alabaster.extensions.annotation

import com.goide.GoParserDefinition.Lazy.STRING_LITERALS
import com.goide.GoTypes.IDENTIFIER
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity.INFORMATION
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.BRACES
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.NUMBER
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.vlnabatov.alabaster.annotateSeparationMarks


class GoAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element !is LeafPsiElement) return

        try {
            when (element.elementType) {
                in STRING_LITERALS -> annotateSeparationMarks(element, holder, BRACES)
                IDENTIFIER if (element.text == "nil") ->
                    holder.newSilentAnnotation(INFORMATION).textAttributes(NUMBER).create()
            }
        } catch (e: Exception) {
            /* Should not happen */
        }


    }
}
