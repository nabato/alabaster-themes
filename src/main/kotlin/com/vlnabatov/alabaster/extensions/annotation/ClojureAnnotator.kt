package com.vlnabatov.alabaster.extensions.annotation

import clojure.lang.Keyword
import clojure.lang.PersistentHashMap
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity.TEXT_ATTRIBUTES
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.BRACES
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.FUNCTION_DECLARATION
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.NUMBER
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.ui.JBColor
import com.vlnabatov.alabaster.annotateSeparationMarks
import com.vlnabatov.alabaster.isBGTheme
import cursive.lexer.ClojureTokenTypes.*
import cursive.lexer.ClojureTokenTypes.DEREF
import cursive.lexer.ClojureTokenTypes.NAMESPACED_MAP
import cursive.lexer.ClojureTokenTypes.SHARP
import cursive.lexer.ClojureTokenTypes.UNQUOTE
import cursive.lexer.ClojureTokenTypes.UNQUOTE_SPLICING
import cursive.psi.ClojurePsiElement
import cursive.psi.ClojurePsiElement.KEYWORD
import cursive.psi.ClojurePsiElement.LIST
import cursive.psi.ClojurePsiElement.SYMBOL
import cursive.psi.ClojurePsiElement.VECTOR
import cursive.psi.api.ClListLike
import cursive.psi.impl.synthetic.SyntheticSymbol
import java.awt.Font


val macro = Keyword.find("macro")
val ns = Keyword.find("ns")

val functionMacrosRegex = Regex("\\bfn|defn|defn-|defmulti|defmethod|defmacro|deftest\\b")

//val SEPARATORS = TokenSet.create(*arrayOf<IElementType>(NS_SEP))


fun isClojureLangNSCall(e: ClojurePsiElement) =
  (((e.reference?.resolve() as SyntheticSymbol)
    .deref() as PersistentHashMap)[ns] as String).matches(Regex("clojure\\..*"))


fun isMacroCall(e: ClojurePsiElement) =
  ((e.reference?.resolve() as SyntheticSymbol).deref() as PersistentHashMap)[macro] != null


fun isFunctionExpression(e: ClojurePsiElement) =
  (e.type == ClojurePsiElement.SHARP && (e.children[0] as ClListLike).type == LIST) ||
      e.children[0].text.matches(functionMacrosRegex)


fun isBasicFunctionDeclaration(e: ClojurePsiElement) =
  (((e.parent as ClListLike).children[0].text.matches(functionMacrosRegex)) &&
      (e.parent as ClListLike).type == LIST &&
      e.parent.children.firstOrNull { (it as ClojurePsiElement).type == SYMBOL && it !== e.parent.children[0] } === e)


fun isFunctionDeclaration(e: ClojurePsiElement) =
  e.parent is ClListLike &&
      (isBasicFunctionDeclaration(e) ||
          isPolymorphicFunctionDeclaration(e) ||
          isCompiledPolymorphicFunctionDeclaration(e) ||
          isVariableDeclarationWithFunctionAssignment(e) ||
          isLetMacroBindingToAFunction(e) ||
          isLetFnMacroBindingToAFunction(e))


fun isPolymorphicFunctionDeclaration(e: ClojurePsiElement) =
  (e.parent.parent.children[0].text
    .matches(Regex("\\bdefinterface|defprotocol|deftype|extend-type|extend-protocol|reify\\b")) &&
      (e.parent as ClListLike).type == LIST &&
      e.parent.children.firstOrNull { (it as ClojurePsiElement).type == SYMBOL } === e)


fun isCompiledPolymorphicFunctionDeclaration(e: ClojurePsiElement) =
  (e.parent.parent.parent.children[0].text.matches(Regex("\\bgen-class|gen-interface\\b")) &&
      (e.parent.parent.parent.children[
        e.parent.parent.parent.children.indexOfFirst {
          ((it as ClojurePsiElement).type == KEYWORD) &&
              it.text.matches(Regex("\\B:methods\\b"))
        } + 1] === e.parent.parent) &&
      e.parent.children.firstOrNull { (it as ClojurePsiElement).type == SYMBOL } === e)


fun isVariableDeclarationWithFunctionAssignment(e: ClojurePsiElement) =
  (e.parent.children[0].text.matches(Regex("\\bdef\\b")) &&
      (isFunctionExpression(e.parent.children.last() as ClojurePsiElement)) &&
      e.parent.children.firstOrNull { it !== e.parent.children[0] && (it as ClojurePsiElement).type == SYMBOL } === e)


