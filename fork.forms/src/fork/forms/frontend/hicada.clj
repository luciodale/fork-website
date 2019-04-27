(ns fork.forms.frontend.hicada
  (:require
   hicada.compiler))

(defmacro html
  [body]
  (hicada.compiler/compile body))
