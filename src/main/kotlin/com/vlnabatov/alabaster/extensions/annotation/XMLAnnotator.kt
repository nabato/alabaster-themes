package com.vlnabatov.alabaster.extensions.annotation

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import com.intellij.psi.xml.XmlTokenType.*
val delimiters = setOf(XML_EQ, XML_ATTRIBUTE_VALUE_START_DELIMITER, XML_ATTRIBUTE_VALUE_END_DELIMITER)

class XMLAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        try {
            // delimiters
            if (element.elementType in delimiters) {
                holder
                    .newSilentAnnotation(HighlightSeverity.TEXT_ATTRIBUTES)
                    .textAttributes(DefaultLanguageHighlighterColors.BRACES)
                    .create()
            }
        } catch (e: Exception) {
            /* Should not happen */
        }
    }
}