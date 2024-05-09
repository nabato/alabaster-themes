package com.vlnabatov.alabaster.extensions.annotation

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity.INFORMATION
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.*
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import org.jetbrains.plugins.scala.lang.lexer.ScalaTokenTypes.*

import com.vlnabatov.alabaster.annotateSeparationMarks

private val valTokens = setOf(*BOOLEAN_TOKEN_SET.types, *NUMBER_TOKEN_SET.types, kNULL)


// Search in org.jetbrains.plugins.scala.lang.lexer.ScalaTokenTypes
class ScalaAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        try {
            when (element.elementType) {
                // constants
                in valTokens -> holder.newSilentAnnotation(INFORMATION).textAttributes(MARKUP_ENTITY).create()
                // strings
                in STRING_LITERAL_TOKEN_SET -> annotateSeparationMarks(element, holder, BRACES)
            }
        } catch (e: Exception) {
            /* Should not happen */
        }
    }
}