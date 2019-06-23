(ns fork.forms.frontend.views.demo
  (:require-macros
   [fork.forms.frontend.hicada :refer [html]])
  (:require
   [ajax.core :as ajax]
   [react :as r]
   [fork.fork :as fork]))

(defn handler
  [[response body] values is-validation-passed?]
  (prn "server result:" response body)
  (is-validation-passed?
   (:validation body) :three))

(defn server-validation
  [props is-validation-passed?]
  (ajax/ajax-request
   {:uri  "/validation"
    :method :post
    :params
    {:input (get (-> props :s :values) "two")}
    :handler #(handler % props is-validation-passed?)
    :format  (ajax/transit-request-format)
    :response-format (ajax/transit-response-format)}))

(defn validation [values]
  {:client
   {:on-change
    {"one"
     [[(= "hello" (get values "one")) {:one "one"}]
      [(= "hello" (get values "one")) {:tfhree "one"}]]}}
   :server
   {:on-submit
    {"two"
     [[server-validation {:three "one"}]]}}})

(defn on-submit
  [{:keys [set-submitting
               is-invalid?
               values is-waiting?]}]

  (prn "is-invalid?" is-invalid?)
  (set-submitting false))

(defn view [_]
  (let [[{:keys [values
                 state
                 errors
                 is-invalid?
                 test
                 handle-change
                 handle-submit
                 handle-blur]}]
        (fork/fork {:validation validation
                    :on-submit on-submit
                    :prevent-default? true
                    :initial-values
                    {"one" ""
                     "two" ""}})]
    (html
     [:form.wrapper3
      {:on-submit handle-submit}
      [:p "Vals:" (get values "one")]
      [:input
       {:style {:line-height "2em"
                :width "400px"}

        :name "one"
        :value (get values "one")
        :on-change handle-change
        :on-blur handle-blur}]
      [:div
       [:input
        {:style {:line-height "2em"
                 :width "400px"}
         :name "two"
         :value (get values "two")
         :on-change handle-change
         :on-blur handle-blur}]]
      [:button
       {:type "submit"}
       "My Submit button"]])))
