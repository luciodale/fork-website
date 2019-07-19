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
  The errors are written in :on-submit-errors and
  merged into :errors when provided to the user.
  These errors are dissoced from the state on each
  new submit."
  [errors {u :u}]
  (doseq [[k error] errors]
    (u #(assoc-in % [:on-submit-errors k] error))))

(defn clear-on-submit-errors
  [u]
  (u #(dissoc % :on-submit-errors)))

(defn errors
  "Merges client, server, and global errors into one map"
  [s]
  (into {}
        (map
         (fn [[k v]]
           (if (empty? v)
             {k nil} {k v}))
         (merge-with
          into
          (:errors s)
          (:on-submit-errors s)
          (:server-errors s)))))

(defn is-invalid?
  "Check if form is valid by looping on each input key"
  [s]
  (some some? (vals (errors s))))

(defn retrieve-target-and-general
  "Retrieve a map subset of the validation map. The chosen subset depends upon
  :client/server and :on-change/:on-blur/:on-submit params. This function returns
  the values that will be passed to the user's created validation function"
  [side kind {:keys [validation s]} target-vals]
  (->>
   (-> (validation (:values s)) side kind)
   (filter (fn [[k _]]
             (or (keyword? k)
                 (some (fn [[target-k _]]
                         (= k target-k)) target-vals))))
   (into {})))

(defn run-validation-client
  "schema: {'one' [[true {:er1 'error 1'}
                  [true {:er2 'error 2'}]]]
            'two' [[false {:er1 'error 1'}]]
  Loop over the input keys and nested vectors to update
  the :errors and :errors-key keys in the state. A true value implies that
  the input is error free. Error keys must be unique per input key"
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
  "Allow server side validation to be completed before submitting form"
  [{u :u} k bool]
  (if bool
    (u #(assoc-in % [:waiting? k] :waiting))
    (u #(update % :waiting?
                (fn [m] (not-empty (dissoc m k)))))))

(defn is-validation-passed?
  "Resolve server side validation. The user provides a true/false
  and :error-msg-key declared in the validation map. Since this
  function is called in an http hanlder, the :waiting? key is set to false"
  [{u :u :as props} input-key msgs-map bool server-error-k]
  (let [err-key (ffirst (select-keys msgs-map [server-error-k]))]
    (if bool
      (u #(update-in % [:server-errors input-key]
                     (fn [m] (not-empty (dissoc m err-key)))))
      (u #(assoc-in % [:server-errors input-key err-key] (err-key msgs-map))))
    (validation-waiting-for-server? props input-key false)))


(defn run-validation-server
  "Run user's declared functions for server side validation"
  [{u :u s :s :as props} schema]
  (doseq [[input-key cond-coll] schema
          [func msgs-map] cond-coll]
    (do
      (validation-waiting-for-server? props input-key true)
      (func {:values (:values s)
             :errors (errors s)
             :set-waiting-for-server
             (fn [k bool]
               (validation-waiting-for-server? props k bool))
             :is-validation-passed?
             (fn [bool server-error-k]
               (is-validation-passed? props input-key msgs-map bool server-error-k))}))))

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

(defn no-submit-on-enter
  [evt]
  (when (= (.-key evt) "Enter")
    (.preventDefault evt)))

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

(defn clear-input-errors
  "Remove errors and server-errors for specified inputs.
  i.e. (clear-input-errors name-one name-two name-three)"
  [{u :u} & name-list]
  (let [remove-err (fn [state place k]
                     (update-in state [place]
                                (fn [m] (not-empty (dissoc m k)))))]
    (doseq [one-name name-list]
      (u #(-> %
              (remove-err :errors one-name)
              (remove-err :server-errors one-name))))))

(defn touch-all
  "Set all inputs to touched true."
  [{s :s u :u}]
  (doseq [[k _] (:values s)]
    (u #(assoc-in % [:touched k] true))))

(defn validate-client
  "Effect that runs every time the passed state-k in state changes.
  It is used for the :on-blur, :on-change, and :on-submit validation"
  [{:keys [u s validation] :as props} state-k val-k]
  (r/useEffect
   (fn []
     (let [ks (keys (state-k s))]
       (when (and validation
                  (seq ks))
         (let [m (select-keys
                  (:values s) ks)]
           (run-validation-client
            props
            (retrieve-target-and-general
             :client val-k props m)))))
     (fn []
       (u #(-> %
               (dissoc state-k)
               (assoc :client-cleared true)))))
   #js [(state-k s)]))

(defn validate-server
  "Effect that runs only after the :client-cleared key becomes true"
  [{:keys [u s validation] :as props} state-k val-k]
  (r/useEffect
   (fn []
     (let [ks (keys (state-k s))]
       (when (and validation
                  (seq ks)
                  (:client-cleared s))
         (let [m (select-keys
                  (:values s) ks)]
           (run-validation-server
            props
            (retrieve-target-and-general
             :server val-k props m)))))
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
                  (:is-submitting? s))
         (run-validation-client
          props
          (merge-with
           into
           (get-validation :client :on-blur)
           (get-validation :client :on-change)
           (get-validation :client :on-submit)))))
     (fn [] (u #(assoc % :client-cleared-submit true))))
   #js [(:is-submitting? s)
        (:validation-exists? s)]))

(defn validate-server-submit
  [{:keys [u s validation] :as props}]
  (r/useEffect
   (fn []
     (let [validation-resolve (validation (:values s))
           get-validation #(-> validation-resolve %1 %2)]
       (when (and (:validation-exists? s)
                  (:is-submitting? s)
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
  [evt {u :u p :prevent-default? :as props}]
  (when p (.preventDefault evt))
  (set-submitting true props)
  (clear-on-submit-errors u)
  (touch-all props)
  (when (:validation props)
    (u #(assoc % :validation-exists? true))))

(defn handle-submit-func
  [on-submit {:keys [s u] :as props}]
  (u #(update % :submit-count inc))
  (on-submit
   {:is-invalid? (is-invalid? s)
    :errors (errors s)
    :submit-count (:submit-count s)
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
  [{:keys [u s validation] :as props}]
  (r/useEffect
   (fn []
     (when (:is-submitting? s)
         (cond
           (:validation-exists? s)
           (when
               (and (every? nil? (vals (:waiting? s)))
                    (:on-submit-ready s))
             (handle-submit-func
              (:on-submit props) props))
           :else
           (handle-submit-func
            (:on-submit props) props)))
     (fn [] (u #(dissoc % :can-submit :on-submit-ready))))
   #js [(:can-submit s)]))
