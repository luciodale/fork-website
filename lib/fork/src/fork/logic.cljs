(ns fork.logic
  (:require
   [clojure.string :as clostr]
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
  (u #(assoc % :is-submitting? bool)))

(defn set-on-submit-errors
  "Add errors coming from the external world.
  Those might be for example a bad server response.
  The errors are set under the key :global-errors and
  subsequently under the error message key itself.
  The global errors are dissoced from the state on each
  new submit."
  [errors {u :u}]
  (doseq [[k error] errors]
    (u #(assoc-in % [:on-submit-errors k] error))))

(defn clear-on-submit-errors
  [u]
  (u #(dissoc % :on-submit-errors)))

(defn retrieve-target-and-general
  "Retrieve target and general checks based on
  :client/server and :on-change/:on-blur/:on-submit params.
  The target-vals are merged with the old one in values.
  It's not enough to pass only the target-vals because the general
  checks might depend upon different values."
  [side kind {:keys [validation s]} target-vals]
  (->>
   (-> (validation (:values s)) side kind)
   (filter (fn [[k _]]
             (or (keyword? k)
                 (some (fn [[target-k _]]
                         (= k target-k)) target-vals))))
   (into {})))

(defn run-validation-client
  "schema: {:one [[true {:er1 'error 1'}
                  [true {:er2 'error 2'}]]]
            :two [[false {:er1 'error 1'}]]
  Loop over the input keys and nested vectors to update
  the :errors and :errors-key keys in the state. A true value implies that
  the input is error free. Error keys must be unique per
  per input key"
  [{u :u} schema]
  (doseq [[input-key cond-coll] schema
          [bool msg] cond-coll
          :let [err-key (ffirst msg)]]
    (cond
      bool
      (u #(update-in % [:errors input-key]
                     (fn [m] (not-empty (dissoc m err-key)))))
      :else
      (u #(assoc-in % [:errors input-key err-key] (err-key msg))))))

(defn validation-waiting-for-server?
  [{u :u} k bool]
  (if bool
    (u #(assoc-in % [:waiting? k] :waiting))
    (u #(update % :waiting?
                (fn [m] (not-empty (dissoc m k)))))))

(defn is-validation-passed?
  [{u :u :as props} input-key msgs-map bool server-error-k]
  (let [err-key (ffirst (select-keys msgs-map [server-error-k]))]
    (validation-waiting-for-server? props input-key false)
    (if bool
      (u #(update-in % [:server-errors input-key] dissoc err-key))
      (u #(assoc-in % [:server-errors input-key err-key] (err-key msgs-map))))))

(defn run-validation-server
  [{u :u :as props} schema]
  (doseq [[input-key cond-coll] schema
          [func msgs-map] cond-coll]
    (do
      (validation-waiting-for-server? props input-key true)
      (u #(update % :server-errors dissoc input-key))
      (func props
            (fn [bool server-error-k]
              (is-validation-passed? props input-key msgs-map bool server-error-k))))))

#_(defn validate-field
  [{:keys [s validation] :as props} side kind k]
  (let [values (:values s)]
    (case side
      :client (run-validation-client
               props
               (retrieve-target-and-general
                :client kind props
                (select-keys values [k])))
      :server (run-validation-server
               props
               (retrieve-target-and-general
                :server kind props
                (select-keys values [k])))
      (js/console.error
       (str "Only keys accepted are"
            " :client or :server")))))

(defn set-touched
  [m {u :u}]
  (u #(-> %
          (update :touched merge m)
          (assoc :on-blur m))))

(defn set-field-touched
  [k {u :u}]
  (u #(-> %
          (update-in [:touched k] true)
          (assoc-in [:on-blur k] true))))

(defn set-values
  [m {u :u}]
  (u #(-> %
          (update :values merge m)
          (assoc :on-change m)
          (assoc :on-blur m))))

(defn set-field-value
  [k v {u :u}]
  (u #(-> %
          (update-in [:values k] v)
          (assoc-in [:on-change k] true)
          (assoc-in [:on-blur k] true))))

(defn clear-state
  [{u :u}]
  (u nil))

(defn no-submit-on-enter
  [evt]
  (when (= (.-key evt) "Enter")
    (.preventDefault evt)))

(defn handle-change
  "API:
  Set the new input value."
  [evt {:keys [u s validation] :as props}]
  (let [k (-> evt .-target .-name)
        v (element-value evt)]
    (u #(-> %
            (assoc-in [:values k] v)
            (assoc-in [:on-change k] true)))))

(defn handle-blur
  "API:
  Set the input to touched."
  [evt {:keys [u validation] :as props}]
  (let [k (-> evt .-target .-name)]
    (u #(-> %
            (assoc-in [:touched k] true)
            (assoc-in [:on-blur k] true)))))

;; only for submit logic!!!

(defn touch-all
  "Set all inputs to touched true."
  [{s :s u :u}]
  (doseq [[k _] (:values s)]
    (u #(assoc-in % [:touched k] true))))

(defn validate-on-change
  [{:keys [u s validation] :as props}]
  (r/useEffect
   (fn []
     (let [on-change-ks (keys (:on-change s))]
       (when (and validation
                  (seq on-change-ks))
         (let [m (select-keys
                  (:values s)
                  on-change-ks)]
           (run-validation-client
            props
            (retrieve-target-and-general
             :client :on-change props m))
           (run-validation-server
            props
            (retrieve-target-and-general
             :server :on-change props m)))))
     (fn [] (u #(dissoc % :on-change))))
   #js [(:on-change s)]))

(defn validate-on-blur
  [{:keys [u s validation] :as props}]
  (r/useEffect
   (fn []
     (let [on-blur-ks (keys (:on-blur s))]
       (when (and validation
                  (seq on-blur-ks))
         (let [m (select-keys
                  (:values s)
                  on-blur-ks)]
           (run-validation-client
            props
            (retrieve-target-and-general
             :client :on-blur props m))
           (run-validation-server
            props
            (retrieve-target-and-general
             :server :on-blur props m)))))
     (fn [] (u #(dissoc % :on-blur))))
   #js [(:on-blur s)]))

(defn validate-form
  "Validate everything in the schema"
  [{:keys [u s validation] :as props}]
  (let [validation-resolve (validation (:values s))
        get-validation #(-> validation-resolve %1 %2)]
    (run-validation-client
     props
     (merge-with
      into
      (get-validation :client :on-blur)
      (get-validation :client :on-change)
      (get-validation :client :on-submit)))
    (run-validation-server
     props
     (merge-with
      into
      (get-validation :server :on-blur)
      (get-validation :server :on-change)
      (get-validation :server :on-submit)))))

(defn handle-submit
  [evt {u :u p :prevent-default? :as props}]
  (when p (.preventDefault evt))
  (set-submitting true props)
  (clear-on-submit-errors u)
  (touch-all props)
  (when (:validation props)
    (u #(assoc % :validation-exists? true))
    (validate-form props)))

(defn is-invalid?
  [s]
  (some some? (vals
               (merge-with
                into
                (:errors s)
                (:on-submit-errors s)
                (:server-errors s)))))

(defn handle-submit-func
  [on-submit {u :u s :s :as props}]
  (on-submit
   {:is-invalid? (is-invalid? s)
    :errors (merge-with
            into
            (:errors s)
            (:on-submit-errors s)
            (:server-errors s))
    :values (:values s)
    :dirty? (not= (:values s)
                  (:initial-values props))
    :set-on-submit-errors
    #(set-on-submit-errors % props)
    :set-touched
    #(set-touched % props)
    :set-values
    #(set-values % props)
    :set-submitting
    #(set-submitting % props)
    :clear-state
    #(clear-state props)}))

(defn submit-form
  [{:keys [u s validation] :as props}]
  (r/useEffect
     (fn [_]
       (when (:is-submitting? s)
         (cond
           (:validation-exists? s)
           (when
               (every? nil? (vals (:waiting? s)))
             (handle-submit-func
              (:on-submit props) props))
           :else
           (handle-submit-func
            (:on-submit props) props)))
       identity)
     #js [(:is-submitting? s)
          (:waiting? s)]))
