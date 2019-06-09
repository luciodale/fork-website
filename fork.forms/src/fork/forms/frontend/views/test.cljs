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
         set-global-errors
         clear-state]}]
  (.preventDefault evt)
  (js/setTimeout
   #(do
      (set-global-errors {:server-404 "server 404"})
      (js/alert values))
   1000)
  (set-submitting false))

(defn validation-schema
  [values]
  {:input
   [[(> (count (:input values)) 3)
     {:err1 "Input has to be bigger than 3"}]
    [(= "hello" (:input values))
     {:err2 "must equal hello"}]]
   :area [[(= "hello" (:area values))
           {:err1 "must be hello"}]]
   :this-id [[(= "hi" (:this-id values))
              {:a "show this ya?"}]]})

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
                  handle-submit] :as props}
          {:keys [field]}]
         (fork/fork-form
          {:id "form-id"
           :framework :bulma
           :on-submit on-submit
           :validation [:on-change validation-schema]
           :initial-values
           {:input "bellaa"
            :area "ahah"
            :dropdown "two"}})]
     (prn (:dropdown values))
     [:form
      {:id "form-id"
       :on-submit handle-submit
       :on-key-down no-submit-on-enter}
      (field {:label "hello"
              :name :this-id
              :type "text"})
      [:input
         {:name :input
          :type "text"
          :disabled (= "aaa" (:area values))
          :value (:input values "")
          :on-blur handle-blur
          :on-change handle-change}]
      (when (and (:input touched) (:input errors))
        [:div (str (vals (:input errors)))])
      ;; checkbox
      [:input
       {:name :check
        :type "checkbox"
        :on-blur handle-blur
        :on-change handle-change}]
      ;; date
      [:input
       {:name :date
        :type "date"
        :value (:date values "")
        :on-change handle-change
        :on-blur handle-blur}]
      ;; dropdown
      [:select
       {:name :dropdown
        :value (:dropdown values)
        :on-change handle-change
        :on-blur handle-blur}
       [:option
        {:value "one"} "one"]
       [:option
        {:value "two"} "two"]]
      (when (:check values)
        [:input
         {:name :new-one
          :value (:new-one values "")
          :on-change handle-change
          :on-blur handle-blur}])
      ;; area
        [:textarea
         {:name :area
          :value (:area values "")
          :on-change handle-change
          :on-blur handle-blur}]
      ;;submit
      [:button
       {:type "submit"
        :disabled is-submitting?}
       "My Submit button"]
      (when (:server-404 errors)
        [:p (:server-404 errors)])
      #_[:a {:href "example"}
         "click to go to example"]])))
