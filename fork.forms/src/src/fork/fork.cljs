(ns fork.fork
  (:require
   [fork.bulma :as bulma]
   [fork.logic :as logic]
   [react :as r]))

(defn framework-dispatch
  [framework handlers]
  (case framework
    :bulma (fork.bulma/framework handlers)
    "default"))

(defn handlers
  [props state update-state]
  {:values (:values state)
   :errors (merge (:errors state) (:global-errors state))
   :dirty? (not= (:values state) (:initial-values props))
   :touched (:touched state)
   :is-submitting? (:is-submitting? state)
   :no-submit-on-enter
   #(logic/no-submit-on-enter %)
   :handle-change
   #(logic/handle-change % props)
   :clear-state
   #(logic/clear-state props)
   :handle-blur
   #(logic/handle-blur % props)
   :handle-on-submit
   #(logic/handle-on-submit % (:on-submit props) props)})

(defn fork-form
  [{:keys [initial-values validation framework] :as props}]
  (let [[state update-state] (r/useState {:values initial-values})
        props (merge props {:s state :u update-state})
        handlers (handlers props state update-state)]
    (when validation
      (logic/effect-run-validation
       props ((second validation) (:values state))))
    [handlers
     (framework-dispatch framework handlers)]))
