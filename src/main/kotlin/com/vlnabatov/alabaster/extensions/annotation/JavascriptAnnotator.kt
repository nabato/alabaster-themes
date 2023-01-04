package com.vlnabatov.alabasterDark.extensions.annotation

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.lang.ecmascript6.ES6StubElementTypes
import com.intellij.lang.javascript.JSElementTypes
import com.intellij.lang.javascript.JSStubElementTypes
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.util.elementType

val functionTypes = setOf("Function", "AsyncFunction", "GeneratorFunction", "AsyncGeneratorFunction")

val functionDeclarationTypes = setOf(
    *JSElementTypes.FUNCTION_DECLARATIONS.types,
    *JSElementTypes.FUNCTION_EXPRESSIONS.types,
    *JSElementTypes.CLASS_EXPRESSIONS.types)

val identifierTypes = setOf(JSTokenTypes.IDENTIFIER, JSTokenTypes.PRIVATE_IDENTIFIER)

val constantTypes = setOf(JSTokenTypes.TRUE_KEYWORD, JSTokenTypes.FALSE_KEYWORD, JSTokenTypes.NULL_KEYWORD)



class JSAnnotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        try {

            if (element is LeafPsiElement) {
                if (element.elementType in constantTypes) {
                    holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                        .textAttributes(DefaultLanguageHighlighterColors.CONSTANT)
                        .create()
                } else if (element.elementType in identifierTypes) {
                    if (
                        isFunctionDeclaration(element) ||
                        isFunctionAssignment(element) ||
                        isPropertyFunction(element) ||
                        isFunctionReference(element)
                    ) {
                        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                            .textAttributes(DefaultLanguageHighlighterColors.FUNCTION_DECLARATION)
                            .create()
                    } else if (
                        element.parent.elementType == JSElementTypes.REFERENCE_EXPRESSION &&
                        element.parent.elementType !in JSElementTypes.CLASS_EXPRESSIONS &&
                        !isFunctionReference(element)
                    ) {
                        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                            .textAttributes(HighlighterColors.TEXT)
                            .create()
                    }
                }
            }
        } catch (e: Exception) {
            /* Should not happen */
        }
    }

    private fun isFunctionDeclaration(element: PsiElement) =
        element.parent.elementType in functionDeclarationTypes

    private fun isFunctionAssignment(element: PsiElement) =
        element.parent.elementType == JSStubElementTypes.VARIABLE &&
                ((element.parent.children[0].elementType == JSStubElementTypes.NEW_EXPRESSION &&
                        element.parent.children[0].children[0].text in functionTypes) ||
                        (element.parent.children[0].elementType == ES6StubElementTypes.CLASS_EXPRESSION))

    private fun isPropertyFunction(element: PsiElement) =
        element.parent.elementType == JSStubElementTypes.FUNCTION_PROPERTY ||
                ((element.parent.elementType == JSStubElementTypes.PROPERTY ||
                        element.parent.elementType == ES6StubElementTypes.FIELD) &&
                        element.parent?.children?.get(0).elementType == JSStubElementTypes.FUNCTION_EXPRESSION)

    private fun isFunctionReference(element: PsiElement) =
        element.parent.elementType == JSElementTypes.REFERENCE_EXPRESSION &&
                element.parent.parent?.elementType == JSElementTypes.DEFINITION_EXPRESSION &&
                element.parent.parent.parent?.elementType == JSStubElementTypes.ASSIGNMENT_EXPRESSION &&
                element.parent.parent.parent.children[1].elementType == JSStubElementTypes.FUNCTION_EXPRESSION
}
