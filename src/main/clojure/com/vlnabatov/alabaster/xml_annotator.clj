(ns com.vlnabatov.alabaster.xml-annotator
  (:require [com.vlnabatov.alabaster.util :refer [import-static]])
  (:import (com.intellij.lang.annotation AnnotationHolder)
           (com.intellij.lang.annotation HighlightSeverity)
           (com.intellij.openapi.editor DefaultLanguageHighlighterColors)
           (com.intellij.psi.xml XmlTokenType)
           (com.intellij.psi PsiElement))
  (:gen-class :implements [com.intellij.lang.annotation.Annotator]
              :name       com.vlnabatov.alabaster.extensions.annotation.XMLAnnotator
              :prefix     "-"))


(defn -annotate
  [_ ^PsiElement element ^AnnotationHolder holder]
  (when (#{XmlTokenType/XML_EQ XmlTokenType/XML_ATTRIBUTE_VALUE_START_DELIMITER
           XmlTokenType/XML_ATTRIBUTE_VALUE_END_DELIMITER}
         (.. element getNode getElementType))
    (do (-> holder
            (.newSilentAnnotation HighlightSeverity/INFORMATION)
            (.textAttributes DefaultLanguageHighlighterColors/NUMBER)
            (.create)))))
