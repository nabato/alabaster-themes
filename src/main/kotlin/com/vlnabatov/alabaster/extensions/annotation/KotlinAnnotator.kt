package com.vlnabatov.alabaster.extensions.annotation

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity.INFORMATION
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.BRACES
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.NUMBER
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.util.elementType
import com.vlnabatov.alabaster.annotateSeparationMarks
import org.jetbrains.kotlin.psi.stubs.elements.KtStubElementTypes.*


class KotlinAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        try {
            when (element.elementType) {
                NULL, BOOLEAN_CONSTANT, FLOAT_CONSTANT, CHARACTER_CONSTANT, INTEGER_CONSTANT ->
                    holder.newSilentAnnotation(INFORMATION).textAttributes(NUMBER).create()

                STRING_TEMPLATE ->
                    annotateSeparationMarks(
                        element,
                        holder,
                        BRACES,
                        (element.firstChild as LeafPsiElement).cachedLength
                    )
            }
        } catch (e: Exception) {
            /* Should not happen */
        }
    }
}
