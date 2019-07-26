(ns fork.logic
  (:require
   [react :as r]))

(defn element-value
  [evt]
  (let [type (-> evt .-target .-type)]
    (case type
      "checkbox"
      (-> evt .-target .-checked)
      (-> evt .-target .-value))))

(defn set-submitting
  [bool {u :u}]
  (u #(assoc % :submitting? bool)))

(defn set-external-errors
  "Add errors coming from the external world.
  Those might be for example a bad server response.
  The errors are written in :external-errors and
  merged into :errors when provided to the user.
  These errors are dissoced from the state on each
  new submit."
  [errors {u :u}]
  (u #(merge % {:external-errors errors})))

;; Not used
(defn clear-external-errors
  [u]
  (u #(dissoc % :external-errors)))

(defn errors
  "Merges client, server, and external errors into one map."
  [s]
  (merge-with
   merge
   (:errors s)
   (:server-errors s)
   (:external-errors s)))

(defn invalid?
  "Check if form is valid by inspecting errors map"
  [s]
  (some some? (vals (errors s))))

(defn validation-submap
  "Retrieve a map subset of the validation map.
  The chosen subset depends upon:
  side -> :client or :server
  kind -> :on-change or :on-blur or :on-submit
  input -> the input that triggered the validation.
  The subset always includes the general validation
  pairs, which have keyword keys instead of strings."
  [side kind {:keys [validation s]} input]
  (->>
   (-> (validation (:values s)) side kind)
   (filter (fn [[validation-key _]]
             ;; filtering the general pairs
             ;; and the specific input triggering the validation
             (or (keyword? validation-key)
                 (= validation-key input))))
   (into {})))

(defn run-validation-client
  "schema: {'one' [[(= 1 1) :k1 'error 1']
                   [(= 1 2) :k2 'error 2']]
            'two' [[(= 1 1) :k3 'error 1']]}
  Loop over the input keys and nested vectors to update
  the :errors in the state. When the function is true,
  the input is error free. :k1, :k2, :k3, and so on
  must be unique per input key and can be any valid keyword.
  For example, the following schema will cause the second error to
  always overwrite the first one due to the same :k1 key.
  {'one' [[(= 1 1) :k1 'error 1']
          [(= 1 2) :k1 'error 2']]}
  Credits: Dominic Monroe"
  [{u :u} schema]
  (u
   #(update % :errors merge
            (zipmap (keys schema)
                    (map
                     (fn [validations]
                       (apply merge
                              (keep
                               (fn [[bool k msg]]
                                 (when-not bool {k msg}))
                               validations)))
                     (vals schema))))))

(defn set-waiting
  "Indicate that the validation is waiting for a server http response"
  [{u :u} k bool]
  (if bool
    (u #(assoc-in % [:waiting? k] :waiting))
    (u #(update % :waiting?
                (fn [m] (not-empty (dissoc m k)))))))

(defn set-valid
  "Resolve server side validation. The user only needs to provide
  a boolean value. Since this function is assumed to be called only
  after the server returned a response, the :waiting? variable for
  the specific input is set to false"
  [{u :u} input-key err-k msg bool]
  (u #(let [new-state
            (update % :waiting? (fn [m] (not-empty (dissoc m input-key))))]
        (if bool
          (update-in new-state [:server-errors input-key]
                     (fn [m] (not-empty (dissoc m err-k))))
          (assoc-in new-state [:server-errors input-key err-k] msg)))))


(defn run-validation-server
  "Set :waiting? true for all those inputs that include server side validation.
  Loop over the user provided functions and call them by passing some helpers
  to resolve the validation later on."
  [{u :u s :s :as props} schema]
  (let [val-keys (keys schema)]
    (u #(assoc % :waiting?
               (zipmap val-keys
                       (take (count val-keys)
                             (repeat true)))))
    (doseq [[input-key cond-coll] schema
            [func err-k msg] cond-coll]
      (func {:values (:values s)
             :errors (errors s)
             :set-waiting
             (fn [k bool] (set-waiting props k bool))
             :set-valid
             (fn [bool] (set-valid props input-key err-k msg bool))}))))

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
     (let [input (ffirst (state-k s))]
       (when (and (fn? validation)
                  (some? input))
         (run-validation-client
          props
          (validation-submap
           :client val-k props input))))
     (fn []
       (u #(-> %
               (dissoc state-k)
               (assoc :client-cleared true)))))
   #js [(state-k s)]))

(defn validate-server
  "Effect that runs only after the :client-cleared key becomes true.
  It dispatches the http request for the server side validation"
  [{:keys [u s validation] :as props} state-k val-k]
  (r/useEffect
   (fn []
     (let [input (ffirst (state-k s))]
       (when (and (fn? validation)
                  (some? input)
                  (:client-cleared s))
         (run-validation-server
          props
          (validation-submap
           :server val-k props input))))
     (fn []
       (u #(dissoc %
                   state-k
                   :client-cleared))))
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
           into
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
           into
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
