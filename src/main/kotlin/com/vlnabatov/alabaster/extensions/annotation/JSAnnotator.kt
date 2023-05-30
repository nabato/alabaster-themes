package com.vlnabatov.alabaster.extensions.annotation

import com.vlnabatov.alabaster.annotateSeparationMarks
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity.INFORMATION
import com.intellij.lang.ecmascript6.ES6StubElementTypes.*
import com.intellij.lang.javascript.JSElementTypes.*
import com.intellij.lang.javascript.JSStubElementTypes.DESTRUCTURING_PROPERTIES
import com.intellij.lang.javascript.JSTokenTypes.*
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.FUNCTION_DECLARATION
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.MARKUP_ENTITY
import com.intellij.openapi.editor.HighlighterColors.TEXT
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.util.elementType

val identifierTypes = setOf(IDENTIFIER, PRIVATE_IDENTIFIER)

val symbolTypes = setOf(TRUE_KEYWORD, FALSE_KEYWORD, NULL_KEYWORD, UNDEFINED_KEYWORD)

val stringTypes = setOf(STRING_LITERAL, SINGLE_QUOTE_STRING_LITERAL)

val symbolRegex =
    Regex(
        "\\bInfinity|NaN|POSITIVE_INFINITY|NEGATIVE_INFINITY|MAX_VALUE|MIN_VALUE|EPSILON|MAX_SAFE_INTEGER|MIN_SAFE_INTEGER|E|LN2|LN10|LOG2E|LOG10E|PI|SQRT1_2|SQRT2|\\b"
    )

val mathObjectRegex = Regex("\\bMath|Number\\b")

val assignmentExpressionTypes =
    setOf(ASSIGNMENT_EXPRESSION, BODY_VARIABLES, ES6_CLASS_FIELDS, PROPERTY, FIELD, FIELD_STATEMENT)

val functionExpressionTypes =
    setOf(
        *FUNCTION_DECLARATIONS.types,
        *FUNCTION_EXPRESSIONS.types,
        *FUNCTION_PROPERTIES.types,
        *CLASS_EXPRESSIONS.types
    )

val functionConstructorRegex =
    Regex("\\bFunction|AsyncFunction|GeneratorFunction|AsyncGeneratorFunction\\b")

val requireRegex = Regex("\\brequire\\b")

class JSAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        try {
            if (element is LeafPsiElement) {
                if (isSymbolIdentifier(element)) {
                    holder.newSilentAnnotation(INFORMATION).textAttributes(MARKUP_ENTITY).create()
                } else if (element.elementType in identifierTypes) {
                    if (isFunctionIdentifier(element)) {
                        holder.newSilentAnnotation(INFORMATION).textAttributes(FUNCTION_DECLARATION).create()
                    } else if (element.parent.elementType == REFERENCE_EXPRESSION ||
                        element.parent.parent.elementType in DESTRUCTURING_PROPERTIES ||
                        element.parent.elementType == EXPORT_SPECIFIER ||
                        element.parent.elementType == IMPORT_SPECIFIER ||
                        element.parent.elementType == IMPORTED_BINDING ||
                        (element.parent.elementType == VARIABLE &&
                                element.parent.children[0].elementType == CALL_EXPRESSION &&
                                element.parent.children[0].children[0].text.matches(requireRegex))
                    ) {
                        holder.newSilentAnnotation(INFORMATION).textAttributes(TEXT).create()
                    }
                    // strings
                } else if (element.elementType in stringTypes ||
                    (element.elementType === BACKQUOTE &&
                            element.parent.elementType === STRING_TEMPLATE_EXPRESSION &&
                            (element === element.parent.firstChild || element === element.parent.lastChild))) {
                    annotateSeparationMarks(element, holder)
                }
            }
        } catch (e: Exception) {
            /* Should not happen */
        }
    }

    // Imprecise detection given current PSI info
    private fun isSymbolIdentifier(element: PsiElement): Boolean {
        if (element.elementType in symbolTypes) {
            return true
        }

        if (element.text.matches(symbolRegex) &&
            element.parent.elementType == REFERENCE_EXPRESSION &&
            (element.parent as JSReferenceExpressionImpl).referencedName!!.matches(symbolRegex)
        ) {
            return true
        }

        if (element.text.matches(symbolRegex) &&
            element.parent.elementType == DESTRUCTURING_PROPERTY &&
            element.parent.parent.parent.elementType == DESTRUCTURING_ELEMENT &&
            element.parent.parent.parent.children[1].text.matches(mathObjectRegex)
        ) {
            return true
        }

        return element.text.matches(symbolRegex) &&
                element.parent.parent.elementType == DESTRUCTURING_SHORTHANDED_PROPERTY &&
                element.parent.parent.parent.parent.elementType == DESTRUCTURING_ELEMENT &&
                element.parent.parent.parent.parent.children[1].text.matches(mathObjectRegex)
    }

    private fun isFunctionIdentifier(element: PsiElement): Boolean {
        if (element.parent.elementType in functionExpressionTypes) {
            return true
        }

        if (element.parent.elementType in assignmentExpressionTypes &&
            element.parent.children[0] !== element &&
            isFunctionExpression(element.parent.children[0])
        ) {
            return true
        }

        return element.parent.elementType == REFERENCE_EXPRESSION &&
                element.parent.parent.elementType == DEFINITION_EXPRESSION &&
                element.parent.parent.parent.elementType == ASSIGNMENT_EXPRESSION &&
                isFunctionExpression(element.parent.parent.parent.children[1])
    }

    private fun isFunctionExpression(element: PsiElement) =
        element.elementType in functionExpressionTypes ||
                (element.elementType == NEW_EXPRESSION &&
                        element.children[0].text.matches(functionConstructorRegex))
}
