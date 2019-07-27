(ns fork.forms.frontend.views.test
  (:require-macros
   [fork.forms.frontend.hicada :refer [html]])
  (:require
   [ajax.core :as ajax]
   [fork.fork :as fork]
   [fork.hooks :as hooks]
   [react :as r]))

(defn handler
  [[response body] values set-valid]
  (set-valid (:validation body)))

(defn server-validation
  [{:keys [values set-valid]}]
  (ajax/ajax-request
   {:uri  "/validation"
    :method :post
    :params
    {:input (get values "two")}
    :handler #(handler % values set-valid)
    :format  (ajax/transit-request-format)
    :response-format (ajax/transit-response-format)}))

(defn validation [values]
  {:client
   {:on-change
    {"one"
     [[(not (clojure.string/blank? (values "one")))
       :err "Error message on change client"]]}}
   :server
   {:on-change
    {"two"
     [[server-validation :err "Error message server"]]}}})

(def fn-count (atom #{}))

(defn on-submit
  [{:keys [set-submitting invalid?
           values waiting? errors]}]
  (prn "is-invalid?" invalid?)
  (prn "errors:" errors)
  (if invalid?
    (do "is invalid!!"
        (set-submitting false))
    (do
      (js/alert values)
      (set-submitting false)))
  )

#_(defn view [_]
    (let [[c1 set-c1] (r/useState 0)
          [c2 set-c2] (r/useState 0)
          inc1 (r/useCallback (fn [] (set-c1 inc)) #js [c1])
          inc2 (r/useCallback (fn [] (set-c2 inc)) #js [c2])]
      (swap! fn-count conj inc1)
      (swap! fn-count conj inc2)
      (html
       [:div {:style
              {:margin-top "100px"}}
        [:div (str "Counter 1 is" c1)]
        [:div (str "Counter 2 is" c2)]
        [:br]
        [:button {:on-click inc1} "Inc 1"]
        [:button {:on-click inc2} "Inc 2"]
        [:br]
        [:div "Newly created fns:" (- (count @fn-count) 2)]])))

(defn view [_]
  (let [[{:keys [values
                 state
                 errors
                 invalid?
                 submitting?
                 handle-change
                 set-values
                 handle-submit
                 handle-blur]}]
        (fork/fork {
                    :on-submit on-submit
                    :prevent-default? true
                    :initial-values
                    {"one" ""
                     "two" ""}
                    :validation validation})]
    (prn state)
    (html
     [:div {:style
            {:text-align "center"
             :margin-top "100px"}}
      [:form.wrapper3
       {:on-submit handle-submit}
       [:input
        {:style {:line-height "2em"
                 :width "400px"}
         :value (get values "one")
         :name "one"
         :on-change handle-change
         :on-blur handle-blur}]
       [:div
        (for [[k msg] (get errors "one")]
          [:p.help
           {:key k}
           msg])]
       [:div
        [:input
         {:style {:line-height "2em"
                  :width "400px"}
          :name "two"
          :value (get values "two")
          :on-change handle-change
          :on-blur handle-blur}]
        (for [[k msg] (get errors "two")]
          [:p.help
           {:key k}
           msg])]
       [:button.button
        {:type "submit"
         :class (when submitting? "is-loading")}
        "My Submit button"]]])))
