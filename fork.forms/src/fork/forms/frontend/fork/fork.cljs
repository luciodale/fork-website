(ns fork.fork
  (:require
   [fork.logic :as logic]
   [react :as r]))

(defn initiate-state
  [{:keys [initial-values]}]
  {:values initial-values})

(defn fork-form
  [{:keys [on-submit
           initial-values
           validation] :as props}]
  (let [[state update-state]
        (r/useState (initiate-state
                     {:initial-values initial-values}))
        is-submitting? (:is-submitting? state)
        values (:values state)
        props (merge props {:s state :u update-state})]
    (when validation
      (logic/effect-run-validation
       update-state ((second validation) values)))
    (prn state)
    [{:values #(logic/values state %)
      :errors (:errors state)
      :dirty? (not= (:values state) initial-values)
      :touched (:touched state)
      :is-submitting? is-submitting?
      :no-submit-on-enter
      #(logic/no-submit-on-enter %)
      :handle-change
      #(logic/handle-change % props)
      :clear-state
      #(logic/clear-state props)
      :handle-blur
      #(logic/handle-blur % props)
      :handle-on-submit
      #(logic/handle-on-submit % on-submit props)}]))
