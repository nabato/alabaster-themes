(ns com.vlnabatov.alabaster.java-annotator
  (:require [com.vlnabatov.alabaster.util :refer [import-static]]
            [clojure.pprint :as pprint])
  (:import (com.intellij.lang.annotation AnnotationHolder)
           (com.intellij.lang.annotation HighlightSeverity)
           (com.intellij.openapi.editor DefaultLanguageHighlighterColors)
           (com.intellij.psi.tree IElementType)
           (com.vlnabatov.alabaster UtilKt)
           (com.intellij.psi JavaTokenType)
           (com.intellij.psi PsiElement))
  (:gen-class :implements [com.intellij.lang.annotation.Annotator]
              :name       com.vlnabatov.alabaster.extensions.annotation.JavaAnnotator
              :prefix     "-"))


(defn -annotate
  [_ ^PsiElement element ^AnnotationHolder holder]
  (let [^IElementType elementType (.. element getNode getElementType)]
    (cond (#{JavaTokenType/TRUE_KEYWORD JavaTokenType/FALSE_KEYWORD JavaTokenType/NULL_KEYWORD} elementType)
            (do (-> holder
                    (.newSilentAnnotation HighlightSeverity/INFORMATION)
                    (.textAttributes DefaultLanguageHighlighterColors/NUMBER)
                    (.create)))
          (= JavaTokenType/STRING_LITERAL elementType) (do (UtilKt/annotateSeparationMarks element holder))
          (= JavaTokenType/TEXT_BLOCK_LITERAL elementType)
            (do (UtilKt/annotateSeparationMarks element holder DefaultLanguageHighlighterColors/BRACES 3)))))