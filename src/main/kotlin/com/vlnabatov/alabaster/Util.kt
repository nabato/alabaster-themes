package com.vlnabatov.alabaster

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.BRACES
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.STRING
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.refactoring.suggested.endOffset
import com.intellij.refactoring.suggested.startOffset
import com.intellij.ui.Gray
import java.awt.Color
import java.awt.Font

val transparentColor: Color = Gray.TRANSPARENT

const val theme = "Alabaster"
const val BGTheme = "$theme BG"
const val darkTheme = "$theme Dark"

fun isDefaultTheme() = EditorColorsManager.getInstance().schemeForCurrentUITheme.name.endsWith(theme)
fun isBGTheme() = EditorColorsManager.getInstance().schemeForCurrentUITheme.name.endsWith(BGTheme)
fun isDarkTheme() = EditorColorsManager.getInstance().schemeForCurrentUITheme.name.endsWith(darkTheme)

fun isAlabasterTheme() = isDefaultTheme() || isBGTheme() || isDarkTheme()


fun annotateSeparationMarks(element: PsiElement, holder: AnnotationHolder, textAttributesKey: TextAttributesKey = BRACES, numberOfQuotationMarks: Int = 1) {
    annotateSeparationMarks(element, holder, textAttributesKey, numberOfQuotationMarks, numberOfQuotationMarks)
}

fun annotateSeparationMarks(element: PsiElement, holder: AnnotationHolder, textAttributesKey: TextAttributesKey = BRACES, numberOfOpeningQuotationMarks: Int = 1, numberOfClosingQuotationMarks: Int = 1) {
    // Standard annotations for non-Alabaster editor themes
    holder
        .newSilentAnnotation(HighlightSeverity.INFORMATION)
        .range(TextRange(element.startOffset, element.startOffset + numberOfOpeningQuotationMarks))
        .textAttributes(textAttributesKey)
        .create()

    holder
        .newSilentAnnotation(HighlightSeverity.INFORMATION)
        .range(TextRange(element.endOffset - numberOfClosingQuotationMarks, element.endOffset))
        .textAttributes(textAttributesKey)
        .create()


    if (isAlabasterTheme()) {
        val foreground = EditorColorsManager.getInstance().schemeForCurrentUITheme.defaultForeground
        val background = transparentColor
        // Differently colored quotation marks for Alabaster themes
        holder
            .newSilentAnnotation(HighlightSeverity.INFORMATION)
            .range(TextRange(element.startOffset, element.startOffset + numberOfOpeningQuotationMarks))
            .enforcedTextAttributes(TextAttributes(foreground, background, null, null, Font.PLAIN))
            .create()

        holder
            .newSilentAnnotation(HighlightSeverity.INFORMATION)
            .range(TextRange(element.endOffset - numberOfClosingQuotationMarks, element.endOffset))
            .enforcedTextAttributes(TextAttributes(foreground, background, null, null, Font.PLAIN))
            .create()

        // Ensure string's background doesn't change when it's highlighted by the IDE or/and its plugins.
        if (isBGTheme()) {
            holder
                .newSilentAnnotation(HighlightSeverity.TEXT_ATTRIBUTES)
                .range(TextRange(element.startOffset + numberOfOpeningQuotationMarks, element.endOffset - numberOfClosingQuotationMarks))
                .enforcedTextAttributes(TextAttributes(foreground, EditorColorsManager.getInstance().schemeForCurrentUITheme.getAttributes(STRING).backgroundColor, null, null, Font.PLAIN))
                .create()
        }
    }
}