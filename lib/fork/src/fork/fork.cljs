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
  [props state]
  {:values (:values state)
   :state state
   :errors (merge-with
            into
            (:errors state)
            (:after-submit-errors state)
            (:server-errors state))
   :dirty? (not= (:values state) (:initial-values props))
   :touched (:touched state)
   :is-submitting? (:is-submitting? state)
   :is-invalid? (some
                 some?
                 (vals
                  (merge-with
                   into
                   (:errors state)
                   (:after-submit-errors state)
                   (:server-errors state))))
   :is-waiting? (:waiting? state)
   :no-submit-on-enter
   #(logic/no-submit-on-enter %)
   :set-values
   #(logic/set-values % props)
   :set-field-value
   #(logic/set-field-value %1 %2 props)
   :set-touched
   #(logic/set-touched % props)
   :set-field-touched
   #(logic/set-field-touched % props)
   :handle-change
   #(logic/handle-change % props)
   :validate-form
   #(logic/validate-form props)
   :clear-state
   #(logic/clear-state props)
   :handle-blur
   #(logic/handle-blur % props)
   :handle-submit
   #(logic/handle-submit % props)})

(defn fork
  [{:keys [initial-values] :as props}]
  (let [[state update-state]
        (r/useState {:values initial-values})
        props
        (merge props {:s state :u update-state})
        handlers (handlers props state)
        _ (logic/validate-on-change props)
        _ (logic/validate-on-blur props)
        _ (logic/submit-form props)]
    [handlers
     #_(framework-dispatch framework handlers)]))
