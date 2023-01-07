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

val functionMacrosRegex = Regex("\\bfn|defn|defn-|defmulti|defmethod|defmacro\\b")

val variableMacroRegex = Regex("\\bdef\\b")

var specialFormRegex = Regex("\\blet|if-let|when-let\\b")
var letfnSpecialFormRegex = Regex("\\bletfn\\b")

val polymorphMacrosRegex =
    Regex("\\bdefinterface|defprotocol|deftype|extend-type|extend-protocol|reify\\b")

val compiledPolymorphMacrosRegex = Regex("\\bgen-class|gen-interface\\b")

val methodsKeywordRegex = Regex("\\B:methods\\b")

val namespacedKeywordSpecialCharactersRegex = Regex("[:/.]")

class ClojureAnnotator : Annotator {

  override fun annotate(element: PsiElement, holder: AnnotationHolder) {
    if (element !is ClojurePsiElement) return

    try {
      when {
        element.type == KEYWORD -> {
          val found = namespacedKeywordSpecialCharactersRegex.findAll(element.text)
          found.forEach { f ->
            holder
                .newSilentAnnotation(INFORMATION)
                .range(
                    TextRange.from(
                        element.textOffset + f.range.first, f.range.last - f.range.first))
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
          // Basic function declaration
          if ((((element.parent as ClListLike).children[0].text.matches(functionMacrosRegex)) &&
              (element.parent as ClListLike).type == LIST &&
              element.parent.children.firstOrNull {
                (it as ClojurePsiElement).type == SYMBOL && it !== element.parent.children[0]
              } === element) ||
              // Polymorph function declaration
              (element.parent.parent.children[0].text.matches(polymorphMacrosRegex) &&
                  (element.parent as ClListLike).type == LIST &&
                  element.parent.children.firstOrNull {
                    (it as ClojurePsiElement).type == SYMBOL
                  } === element) ||
              // Compiled polymorph function declaration
              (element.parent.parent.parent.children[0]
                  .text
                  .matches(compiledPolymorphMacrosRegex) &&
                  (element.parent.parent.parent.children[
                          element.parent.parent.parent.children.indexOfFirst {
                            ((it as ClojurePsiElement).type == KEYWORD) &&
                                it.text.matches(methodsKeywordRegex)
                          } + 1] === element.parent.parent) &&
                  element.parent.children.firstOrNull {
                    (it as ClojurePsiElement).type == SYMBOL
                  } === element) ||
              // Variable declaration with function value
              (element.parent.children[0].text.matches(variableMacroRegex) &&
                  (isFunctionExpression(element.parent.children.last())) &&
                  element.parent.children.firstOrNull {
                    it !== element.parent.children[0] && (it as ClojurePsiElement).type == SYMBOL
                  } === element) ||
              // 'let' macro binding symbol to a function
              ((element.parent as ClListLike).type == VECTOR &&
                  element.type == SYMBOL &&
                  element.parent.parent.children[0].text.matches(specialFormRegex) &&
                  isFunctionExpression(
                      element.parent.children[element.parent.children.indexOf(element) + 1])) ||
              // 'Let' macro binding symbol to a function (we need to go deeper)
              (element.parent.parent.parent.children[0].text.matches(letfnSpecialFormRegex) &&
                  (element.parent.parent as ClListLike).type == VECTOR &&
                  (element.parent as ClListLike).type == LIST &&
                  element.parent.children.firstOrNull {
                    (it as ClojurePsiElement).type == SYMBOL
                  } === element)) {
            holder.newSilentAnnotation(INFORMATION).textAttributes(FUNCTION_DECLARATION).create()
          }
        }
      }
    } catch (e: Exception) {
      /* Should not happen */
    }
  }

  private fun isFunctionExpression(element: PsiElement) =
      ((element as ClojurePsiElement).type == SHARP &&
          (element.children[0] as ClListLike).type == LIST) ||
          element.children[0].text.matches(functionMacrosRegex)
}
