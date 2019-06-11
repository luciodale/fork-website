(ns fork.forms.frontend.views.demo
  (:require-macros
   [fork.forms.frontend.hicada :refer [html]])
  (:require
   [react :as r]
   [fork.fork :as fork]))

(defn validation [values]
  {"one"
   [[(= "hello" (get values "one")) {:whatever1 "one"}]]
   "two"
   [[(= "hello" (get values "two")) {:whatever2 "two"}]]})

(defn on-submit
  [evt {:keys [set-submitting is-invalid? values]}]
  (.preventDefault evt)
  (js/alert values)
  (set-submitting false))

(defn view [_]
  (html
   (let [[{:keys [values
                  state
                  handle-change
                  handle-submit
                  set-values
                  is-validating?
                  handle-blur]}]
         (fork/fork {:validation [:on-change validation]
                          :on-submit on-submit
                          :id "idd"
                          :initial-values
                          {"one" ""
                           "two" ""}})]
     (prn state)
     (r/useEffect
      (fn []
        (prn "in effect")
        (when (= (get values "two") "hello")
          (set-values {"one" "hello"}))
        identity)
      #js [(get values "two")])
     [:form.wrapper3
      {:id "idd"
       :on-submit handle-submit}
      [:p "Vals:" (get values "one")]
      [:input
       {:style {:line-height "2em"
                :width "400px"}

        :name :one
        :value (get values "one")
        :on-change handle-change
        :on-blur handle-blur}]
      [:div
       [:input
        {:style {:line-height "2em"
                 :width "400px"}
         :name :two
         :value (get values "two")
         :on-change handle-change
         :on-blur handle-blur}]]
      [:button
       {:type "submit"}
       "My Submit button"]])))