fun isLetMacroBindingToAFunction(e: ClojurePsiElement) =
  ((e.parent as ClListLike).type == VECTOR &&
      e.type == SYMBOL &&
      e.parent.parent.children[0].text.matches(Regex("\\blet|if-let|when-let\\b")) &&
      isFunctionExpression(e.parent.children[e.parent.children.indexOf(e) + 1] as ClojurePsiElement))


fun isLetFnMacroBindingToAFunction(e: ClojurePsiElement) =
  (e.parent.parent.parent.children[0].text.matches(Regex("\\bletfn\\b")) &&
      (e.parent.parent as ClListLike).type == VECTOR &&
      (e.parent as ClListLike).type == LIST &&
      e.parent.children.firstOrNull { (it as ClojurePsiElement).type == SYMBOL } === e)


class ClojureAnnotator : Annotator {
  override fun annotate(element: PsiElement, holder: AnnotationHolder) {
    if (element !is LeafPsiElement) return

    try {
      when (element.elementType) {
        // Highlight outer braces of the top forms in black in Alabaster BG theme
        in setOf(LEFT_PAREN, RIGHT_PAREN) if (isBGTheme() && element.parent.parent is PsiFile) -> {
          holder
            .newSilentAnnotation(TEXT_ATTRIBUTES)
            .enforcedTextAttributes(TextAttributes(JBColor.BLACK, null, null, null, Font.PLAIN))
            .create()
        }

        // Technically BACKQUOTE is a form prefix punctuation mark, but we'll treat it as symbol prefix because that way it looks better
        in setOf(DEREF, SYNTAX_QUOTE, UNQUOTE, UNQUOTE_SPLICING, QUOTE) if (isBGTheme()) ->
          holder.newSilentAnnotation(TEXT_ATTRIBUTES).textAttributes(BRACES).create()

        // Highlight quotation marks differently
        STRING_LITERAL -> annotateSeparationMarks(element, holder)

        // constants
        in ALL_LITERALS -> holder.newSilentAnnotation(TEXT_ATTRIBUTES).textAttributes(NUMBER).create()

        // Highlight decorators for metadata, namespaced map, regex differently.
        // Technically UP is a symbol prefix punctuation mark, but we'll treat it as form prefix because that way it looks better.
        LEFT_CURLY,
        LEFT_PAREN,
        LEFT_SQUARE,
        RIGHT_CURLY,
        RIGHT_PAREN,
        RIGHT_SQUARE,
        SHARP,
        NAMESPACED_MAP,
        NS_SEP,
        SET_BRACE,
        AUTO_RESOLVED_MAP,
        CONDITIONAL,
        CONDITIONAL_LIST,
        VAR,
        META_OLD,
        META ->
          holder.newSilentAnnotation(TEXT_ATTRIBUTES).textAttributes(BRACES).create()

        // Highlight keywords' special characters differently
        KEYWORD_TOKEN -> Regex(":_/|[:/.]").findAll(element.text).forEach { f ->
          holder
            .newSilentAnnotation(TEXT_ATTRIBUTES)
            .range(TextRange.from(element.textOffset + f.range.first, f.value.length))
            .enforcedTextAttributes(
              TextAttributes(
                EditorColorsManager.getInstance().schemeForCurrentUITheme.defaultForeground,
                null,
                null,
                null,
                Font.PLAIN
              )
            )
            .create()
        }

        SYMBOL_TOKEN -> {
          when {
            (element.parent.parent as ClojurePsiElement).type === ClojurePsiElement.NAMESPACED_MAP ->
              holder.newSilentAnnotation(TEXT_ATTRIBUTES).textAttributes(NUMBER).create()

            isFunctionDeclaration(element.parent as ClojurePsiElement) ->
              holder.newSilentAnnotation(TEXT_ATTRIBUTES).textAttributes(FUNCTION_DECLARATION).create()

            // non-STD macros' calls
            isMacroCall(element.parent as ClojurePsiElement) && !isClojureLangNSCall(element.parent as ClojurePsiElement) ->
              holder
                .newSilentAnnotation(TEXT_ATTRIBUTES)
                .enforcedTextAttributes(TextAttributes(null, null, null, null, Font.ITALIC))
                .create()
          }
        }
      }
    } catch (e: Exception) { /* Should not happen */
    }
  }
}