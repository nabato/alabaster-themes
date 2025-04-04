(ns com.vlnabatov.alabaster.scala-annotator
  (:require [com.vlnabatov.alabaster.util :refer [import-static]])
  (:import (com.intellij.lang.annotation AnnotationHolder)
           (com.intellij.lang.annotation HighlightSeverity)
           (com.intellij.openapi.editor DefaultLanguageHighlighterColors)
           (com.intellij.psi.tree IElementType)
           (com.vlnabatov.alabaster UtilKt)
           (org.jetbrains.plugins.scala.lang.lexer ScalaTokenTypes)
           (com.intellij.psi PsiElement))
  (:gen-class :implements [com.intellij.lang.annotation.Annotator]
              :name       com.vlnabatov.alabaster.extensions.annotation.ScalaAnnotator
              :prefix     "-"))


(defn -annotate
  [_ ^PsiElement element ^AnnotationHolder holder]
  (let [^IElementType elementType (.. element getNode getElementType)]
    (cond (.contains ScalaTokenTypes/STRING_LITERAL_TOKEN_SET elementType) (do (UtilKt/annotateSeparationMarks element
                                                                                                               holder))
          (or (.contains ScalaTokenTypes/BOOLEAN_TOKEN_SET elementType)
              (.contains ScalaTokenTypes/NUMBER_TOKEN_SET elementType)
              (= ScalaTokenTypes/kNULL elementType))
            (do (-> holder
                    (.newSilentAnnotation HighlightSeverity/INFORMATION)
                    (.textAttributes DefaultLanguageHighlighterColors/NUMBER)
                    (.create))))))