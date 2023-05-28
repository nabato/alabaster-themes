import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.*
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.psiUtil.endOffset
import org.jetbrains.kotlin.psi.psiUtil.startOffset
import java.awt.Font

const val theme = "Alabaster"
const val BGTheme = "$theme BG"
const val DarkTheme = "$theme Dark"

fun isDefaultTheme() = EditorColorsManager.getInstance().schemeForCurrentUITheme.name.endsWith(theme)
fun isBGTheme() = EditorColorsManager.getInstance().schemeForCurrentUITheme.name.endsWith(BGTheme)
fun isDarkTheme() = EditorColorsManager.getInstance().schemeForCurrentUITheme.name.endsWith(DarkTheme)


fun annotateSeparationMarks(element: PsiElement, holder: AnnotationHolder, textAttributesKey: TextAttributesKey = BRACES, numberOfQuotationMarks: Int = 1) {
    annotateSeparationMarks(element, holder, textAttributesKey, numberOfQuotationMarks, numberOfQuotationMarks)
}

fun annotateSeparationMarks(element: PsiElement, holder: AnnotationHolder, textAttributesKey: TextAttributesKey = BRACES, numberOfOpeningQuotationMarks: Int = 1, numberOfClosingQuotationMarks: Int = 1) {
    holder
        .newSilentAnnotation(HighlightSeverity.TEXT_ATTRIBUTES)
        .range(TextRange(element.startOffset, element.startOffset + numberOfOpeningQuotationMarks))
        .textAttributes(BRACES)
        .create()

    holder
        .newSilentAnnotation(HighlightSeverity.TEXT_ATTRIBUTES)
        .range(TextRange(element.endOffset - numberOfClosingQuotationMarks, element.endOffset))
        .textAttributes(BRACES)
        .create()

    if (isBGTheme()) {
        holder
            .newSilentAnnotation(HighlightSeverity.TEXT_ATTRIBUTES)
            .range(TextRange(element.startOffset, element.startOffset + numberOfOpeningQuotationMarks))
            .enforcedTextAttributes(TextAttributes(null, EditorColorsManager.getInstance().schemeForCurrentUITheme.defaultBackground, null, null, Font.PLAIN))
            .textAttributes(BRACES)
            .create()

        holder
            .newSilentAnnotation(HighlightSeverity.TEXT_ATTRIBUTES)
            .range(TextRange(element.endOffset - numberOfClosingQuotationMarks, element.endOffset))
            .enforcedTextAttributes(TextAttributes(null, EditorColorsManager.getInstance().schemeForCurrentUITheme.defaultBackground, null, null, Font.PLAIN))
            .create()
    }
}