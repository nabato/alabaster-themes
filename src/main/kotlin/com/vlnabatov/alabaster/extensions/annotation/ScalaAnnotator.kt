package com.vlnabatov.alabaster.extensions.annotation

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity.INFORMATION
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.BRACES
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.NUMBER
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import com.vlnabatov.alabaster.annotateSeparationMarks
import kotlin.collections.setOf
import org.jetbrains.plugins.scala.lang.lexer.ScalaTokenTypes.BOOLEAN_TOKEN_SET
import org.jetbrains.plugins.scala.lang.lexer.ScalaTokenTypes.kNULL
import org.jetbrains.plugins.scala.lang.lexer.ScalaTokenTypes.NUMBER_TOKEN_SET
import org.jetbrains.plugins.scala.lang.lexer.ScalaTokenTypes.STRING_LITERAL_TOKEN_SET


// Search in org.jetbrains.plugins.scala.lang.lexer.ScalaTokenTypes
class ScalaAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        try {
            when (element.elementType) {
                // constants
                in setOf(*BOOLEAN_TOKEN_SET.types, *NUMBER_TOKEN_SET.types, kNULL) ->
                    holder.newSilentAnnotation(INFORMATION).textAttributes(NUMBER).create()
                // strings
                in STRING_LITERAL_TOKEN_SET -> annotateSeparationMarks(element, holder, BRACES)
            }
        } catch (e: Exception) {
            /* Should not happen */
        }
    }
}