(ns fork.forms.frontend.views.docs
  (:require-macros
   [fork.forms.frontend.hicada :refer [html]])
  (:require
   [react :as r]))

(defn view []
  (html
   [:*
    (let [[state update-state] (r/useState {})
          u update-state]
      [:div
       [:input
        {:on-change
         (fn [evento]
           (prn (-> evento .-target .-value)))}]
       [:select
        {:on-change
         (fn [evento]
           (prn (-> evento .-target .-value)))}
        [:option "ciao"]
        [:option "saab"]
        [:option "audi"]]]

      #_[:div
       [:input
        {:on-change (f u state)}]
       (:a state)])]))
