(ns com.vlnabatov.alabaster.clojure-annotator
  (:require [com.vlnabatov.alabaster.util :refer [import-static]])
  (:import (com.intellij.lang.annotation AnnotationHolder)
           (com.intellij.lang.annotation HighlightSeverity)
           (com.intellij.openapi.editor DefaultLanguageHighlighterColors)
           (com.intellij.psi.tree IElementType)
           (com.vlnabatov.alabaster UtilKt)
           (cursive.psi ClojurePsiElement)
           (org.jetbrains.plugins.scala.lang.lexer ScalaTokenTypes)
           (clojure.lang Keyword)
           (com.intellij.psi PsiElement))
  (:gen-class :implements [com.intellij.lang.annotation.Annotator]
              :name       com.vlnabatov.alabaster.extensions.annotation.CljAnnotator
              :prefix     "-"))


(def macro (Keyword/find "macro"))
(def ns (Keyword/find "ns"))

(def functions-macros-regex #"\bfn|defn|defn-|defmulti|defmethod|defmacro|deftest\b")


(defn is-clojure-lang-ns-call [^ClojurePsiElement e] ())



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