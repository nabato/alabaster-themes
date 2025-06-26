(ns com.vlnabatov.alabaster.kotlin-annotator
  (:import (com.intellij.lang.annotation AnnotationHolder)
           (com.intellij.lang.annotation HighlightSeverity)
           (com.intellij.openapi.editor DefaultLanguageHighlighterColors)
           (com.intellij.psi PsiElement)
           (com.intellij.psi.tree IElementType)
           (org.jetbrains.kotlin.psi.stubs.elements KtStubElementTypes)
           (com.vlnabatov.alabaster UtilKt))
  (:gen-class :implements [com.intellij.lang.annotation.Annotator]
              :name       com.vlnabatov.alabaster.extensions.annotation.KotlinAnnotator
              :prefix     "-"))


(defn -isDumbAware [_this] false)

(defn -annotate
  [_ ^PsiElement element ^AnnotationHolder holder]
  (let [^IElementType elementType (.. element getNode getElementType)]
    (cond (= KtStubElementTypes/STRING_TEMPLATE elementType) (do (UtilKt/annotateSeparationMarks element holder))
          (contains? #{KtStubElementTypes/NULL KtStubElementTypes/BOOLEAN_CONSTANT KtStubElementTypes/FLOAT_CONSTANT
                       KtStubElementTypes/CHARACTER_CONSTANT KtStubElementTypes/INTEGER_CONSTANT}
                     elementType)
            (do (-> holder
                    (.newSilentAnnotation HighlightSeverity/INFORMATION)
                    (.textAttributes DefaultLanguageHighlighterColors/NUMBER)
                    (.create))))))