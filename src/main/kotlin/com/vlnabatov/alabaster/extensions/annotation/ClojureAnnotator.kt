package com.vlnabatov.alabaster.extensions.annotation

import com.vlnabatov.alabaster.annotateSeparationMarks
import clojure.lang.Keyword
import clojure.lang.PersistentHashMap
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity.TEXT_ATTRIBUTES
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.BRACES
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.FUNCTION_DECLARATION
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.MARKUP_ENTITY
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.ui.JBColor
import cursive.lexer.ClojureTokenTypes
import cursive.lexer.ClojureTokenTypes.*
import cursive.psi.ClojurePsiElement
import cursive.psi.ClojurePsiElement.*
import cursive.psi.api.ClListLike
import cursive.psi.impl.synthetic.SyntheticSymbol
import com.vlnabatov.alabaster.isBGTheme
import java.awt.Font


val macro: Keyword = Keyword.find("macro")
val ns: Keyword = Keyword.find("ns")

val clojureLangNSPatternRegex = Regex("clojure\\..*")

val variableMacroRegex = Regex("\\bdef\\b")
val functionMacrosRegex = Regex("\\bfn|defn|defn-|defmulti|defmethod|defmacro|deftest\\b")
val letSpecialFormRegex = Regex("\\blet|if-let|when-let\\b")
val letfnSpecialFormRegex = Regex("\\bletfn\\b")
val polymorphicMacroRegex =
    Regex("\\bdefinterface|defprotocol|deftype|extend-type|extend-protocol|reify\\b")
val compiledPolymorphicMacroRegex = Regex("\\bgen-class|gen-interface\\b")
val methodsKeywordRegex = Regex("\\B:methods\\b")
val keywordSpecialCharactersRegex = Regex(":_/|[:/.]")

// Technically BACKQUOTE is a form prefix punctuation mark, but we'll treat it as symbol prefix one to make it pleasing
val symbolPrefixPunctuationMarks = setOf(AT, BACKQUOTE, TILDA, TILDAAT, QUOTE)

// Technically UP is a symbol prefix punctuation mark, but we'll treat it as form prefix one to make it pleasing
val formPrefixPunctuationMarks = setOf(
    LEFT_CURLY,
    LEFT_PAREN,
    LEFT_SQUARE,
    RIGHT_CURLY,
    RIGHT_PAREN,
    RIGHT_SQUARE,
    ClojureTokenTypes.SHARP,
    SHARP_COLON,
    SHARP_CURLY,
    SHARP_DOUBLE_COLON,
    SHARP_QUESTION,
    SHARP_QUESTION_AT,
    SHARP_QUOTE,
    SHARPUP,
    UP
)

class ClojureAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element !is LeafPsiElement) return

        try {
            when (element.elementType) {
                LEFT_PAREN, RIGHT_PAREN -> {
                    // highlight outer braces of the top forms in black in Alabaster BG theme
                    if (isBGTheme() && element.parent.parent is PsiFile) {
                        holder
                            .newSilentAnnotation(TEXT_ATTRIBUTES)
                            .enforcedTextAttributes(TextAttributes(JBColor.BLACK, null, null, null, Font.PLAIN))
                            .create()
                    }
                }

                in ALL_LITERALS -> holder.newSilentAnnotation(TEXT_ATTRIBUTES).textAttributes(MARKUP_ENTITY).create()
                // highlight keywords' special characters differently
                KEYWORD_TOKEN -> keywordSpecialCharactersRegex.findAll(element.text).forEach { f ->
                    holder
                        .newSilentAnnotation(TEXT_ATTRIBUTES)
                        .range(TextRange.from(element.textOffset + f.range.first, f.value.length))
                        .enforcedTextAttributes(TextAttributes(EditorColorsManager.getInstance().schemeForCurrentUITheme.defaultForeground, null, null, null, Font.PLAIN))
                        .create()
                }

                SYMBOL_TOKEN -> {
                    if ((element.parent.parent as ClojurePsiElement).type === NAMESPACED_MAP) {
                        holder.newSilentAnnotation(TEXT_ATTRIBUTES).textAttributes(MARKUP_ENTITY).create()
                    }

                    if (isFunctionDeclaration(element.parent as ClojurePsiElement)) {
                        holder.newSilentAnnotation(TEXT_ATTRIBUTES).textAttributes(FUNCTION_DECLARATION).create()
                    }
                    // non-STD macros' calls
                    if (isMacroCall(element.parent as ClojurePsiElement) && !isClojureLangNSCall(element.parent as ClojurePsiElement)) {
                        holder
                            .newSilentAnnotation(TEXT_ATTRIBUTES)
                            .enforcedTextAttributes(TextAttributes(null, null, null, null, Font.ITALIC))
                            .create()
                    }
                }
                // highlight decorators for metadata, namespaced map, regex differently
                in SEPARATORS, in formPrefixPunctuationMarks ->
                    holder.newSilentAnnotation(TEXT_ATTRIBUTES).textAttributes(BRACES).create()

                in symbolPrefixPunctuationMarks ->
                    if (isBGTheme()) holder.newSilentAnnotation(TEXT_ATTRIBUTES).textAttributes(BRACES).create()
                // highlight quotation marks differently
                STRING_LITERAL -> {
                    annotateSeparationMarks(element, holder)
                }
            }
        } catch (e: Exception) { /* Should not happen */ }
    }

    private fun isMacroCall(element: ClojurePsiElement): Boolean =
        ((element.reference?.resolve() as SyntheticSymbol).deref() as PersistentHashMap)[macro] !=
                null

    private fun isClojureLangNSCall(element: ClojurePsiElement): Boolean =
        (((element.reference?.resolve() as SyntheticSymbol).deref() as PersistentHashMap)[ns]
                as String)
            .matches(clojureLangNSPatternRegex)

    private fun isFunctionDeclaration(element: ClojurePsiElement) =
        element.parent is ClListLike &&
                (isBasicFunctionDeclaration(element) ||
                        isPolymorphicFunctionDeclaration(element) ||
                        isCompiledPolymorphicFunctionDeclaration(element) ||
                        isVariableDeclarationWithFunctionAssignment(element) ||
                        isLetMacroBindingToAFunction(element) ||
                        isLetFnMacroBindingToAFunction(element))

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
                            as ClojurePsiElement
                ))

    private fun isLetFnMacroBindingToAFunction(element: ClojurePsiElement) =
        (element.parent.parent.parent.children[0].text.matches(letfnSpecialFormRegex) &&
                (element.parent.parent as ClListLike).type == VECTOR &&
                (element.parent as ClListLike).type == LIST &&
                element.parent.children.firstOrNull { (it as ClojurePsiElement).type == SYMBOL } ===
                element)

    private fun isFunctionExpression(element: ClojurePsiElement) =
        (element.type == ClojurePsiElement.SHARP && (element.children[0] as ClListLike).type == LIST) ||
                element.children[0].text.matches(functionMacrosRegex)
}
