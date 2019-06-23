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
        [is-invalid?
         values
         dirty?
         set-touched
         set-values
         set-submitting
         set-after-submit-errors
         clear-state]}]
  (.preventDefault evt)
  (js/setTimeout
   #(do
      (when is-invalid?
        (set-after-submit-errors {:server-404 "server 404"})
        (js/alert values)))
   1000)
  (set-submitting false))

(defn validation-schema
  [values]
  {:client
   {:on-change
    {"input"
     [[(> (count (:input values)) 10)
       {:err1 "Input has to be bigger than 3"}]
      [(= "hello" (:input values))
       {:err2 "must equal hello"}]]}}})

(defn fork [_]
  (html
   (let [[{:keys [values
                  is-submitting?
                  dirty?
                  touched
                  state
                  errors
                  no-submit-on-enter
                  handle-change
                  handle-blur
                  handle-submit] :as props}
          {:keys [field]}]
         (fork/fork
          {:id "form-id"
           :framework :bulma
           :on-submit on-submit
           :validation  validation-schema
           :initial-values
           {"input" "bellaa"}})]
     (prn state)
     [:form
      {:style {:margin-top "200px"}
       :on-submit handle-submit
       :on-key-down no-submit-on-enter}
      [:input
       {:name "input"
          :type "text"
          :disabled (= "aaa" (:area values))
        :value (values "input")
          :on-blur handle-blur
          :on-change handle-change}]
      (when (and (get touched "input")
                 (get errors "input"))
        [:div (str (vals (:input errors)))])

      [:button
       {:type "submit"
        :disabled is-submitting?}
       "My Submit button"]
      (when (:server-404 errors)
        [:p (:server-404 errors)])
      #_[:a {:href "example"}
         "click to go to example"]])))
