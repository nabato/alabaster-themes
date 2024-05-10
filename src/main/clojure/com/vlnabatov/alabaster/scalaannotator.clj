(ns com.vlnabatov.alabaster.scalaannotator
  (:import (com.intellij.lang.annotation Annotator))
  (:gen-class :implements [com.intellij.lang.annotation.Annotator]
              :name
                com.vlnabatov.alabaster.extensions.annotation.CLJScalaAnnotator
              :prefix "-"))

(defn -annotate [this a b] (println "hello"))
