package com.vlnabatov.alabaster.extensions.annotation

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity.INFORMATION
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.MARKUP_ENTITY
import com.intellij.psi.JavaTokenType.*
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType

private val valTokens = setOf(TRUE_KEYWORD, FALSE_KEYWORD, NULL_KEYWORD)

class JavaAnnotator : Annotator {
  override fun annotate(element: PsiElement, holder: AnnotationHolder) {
    if (element.elementType in valTokens) {
      holder.newSilentAnnotation(INFORMATION).textAttributes(MARKUP_ENTITY).create()
    }
  }
}
