package com.vlnabatov.alabaster.extensions.annotation

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity.TEXT_ATTRIBUTES
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.BRACES
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import com.intellij.psi.xml.XmlTokenType.*

class XMLAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        try {
            // delimiters
            if (element.elementType in setOf(
                    XML_EQ,
                    XML_ATTRIBUTE_VALUE_START_DELIMITER,
                    XML_ATTRIBUTE_VALUE_END_DELIMITER
                )
            ) {
                holder.newSilentAnnotation(TEXT_ATTRIBUTES).textAttributes(BRACES).create()
            }
        } catch (e: Exception) {
            /* Should not happen */
        }
    }
}