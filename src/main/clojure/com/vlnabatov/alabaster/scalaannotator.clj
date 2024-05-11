(ns com.vlnabatov.alabaster.scalaannotator
  (:require [com.vlnabatov.alabaster.util :refer [import-static]])
  (:import (com.intellij.lang.annotation AnnotationHolder)
           (com.intellij.lang.annotation HighlightSeverity)
           (com.intellij.openapi.editor DefaultLanguageHighlighterColors)
           (org.jetbrains.plugins.scala.lang.lexer ScalaTokenTypes)
           (com.intellij.psi PsiElement))
  (:gen-class :implements [com.intellij.lang.annotation.Annotator]
              :name com.vlnabatov.alabaster.extensions.annotation.CLJScalaAnnotator
              :prefix "-"))

;;(def val-tokens #{(.-types ScalaTokenTypes/BOOLEAN_TOKEN_SET)})

(defn -annotate
  [this ^PsiElement element ^AnnotationHolder holder]
  (let [elementType (.. element (getNode) (getElementType))
        is-string? (.contains ScalaTokenTypes/STRING_LITERAL_TOKEN_SET elementType)]
    (cond is-string? (-> holder
                         (.newSilentAnnotation HighlightSeverity/INFORMATION)
                         (.textAttributes DefaultLanguageHighlighterColors/MARKUP_ENTITY)
                         (.create))
          (.contains ScalaTokenTypes/STRING_LITERAL_TOKEN_SET elementType)
            (-> holder
                (.newSilentAnnotation HighlightSeverity/INFORMATION)
                (.textAttributes DefaultLanguageHighlighterColors/MARKUP_ENTITY)
                (.create)))))

