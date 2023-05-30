package com.vlnabatov.alabaster.extensions.annotation

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity.INFORMATION
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.MARKUP_ENTITY
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.*
import com.intellij.psi.JavaTokenType.*
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType

import com.vlnabatov.alabaster.annotateSeparationMarks

private val valTokens = setOf(TRUE_KEYWORD, FALSE_KEYWORD, NULL_KEYWORD)

class JavaAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        try {
            // constants
            if (element.elementType in valTokens) {
                holder.newSilentAnnotation(INFORMATION).textAttributes(MARKUP_ENTITY).create()
            }
            // strings
            if (element.elementType === STRING_LITERAL || element.elementType === TEXT_BLOCK_LITERAL) {
                val numberOfQuotationMarks = if (element.elementType === TEXT_BLOCK_LITERAL) 3 else 1

                annotateSeparationMarks(element, holder, BRACES, numberOfQuotationMarks)
            }
        } catch (e: Exception) {
            /* Should not happen */
        }
    }
}
