package com.vlnabatov.alabaster.extensions.annotation

import clojure.lang.Keyword
import clojure.lang.PersistentHashMap
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity.TEXT_ATTRIBUTES
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.MARKUP_ENTITY
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.BRACES
import com.intellij.openapi.editor.HighlighterColors.TEXT
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.util.elementType
import cursive.psi.ClojurePsiElement
import cursive.psi.ClojurePsiElement.*
import cursive.psi.api.ClListLike
import cursive.psi.impl.synthetic.SyntheticSymbol
import java.awt.Font

val namespacedMapPrefixes = setOf("#::", "#:")

val macro: Keyword = Keyword.find("macro")
val ns: Keyword = Keyword.find("ns")

val clojureLangNSPatternRegex = Regex("clojure\\..*")

val variableMacroRegex = Regex("\\bdef\\b")
val functionMacrosRegex = Regex("\\bfn|defn|defn-|defmulti|defmethod|defmacro|deftest\\b")
var letSpecialFormRegex = Regex("\\blet|if-let|when-let\\b")
var letfnSpecialFormRegex = Regex("\\bletfn\\b")
val polymorphicMacroRegex =
    Regex("\\bdefinterface|defprotocol|deftype|extend-type|extend-protocol|reify\\b")
val compiledPolymorphicMacroRegex = Regex("\\bgen-class|gen-interface\\b")
val methodsKeywordRegex = Regex("\\B:methods\\b")
val namespacedKeywordSpecialCharactersRegex = Regex("[:/.]")

class ClojureAnnotator : Annotator {

  override fun annotate(element: PsiElement, holder: AnnotationHolder) {
    if (element !is ClojurePsiElement) return

    try {

        if ((element.parent as ClojurePsiElement).type == NAMESPACED_MAP) {
            if (element.type == SYMBOL && element.parent.children[0] === element) {
                holder.newSilentAnnotation(TEXT_ATTRIBUTES).textAttributes(MARKUP_ENTITY).create()
            }

            if (element.parent.firstChild === element) {
                println((element as PsiElement).text)
                println(element.elementType)
                holder.newSilentAnnotation(TEXT_ATTRIBUTES).textAttributes(BRACES).create()
            }

            return
        }

        if (element.type === KEYWORD || (element.parent as ClojurePsiElement).type === KEYWORD) {
            namespacedKeywordSpecialCharactersRegex.findAll(element.text).forEach { f ->
                holder
                    .newSilentAnnotation(TEXT_ATTRIBUTES)
                    .range(
                        TextRange.from(element.textOffset + f.range.first, f.range.last - f.range.first))
                    .textAttributes(BRACES)
                    .create()
            }
            return
        }
      if (element.parent is ClListLike) {
        if (isBasicFunctionDeclaration(element) ||
            isPolymorphicFunctionDeclaration(element) ||
            isCompiledPolymorphicFunctionDeclaration(element) ||
            isVariableDeclarationWithFunctionAssignment(element) ||
            isLetMacroBindingToAFunction(element) ||
            isLetFnMacroBindingToAFunction(element)) {
          holder
              .newSilentAnnotation(TEXT_ATTRIBUTES)
              .textAttributes(DefaultLanguageHighlighterColors.FUNCTION_DECLARATION)
              .create()
        }
      }

      if (isMacroCall(element) && !isClojureLangNSCall(element)) {
        holder
            .newSilentAnnotation(TEXT_ATTRIBUTES)
            .enforcedTextAttributes(TextAttributes(null, null, null, null, Font.ITALIC))
            .create()
      }
    } catch (e: Exception) {
      /* Should not happen */
    }
  }

  private fun isMacroCall(element: ClojurePsiElement): Boolean =
      ((element.reference?.resolve() as SyntheticSymbol).deref() as PersistentHashMap)[macro] !=
          null

  private fun isClojureLangNSCall(element: ClojurePsiElement): Boolean =
      (((element.reference?.resolve() as SyntheticSymbol).deref() as PersistentHashMap)[ns]
              as String)
          .matches(clojureLangNSPatternRegex)

  private fun isBasicFunctionDeclaration(element: ClojurePsiElement) =
      (((element.parent as ClListLike).children[0].text.matches(functionMacrosRegex)) &&
          (element.parent as ClListLike).type == LIST &&
          element.parent.children.firstOrNull {
            (it as ClojurePsiElement).type == SYMBOL && it !== element.parent.children[0]
          } === element)

  private fun isPolymorphicFunctionDeclaration(element: ClojurePsiElement) =
      (element.parent.parent.children[0].text.matches(polymorphicMacroRegex) &&
          (element.parent as ClListLike).type == LIST &&
          element.parent.children.firstOrNull { (it as ClojurePsiElement).type == SYMBOL } ===
              element)

  private fun isCompiledPolymorphicFunctionDeclaration(element: ClojurePsiElement) =
      (element.parent.parent.parent.children[0].text.matches(compiledPolymorphicMacroRegex) &&
          (element.parent.parent.parent.children[
                  element.parent.parent.parent.children.indexOfFirst {
                    ((it as ClojurePsiElement).type == KEYWORD) &&
                        it.text.matches(methodsKeywordRegex)
                  } + 1] === element.parent.parent) &&
          element.parent.children.firstOrNull { (it as ClojurePsiElement).type == SYMBOL } ===
              element)

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
              element.parent.children[element.parent.children.indexOf(element) + 1]
                  as ClojurePsiElement))

  private fun isLetFnMacroBindingToAFunction(element: ClojurePsiElement) =
      (element.parent.parent.parent.children[0].text.matches(letfnSpecialFormRegex) &&
          (element.parent.parent as ClListLike).type == VECTOR &&
          (element.parent as ClListLike).type == LIST &&
          element.parent.children.firstOrNull { (it as ClojurePsiElement).type == SYMBOL } ===
              element)

  private fun isFunctionExpression(element: ClojurePsiElement) =
      (element.type == SHARP && (element.children[0] as ClListLike).type == LIST) ||
          element.children[0].text.matches(functionMacrosRegex)
}
