(ns fork.forms.frontend.views.test
  (:require-macros
   [fork.forms.frontend.hicada :refer [html]])
  (:require
   [fork.fork :as fork]
   [react :as r]))

(defn actions
  [values]
  (when (= "ciao" (:keyword values))
    {:check
     {:value true
      :disabled true}}))

(defn on-submit
  [evt {:keys
        [errors?
         values
         dirty?
         set-touched
         set-values
         set-submitting
         clear-state]}]
  (.preventDefault evt)
  (if errors?
    (do
      (js/setTimeout
       #(set-values {:input "hello"})
       500))
    (js/setTimeout
     #(do
        (js/alert values))
     1000))
  (set-submitting false))

(defn validation-schema
  [values]
  {:input
   [[(> (count (:input values)) 3)
     "Input has to be bigger than 3"]
    [(= "hello" (:input values))
     "must equal hello"]]
   :area [[(= "hello" (:area values)) "must be hello"]]})

(defn fork []
  (html
   (let [[{:keys [values
                  is-submitting?
                  dirty?
                  touched
                  errors
                  no-submit-on-enter
                  handle-change
                  handle-blur
                  handle-on-submit]}
          ]
         (fork/fork-form
          {:on-submit on-submit
           :validation [:on-change validation-schema]
           :initial-values
           {:input "bellaa"
            :area "ahah"}})]
     [:form
      {:on-submit handle-on-submit
       :on-key-down no-submit-on-enter}
      [:input
         {:name :input
          :type "text"
          :disabled (= "aaa" (:area values))
          :value (:input values "")
          :on-blur handle-blur
          :on-change handle-change}]
      (when (and (:input touched) (:input errors))
        [:div "errors here"])
        [:input
         {:name :check
          :type "checkbox"
          :on-blur handle-blur
          :on-change handle-change}]
      (prn (values :area))
      [:input
       {:name :date
        :type "date"
        :on-change handle-change}]
        [:textarea
         {:name :area
          :value (:area values "")
          :on-change handle-change
          :on-blur handle-blur
          }]
        [:button
         {:type "submit"
          :disabled is-submitting?}
         "My Submit button"]
        [:a {:href "example"}
         "click to go to example"]])))
