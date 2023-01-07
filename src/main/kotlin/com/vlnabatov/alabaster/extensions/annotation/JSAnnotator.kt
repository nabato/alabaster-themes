package com.vlnabatov.alabaster.extensions.annotation

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

private val identifierTokens = setOf(IDENTIFIER, PRIVATE_IDENTIFIER)

private val valTokens = setOf(TRUE_KEYWORD, FALSE_KEYWORD, NULL_KEYWORD, UNDEFINED_KEYWORD)

private val valRegex =
    Regex(
        "\\bInfinity|NaN|POSITIVE_INFINITY|NEGATIVE_INFINITY|MAX_VALUE|MIN_VALUE|EPSILON|MAX_SAFE_INTEGER|MIN_SAFE_INTEGER|E|LN2|LN10|LOG2E|LOG10E|PI|SQRT1_2|SQRT2|\\b")

private val mathObjectRegex = Regex("\\bMath|Number\\b")

private val assignmentExpressionTokens =
    setOf(ASSIGNMENT_EXPRESSION, BODY_VARIABLES, ES6_CLASS_FIELDS, PROPERTY, FIELD, FIELD_STATEMENT)

private val functionExpressionTokens =
    setOf(
        *FUNCTION_DECLARATIONS.types,
        *FUNCTION_EXPRESSIONS.types,
        *FUNCTION_PROPERTIES.types,
        *CLASS_EXPRESSIONS.types)

private val functionConstructorRegex =
    Regex("\\bFunction|AsyncFunction|GeneratorFunction|AsyncGeneratorFunction\\b")

private val requireRegex = Regex("\\brequire\\b")

class JSAnnotator : Annotator {

  override fun annotate(element: PsiElement, holder: AnnotationHolder) {
    try {
      if (element is LeafPsiElement) {
        when {
          isSymbolIdentifier(element) -> {
            holder.newSilentAnnotation(INFORMATION).textAttributes(MARKUP_ENTITY).create()
          }
          element.elementType in identifierTokens -> {
            if (isFunctionIdentifier(element)) {
              holder.newSilentAnnotation(INFORMATION).textAttributes(FUNCTION_DECLARATION).create()
            } else if (element.parent.elementType == REFERENCE_EXPRESSION ||
                element.parent.parent.elementType in DESTRUCTURING_PROPERTIES ||
                element.parent.elementType == EXPORT_SPECIFIER ||
                element.parent.elementType == IMPORT_SPECIFIER ||
                element.parent.elementType == IMPORTED_BINDING ||
                (element.parent.elementType == VARIABLE &&
                    element.parent.children[0].elementType == CALL_EXPRESSION &&
                    element.parent.children[0].children[0].text.matches(requireRegex))) {
              holder.newSilentAnnotation(INFORMATION).textAttributes(TEXT).create()
            }
          }
        }
      }
    } catch (e: Exception) {
      /* Should not happen */
    }
  }

  // Imprecise detection given current PSI info
  private fun isSymbolIdentifier(element: PsiElement): Boolean {
    if (element.elementType in valTokens) {
      return true
    }

    if (element.text.matches(valRegex) &&
        element.parent.elementType == REFERENCE_EXPRESSION &&
        (element.parent as JSReferenceExpressionImpl).referencedName!!.matches(valRegex)) {
      return true
    }

    if (element.text.matches(valRegex) &&
        element.parent.elementType == DESTRUCTURING_PROPERTY &&
        element.parent.parent.parent.elementType == DESTRUCTURING_ELEMENT &&
        element.parent.parent.parent.children[1].text.matches(mathObjectRegex)) {
      return true
    }

    if (element.text.matches(valRegex) &&
        element.parent.parent.elementType == DESTRUCTURING_SHORTHANDED_PROPERTY &&
        element.parent.parent.parent.parent.elementType == DESTRUCTURING_ELEMENT &&
        element.parent.parent.parent.parent.children[1].text.matches(mathObjectRegex)) {
      return true
    }

    return false
  }

  private fun isFunctionIdentifier(element: PsiElement): Boolean {
    if (element.parent.elementType in functionExpressionTokens) {
      return true
    }

    if (element.parent.elementType in assignmentExpressionTokens &&
        element.parent.children[0] !== element &&
        isFunctionExpression(element.parent.children[0])) {

      return true
    }

    if (element.parent.elementType == REFERENCE_EXPRESSION &&
        element.parent.parent.elementType == DEFINITION_EXPRESSION &&
        element.parent.parent.parent.elementType == ASSIGNMENT_EXPRESSION &&
        isFunctionExpression(element.parent.parent.parent.children[1])) {

      return true
    }

    return false
  }

  private fun isFunctionExpression(element: PsiElement) =
      element.elementType in functionExpressionTokens ||
          (element.elementType == NEW_EXPRESSION &&
              element.children[0].text.matches(functionConstructorRegex))
}
