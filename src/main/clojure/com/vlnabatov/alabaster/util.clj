(ns com.vlnabatov.alabaster.util
  (:require [clojure.set :as set :refer [intersection]]
            [clojure.pprint :as pprint]
            [clojure.reflect :as reflect])
  (:import (java.lang.reflect Modifier)))


(defmacro import-static
  "Imports the named static fields and/or static methods of the class
  as (private) symbols in the current namespace.

  Example:
      user=> (import-static java.lang.Math PI sqrt)
      nil
      user=> PI
      3.141592653589793
      user=> (sqrt 16)
      4.0

  Note: The class name must be fully qualified, even if it has already
  been imported.  Static methods are defined as MACROS, not
  first-class fns."
  [class & fields-and-methods]
  (let [only          (set (map str fields-and-methods))
        the-class     (. Class forName (str class))
        static?       (fn [x] (. Modifier (isStatic (. x (getModifiers)))))
        statics       (fn [array] (set (map (memfn getName) (filter static? array))))
        all-fields    (statics (. the-class (getFields)))
        all-methods   (statics (. the-class (getMethods)))
        fields-to-do  (intersection all-fields only)
        methods-to-do (intersection all-methods only)
        make-sym      (fn [string] (with-meta (symbol string) {:private true}))
        import-field  (fn [name] (list 'def (make-sym name) (list '. class (symbol name))))
        import-method (fn [name]
                        (list
                          'defmacro
                          (make-sym name)
                          '[& args]
                          (list 'list ''. (list 'quote class) (list 'apply 'list (list 'quote (symbol name)) 'args))))]
    `(do ~@(map import-field fields-to-do) ~@(map import-method methods-to-do))))


(defn print-methods
  [c]
  (->> (reflect/reflect c)
       :members
       (filter :return-type)
       (sort-by :name)
       (map #(select-keys % [:name :parameter-types :return-type]))
       (pprint/print-table)))
