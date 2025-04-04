(ns com.vlnabatov.alabaster.go-annotator
  (:require [com.vlnabatov.alabaster.util :refer [import-static print-methods]]
            [clojure.pprint :as pprint]
            [clojure.reflect :as reflect]
            [clojure.inspector :as insp])
  (:import (com.goide.psi GoTokenType)
           (com.intellij.lang.annotation AnnotationHolder)
           (com.intellij.lang.annotation HighlightSeverity)
           (com.intellij.openapi.editor DefaultLanguageHighlighterColors)
           (com.goide GoParserDefinition$Lazy)
           (com.goide GoTypes)
           (com.intellij.psi PsiElement)
           (com.intellij.psi.tree IElementType)
           (com.vlnabatov.alabaster UtilKt))
  (:gen-class :implements [com.intellij.lang.annotation.Annotator]
              :name       com.vlnabatov.alabaster.extensions.annotation.GoAnnotator
              :prefix     "-"))



(defn -annotate
  [_ ^PsiElement element ^AnnotationHolder holder]
  (let [^IElementType elementType (.. element getNode getElementType)]
    (cond (.contains GoParserDefinition$Lazy/STRING_LITERALS elementType) (do (UtilKt/annotateSeparationMarks element
                                                                                                              holder))
          (and (= "nil" (.getText element)) (= elementType GoTypes/IDENTIFIER))
            (do (-> holder
                    (.newSilentAnnotation HighlightSeverity/INFORMATION)
                    (.textAttributes DefaultLanguageHighlighterColors/NUMBER)
                    (.create))))))
