(ns fork.forms.frontend.devcards.hooks
  (:require
   [react :as r])
  (:require-macros
   [devcards.core :as dc :refer [defcard defcard-doc]]
   [fork.forms.frontend.hicada :refer [html]]))

(defn dio []
  (html
   (let [[s u] (r/useState 0)]
     [:div
      [:p s]
      [:p
       {:on-click #(u inc)}
       "click me dio"]])))

(defcard hooks-test
  (html
   [:> dio nil]))
