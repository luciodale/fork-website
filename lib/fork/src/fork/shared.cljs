(ns fork.shared)

(defn element-value
  [evt]
  (let [type (-> evt .-target .-type)]
    (case type
      "checkbox"
      (-> evt .-target .-checked)
      (-> evt .-target .-value))))

(defn errors
  "Merges client, server, and external errors into one map."
  [s]
  (merge-with
   merge
   (:errors s)
   (:server-errors s)
   (:external-errors s)))

(defn set-external-errors
  "Add errors coming from the external world.
  Those might be for example a bad server response.
  The errors are written in :external-errors and
  merged into :errors when provided to the user.
  These errors are dissoced from the state on each
  new submit."
  [errors {u :u}]
  (u #(merge % {:external-errors errors})))

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
  [side kind {:keys [validation values]} input]
  (->>
   (-> (validation values) side kind)
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
  [schema]
  (zipmap (keys schema)
          (map
           (fn [validations]
             (apply merge
                    (keep
                     (fn [[bool k msg]]
                       (when-not bool {k msg}))
                     validations)))
           (vals schema))))

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
  [{s :s :as props} schema]
  (doseq [[input-key cond-coll] schema
          [func err-k msg] cond-coll]
    (func {:values (:values s)
           :errors (errors s)
           :set-waiting
           (fn [k bool] (set-waiting props k bool))
           :set-valid
           (fn [bool] (set-valid props input-key err-k msg bool))})))

(defn set-submitting
  [bool {u :u}]
  (u #(assoc % :submitting? bool)))

(defn clear-state
  [{u :u}]
  (u nil))

(defn dirty?
  [{:keys [s initial-values]}]
  (not= (:values s) initial-values))

(defn disabled?
  [s input]
  (get (:disabled? s) input))

(defn set-disabled
  [u input bool]
  (u #(assoc-in % [:disabled? input] bool)))
