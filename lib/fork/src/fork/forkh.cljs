(ns fork.forkh
  (:require
   #_[fork.bulma :as bulma]
   [fork.hooks :as hooks]
   [react :as r]))

;;-------- hooks ---------

(defn fork
  [{:keys [initial-values validation] :as props}]
  (let [[state update-state] (r/useState {:values initial-values})
        props
        (merge props
               {:s state :u update-state :validation (or validation {})})
        _ (hooks/validate-client props :on-change-client :on-change)
        _ (hooks/validate-client props :on-blur-client :on-blur)
        _ (hooks/validate-server props :on-change-server :on-change)
        _ (hooks/validate-server props :on-blur-server :on-blur)
        _ (hooks/validate-client-submit props)
        _ (hooks/validate-server-submit props)
        _ (hooks/manage-submit-call props)
        _ (hooks/submit-form props)]
    [{:values (:values state)
      :state state
      :update-state update-state
      :errors (hooks/errors state)
      :dirty? (not= (:values state) (:initial-values props))
      :touched (:touched state)
      :submitting? (:submitting? state)
      :invalid? (hooks/invalid? state)
      :waiting? (:waiting? state)
      :submit-count (:submit-count state)
      :set-values
      (r/useCallback #(hooks/set-values % props) #js [])
      :set-field-value
      (r/useCallback #(hooks/set-field-value %1 %2 props) #js [])
      :set-touched
      (r/useCallback #(hooks/set-touched % props) #js [])
      :set-field-touched
      (r/useCallback #(hooks/set-field-touched % props) #js [])
      :handle-change
      (r/useCallback #(hooks/handle-change % props) #js [])
      :clear-state
      (r/useCallback #(hooks/clear-state props) #js [])
      :handle-blur
      (r/useCallback #(hooks/handle-blur % props) #js [])
      :handle-submit
      (r/useCallback #(hooks/handle-submit % props)
                     #js [(:values state)])}
     "maybe some 'easy-input' here?"]))
