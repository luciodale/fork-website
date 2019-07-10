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

(defn fork
  [{:keys [initial-values validation] :as props}]
  (let [[state update-state]
        (r/useState {:values initial-values})
        props
        (merge props {:s state :u update-state
                      :validation (or validation {})})
        _ (logic/validate-client props :on-change-client :on-change)
        _ (logic/validate-client props :on-blur-client :on-blur)
        _ (logic/validate-server props :on-change-server :on-change)
        _ (logic/validate-server props :on-blur-server :on-blur)
        _ (logic/validate-client-submit props)
        _ (logic/validate-server-submit props)
        _ (logic/manage-submit-call props)
        _ (logic/submit-form props)
        ]
    [{:values (:values state)
      :state state
      :errors (logic/errors state)
      :dirty? (not= (:values state) (:initial-values props))
      :touched (:touched state)
      :is-submitting? (:is-submitting? state)
      :is-invalid? (logic/is-invalid? state)
      :is-waiting? (:waiting? state)
      :submit-count (:submit-count state)
      :no-submit-on-enter
      (r/useCallback #(logic/no-submit-on-enter %) #js [])
      :set-values
      (r/useCallback #(logic/set-values % props) #js [])
      :set-field-value
      (r/useCallback #(logic/set-field-value %1 %2 props) #js [])
      :set-touched
      (r/useCallback #(logic/set-touched % props) #js [])
      :set-field-touched
      (r/useCallback #(logic/set-field-touched % props) #js [])
      :handle-change
      (r/useCallback #(logic/handle-change % props) #js [])
      :clear-state
      (r/useCallback #(logic/clear-state props) #js [])
      :handle-blur
      (r/useCallback #(logic/handle-blur % props) #js [])
      :handle-submit
      (r/useCallback #(logic/handle-submit % props)
                     #js [(:values state)])}
     #_(framework-dispatch framework handlers)]))
