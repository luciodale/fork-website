(ns fork.forms.frontend.views.common
  (:require-macros
   [fork.forms.frontend.hicada :refer [html]])
  (:require
   [react :as r]))


(defn code-snippet
  [snippet]
  (let [[state update-state] (r/useState "Copy")]
    (html
     [:div.code-snippet
      [:pre
       [:code.clj snippet]]
      [:a.button.code-snippet__copy
        {:class "btn-code"
         :data-clipboard-text snippet
         :on-click #(update-state "Copied")}
        [:span.icon
         [:i.far.fa-copy]]
        [:span state]]])))
