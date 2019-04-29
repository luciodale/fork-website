(ns fork.forms.frontend.views.test
  (:require-macros
   [fork.forms.frontend.hicada :refer [html]])
  (:require
   [fork.fork :as fork]))

(defn actions
  [values]
  (when (= "ciao" (:keyword values))
    {:check
     {:value true
      :disabled true}}))

(defn fork []
  (html
   (let [{:keys [values
                 handle-change]}
         (fork/fork)]
     (prn values)
     [:div
      [:input
       {:name :keyword
        :on-change handle-change}]
      [:input
       {:name :check
        :type "checkbox"
        :on-change handle-change}]
      [:input
       {:name :date
        :type "date"
        :on-change handle-change}]
      [:textarea
       {:name :area
        :on-change handle-change}]])))
