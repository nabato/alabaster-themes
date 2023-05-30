package com.vlnabatov.alabaster.extensions.annotation

import com.vlnabatov.alabaster.annotateSeparationMarks
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity.INFORMATION
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.*
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.MARKUP_ENTITY
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.util.elementType
import org.jetbrains.kotlin.psi.stubs.elements.KtStubElementTypes.*


private val valTokens = setOf(NULL,BOOLEAN_CONSTANT, FLOAT_CONSTANT, CHARACTER_CONSTANT, INTEGER_CONSTANT)


class KtAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        try {
            // constants
            if (element.elementType in valTokens) {
                holder.newSilentAnnotation(INFORMATION).textAttributes(MARKUP_ENTITY).create()
            }
            // strings
            if (element.elementType === STRING_TEMPLATE) {
                annotateSeparationMarks(element, holder, BRACES, (element.firstChild as LeafPsiElement).cachedLength)
            }
        } catch (e: Exception) {
            /* Should not happen */
        }
    }
}
