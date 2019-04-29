(ns fork.forms.frontend.views
  (:require-macros
   [fork.forms.frontend.hicada :refer [html]])
  (:require
   [fork.forms.frontend.views.docs :as docs]
   [fork.forms.frontend.views.test :as test]))

(defn main-panel
  [page]
  (html
   (case page
     :index [:> test/fork nil]
     :docs [:> docs/view nil]
     [:div "nothing found"])))
