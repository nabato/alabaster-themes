package com.vlnabatov.alabaster.extensions.annotation

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity.INFORMATION
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.FUNCTION_DECLARATION
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.MARKUP_ENTITY
import com.intellij.openapi.editor.HighlighterColors.TEXT
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import cursive.psi.ClojurePsiElement
import cursive.psi.ClojurePsiElement.*
import cursive.psi.api.ClListLike

val variableMacroRegex = Regex("\\bdef\\b")
val functionMacrosRegex = Regex("\\bfn|defn|defn-|defmulti|defmethod|defmacro|deftest\\b")
var letSpecialFormRegex = Regex("\\blet|if-let|when-let\\b")
var letfnSpecialFormRegex = Regex("\\bletfn\\b")
val polymorphicMacrosRegex =
    Regex("\\bdefinterface|defprotocol|deftype|extend-type|extend-protocol|reify\\b")
val compiledPolymorphicMacrosRegex = Regex("\\bgen-class|gen-interface\\b")
val methodsKeywordRegex = Regex("\\B:methods\\b")
val namespacedKeywordSpecialCharactersRegex = Regex("[:/.]")

class ClojureAnnotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element !is ClojurePsiElement) return

        try {
            when {
                element.type == KEYWORD -> {
                    namespacedKeywordSpecialCharactersRegex.findAll(element.text).forEach { f ->
                        holder
                            .newSilentAnnotation(INFORMATION)
                            .range(
                                TextRange.from(
                                    element.textOffset + f.range.first, f.range.last - f.range.first
                                )
                            )
                            .textAttributes(TEXT)
                            .create()
                    }
                }

                (element.parent as ClojurePsiElement).type == NAMESPACED_MAP -> {
                    if (element.type == SYMBOL && element.parent.children[0] === element) {
                        holder.newSilentAnnotation(INFORMATION).textAttributes(MARKUP_ENTITY).create()
                    }
                }

                element.parent is ClListLike -> {
                    if (isBasicFunctionDeclaration(element) ||
                        isPolymorphicFunctionDeclaration(element) ||
                        isCompiledPolymorphicFunctionDeclaration(element) ||
                        isVariableDeclarationWithFunctionAssignment(element) ||
                        isLetMacroBindingToAFunction(element) ||
                        isLetFnMacroBindingToAFunction(element)
                    ) {
                        holder.newSilentAnnotation(INFORMATION).textAttributes(FUNCTION_DECLARATION).create()
                    }
                }
            }
        } catch (e: Exception) {
            /* Should not happen */
        }
    }

    private fun isBasicFunctionDeclaration(element: ClojurePsiElement) =
        (((element.parent as ClListLike).children[0].text.matches(functionMacrosRegex)) &&
                (element.parent as ClListLike).type == LIST &&
                element.parent.children.firstOrNull {
                    (it as ClojurePsiElement).type == SYMBOL && it !== element.parent.children[0]
                } === element)

    private fun isPolymorphicFunctionDeclaration(element: ClojurePsiElement) =
        (element.parent.parent.children[0].text.matches(polymorphicMacrosRegex) &&
                (element.parent as ClListLike).type == LIST &&
                element.parent.children.firstOrNull {
                    (it as ClojurePsiElement).type == SYMBOL
                } === element)

    private fun isCompiledPolymorphicFunctionDeclaration(element: ClojurePsiElement) =
        (element.parent.parent.parent.children[0]
            .text
            .matches(compiledPolymorphicMacrosRegex) &&
                (element.parent.parent.parent.children[
                    element.parent.parent.parent.children.indexOfFirst {
                        ((it as ClojurePsiElement).type == KEYWORD) &&
                                it.text.matches(methodsKeywordRegex)
                    } + 1] === element.parent.parent) &&
                element.parent.children.firstOrNull {
                    (it as ClojurePsiElement).type == SYMBOL
                } === element)

    private fun isVariableDeclarationWithFunctionAssignment(element: ClojurePsiElement) =
        (element.parent.children[0].text.matches(variableMacroRegex) &&
                (isFunctionExpression(element.parent.children.last() as ClojurePsiElement)) &&
                element.parent.children.firstOrNull {
                    it !== element.parent.children[0] && (it as ClojurePsiElement).type == SYMBOL
                } === element)

    private fun isLetMacroBindingToAFunction(element: ClojurePsiElement) =
        ((element.parent as ClListLike).type == VECTOR &&
                element.type == SYMBOL &&
                element.parent.parent.children[0].text.matches(letSpecialFormRegex) &&
                isFunctionExpression(
                    element.parent.children[element.parent.children.indexOf(element) + 1] as ClojurePsiElement
                ))

    private fun isLetFnMacroBindingToAFunction(element: ClojurePsiElement) =
        (element.parent.parent.parent.children[0].text.matches(letfnSpecialFormRegex) &&
                (element.parent.parent as ClListLike).type == VECTOR &&
                (element.parent as ClListLike).type == LIST &&
                element.parent.children.firstOrNull {
                    (it as ClojurePsiElement).type == SYMBOL
                } === element)

    private fun isFunctionExpression(element: ClojurePsiElement) =
        (element.type == SHARP &&
                (element.children[0] as ClListLike).type == LIST) ||
                element.children[0].text.matches(functionMacrosRegex)
}
