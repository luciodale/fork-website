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

(defn on-submit
  [evt values set-submitting]
  (.preventDefault evt)
  (js/setTimeout
   #(do
      (js/alert values)
      (set-submitting false))
   2000))

(defn fork []
  (html
   (let [{:keys [values
                 is-submitting?
                 handle-change
                 handle-on-submit]}
         (fork/fork-form
          {:on-submit on-submit})]
     [:form
      {:on-submit handle-on-submit}
      [:input
       {:name :input
        :type "text"
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
        :on-change handle-change}]
      [:button
       {:type "submit"
        :disabled is-submitting?}
       "My Submit button"]
      [:a {:href "example"}
       "click to go to example"]])))
