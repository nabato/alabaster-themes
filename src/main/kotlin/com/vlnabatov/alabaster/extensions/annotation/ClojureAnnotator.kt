package com.vlnabatov.alabaster.extensions.annotation

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import cursive.psi.api.symbols.ClSymbol


class ClojureAnnotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        try {
            if (isFunctionDeclaration(element)) {
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                    .range(TextRange.from(element.textRange.startOffset, element.text.length))
                    .textAttributes(DefaultLanguageHighlighterColors.FUNCTION_DECLARATION)
                    .create()
            }
        } catch (e: Exception) {
            /**/
        }
    }

    private fun isFunctionDeclaration(element: PsiElement) =
        element is ClSymbol &&
                element === element.parent.children[1] &&
                element.parent.children.first().text in arrayOf("fn", "defn", "defn-")
}
