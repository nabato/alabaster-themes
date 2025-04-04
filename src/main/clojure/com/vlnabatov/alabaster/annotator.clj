(ns com.vlnabatov.alabaster.annotator
  (:require [com.vlnabatov.alabaster.util :refer [import-static]])
  (:import (com.intellij.lang.annotation AnnotationHolder)
           (com.intellij.lang.annotation HighlightSeverity)
           (com.intellij.openapi.editor DefaultLanguageHighlighterColors)
           (com.vlnabatov.alabaster UtilKt)
           (org.jetbrains.plugins.scala.lang.lexer ScalaTokenTypes)
           (com.intellij.psi PsiElement))
  (:gen-class :implements [com.intellij.lang.annotation.Annotator]
              :name com.vlnabatov.alabaster.extensions.annotation.Annotator
              :prefix "-"))


(def constants
  (set (concat (.getTypes ScalaTokenTypes/BOOLEAN_TOKEN_SET)
               (.getTypes ScalaTokenTypes/NUMBER_TOKEN_SET)
               [ScalaTokenTypes/kNULL])))

(def strings (set (.getTypes ScalaTokenTypes/STRING_LITERAL_TOKEN_SET)))


(defn -annotate
  [_ ^PsiElement element ^AnnotationHolder holder]
  (let [elementType (-> element
                        (.getNode)
                        (.getElementType))]
    (cond (contains? strings elementType) (UtilKt/annotateSeparationMarks element holder)
          (contains? constants elementType) (-> holder
                                                (.newSilentAnnotation HighlightSeverity/INFORMATION)
                                                (.textAttributes DefaultLanguageHighlighterColors/NUMBER)
                                                (.create)))))