(ns fork.hooks
  (:require
   [fork.shared :as shared]
   [react :as r]))

(defn element-value
  [evt]
  (shared/element-value evt))

(defn errors
  [s]
  (shared/errors s))

(defn set-external-errors
  [errors props]
  (shared/set-external-errors errors props))

(defn invalid?
  [s]
  (shared/invalid? s))

(defn set-submitting
  [bool props]
  (shared/set-submitting bool props))

(defn validation-submap
  [side kind props input]
  (shared/validation-submap side kind props input))

(defn run-validation-client
  [props schema]
  (shared/run-validation-client props schema))

(defn set-waiting
  [props k bool]
  (shared/set-waiting props k bool))

(defn set-valid
  [props input-key err-k msg bool]
  (shared/set-valid props input-key err-k msg bool))

(defn run-validation-server
  [props schema]
  (shared/run-validation-server props schema))

(defn set-touched
  [m {u :u}]
  (u #(-> %
          (update :touched merge m)
          (assoc :on-change-client m)
          (assoc :on-blur-client m)
          (assoc :on-change-server m)
          (assoc :on-blur-server m))))

(defn set-field-touched
  [k {u :u}]
  (u #(-> %
          (assoc-in [:touched k] true)
          (assoc-in [:on-change-client k] true)
          (assoc-in [:on-blur-client k] true)
          (assoc-in [:on-change-server k] true)
          (assoc-in [:on-blur-server k] true))))

(defn set-values
  [m {u :u}]
  (u #(-> %
          (update :values merge m)
          (assoc :on-change-client m)
          (assoc :on-blur-client m)
          (assoc :on-change-server m)
          (assoc :on-blur-server m))))

(defn set-field-value
  [k v {u :u}]
  (u #(-> %
          (assoc-in [:values k] v)
          (assoc-in [:on-change-client k] true)
          (assoc-in [:on-blur-client k] true)
          (assoc-in [:on-change-server k] true)
          (assoc-in [:on-blur-server k] true))))

(defn clear-state
  [{u :u}]
  (u nil))

(defn handle-change
  "Set the new input value"
  [evt {u :u}]
  (let [k (-> evt .-target .-name)
        v (element-value evt)]
    (u #(-> %
            (assoc-in [:values k] v)
            (assoc-in [:on-change-client k] true)
            (assoc-in [:on-change-server k] true)))))

(defn handle-blur
  "Set the input to touched"
  [evt {u :u}]
  (let [k (-> evt .-target .-name)]
    (u #(-> %
            (assoc-in [:touched k] true)
            (assoc-in [:on-change-client k] true)
            (assoc-in [:on-change-server k] true)
            (assoc-in [:on-blur-client k] true)
            (assoc-in [:on-blur-server k] true)))))

;; Not used
(defn touch-all
  "Set all inputs to touched true."
  [{s :s u :u}]
  (let [input-names (keys (:values s))]
    (u #(assoc % :touched
               (zipmap input-names
                       (take (count input-names)
                             (repeat true)))))))

(defn validate-client
  "Effect that runs every time the :on-change-client and
  :on-blur-client values change.
  It is used for the client side :on-change and :on-blur validation"
  [{:keys [u s validation] :as props} state-k val-k]
  (r/useEffect
   (fn []
     (when (fn? validation)
       (run-validation-client
        props
        (into {}
              (for [[input _] (state-k s)]
                (validation-submap
                 :client val-k props input)))))
     (fn []
       (when (fn? validation)
         (u #(-> %
                 (dissoc state-k)
                 (assoc :client-cleared true))))))
   #js [(state-k s)]))

(defn validate-server
  "Effect that runs only after the :client-cleared key becomes true.
  It dispatches the http requests for the server side validation"
  [{:keys [u s validation] :as props} state-k val-k]
  (r/useEffect
   (fn []
     (when (and (fn? validation)
                (:client-cleared s))
       (run-validation-server
        props
        (into {}
              (for [[input _] (state-k s)]
                (validation-submap
                 :server val-k props input)))))
     (fn []
       (when (fn? validation)
         (u #(dissoc % state-k :client-cleared)))))
   #js [(:client-cleared s)]))

(defn validate-client-submit
  [{:keys [u s validation] :as props}]
  (r/useEffect
   (fn []
     (let [validation-resolve (validation (:values s))
           get-validation #(-> validation-resolve %1 %2)]
       (when (and (:validation-exists? s)
                  (:submitting? s))
         (run-validation-client
          props
          (merge-with
           merge
           (get-validation :client :on-blur)
           (get-validation :client :on-change)
           (get-validation :client :on-submit)))))
     (fn [] (u #(assoc % :client-cleared-submit true))))
   #js [(:submitting? s)
        (:validation-exists? s)]))

(defn validate-server-submit
  [{:keys [u s validation] :as props}]
  (r/useEffect
   (fn []
     (let [validation-resolve (validation (:values s))
           get-validation #(-> validation-resolve %1 %2)]
       (when (and (:validation-exists? s)
                  (:submitting? s)
                  (:client-cleared-submit s))
         (run-validation-server
          props
          (merge-with
           merge
           (get-validation :server :on-blur)
           (get-validation :server :on-change)
           (get-validation :server :on-submit)))))
     (fn []
       (u #(-> %
               (dissoc :client-cleared-submit)
               (assoc :on-submit-ready true)))))
   #js [(:client-cleared-submit s)]))

(defn handle-submit
  [evt {u :u s :s p :prevent-default? :as props}]
  (when p (.preventDefault evt))
  (u #(-> %
          (dissoc :external-errors)
          (assoc :submitting? true)
          (assoc :touched
                 (let [input-names (keys (:values s))]
                   (zipmap input-names
                           (take (count input-names)
                                 (repeat true)))))
          (assoc :validation-exists?
                 (when (fn? (:validation props)) true)))))

(defn handle-submit-func
  [on-submit {s :s init :initial-values :as props}]
  (on-submit
   {:invalid? (invalid? s)
    :errors (errors s)
    :submit-count (:submit-count s)
    :values (:values s)
    :dirty? (not= (:values s) init)
    :set-external-errors
    #(set-external-errors % props)
    :set-touched
    #(set-touched % props)
    :set-values
    #(set-values % props)
    :set-submitting
    #(set-submitting % props)
    :clear-state
    #(clear-state props)}))

(defn manage-submit-call
  [{:keys [u s]}]
  (r/useEffect
   (fn []
     (when (and (:on-submit-ready s)
                (not (:waiting? s)))
       (u #(assoc % :can-submit true)))
     identity)
   #js [(:waiting? s)
        (:on-submit-ready s)]))

(defn submit-form
  [{:keys [u s validation on-submit] :as props}]
  (r/useEffect
   (fn []
     (when (:submitting? s)
       (u #(update % :submit-count inc))
       (cond
         (:validation-exists? s)
         (when
             (and (every? nil? (vals (:waiting? s)))
                  (:on-submit-ready s))
           (handle-submit-func on-submit props))
         :else
         (handle-submit-func on-submit props)))
     (fn [] (u #(dissoc % :can-submit :on-submit-ready))))
   #js [(:can-submit s)]))
