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
  [evt values]
  (.preventDefault evt)
  (js/alert values))

(defn fork []
  (html
   (let [{:keys [values
                 handle-change
                 handle-on-submit]}
         (fork/fork-form
          {:on-submit on-submit})]
     (prn values)
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
       {:type "submit"}
       "My Submit button"]])))
