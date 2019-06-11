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

;; not in use
#_(defn form-nodes [props]
  (reduce
   (fn [coll node]
     (let [node-name (.-name node)
           node-val (.-value node)]
       (if-not (empty? node-name)
         (assoc coll node-name [node-name node-val])
         coll)))
   {}
   (try
     (.values
      js/Object
      (.-elements
       (js/document.getElementById (:id props))))
     (catch :default e
       (js/console.error
        e)))))

;; not in use
#_(defn unset-form-nodes
  [{u :u} form-nodes]
  (u #(assoc
       %
       :values
       (merge
        (reduce-kv
         (fn [coll k [ks v]]
           (if (empty? v)
             (assoc coll k v)
             coll))
         {}
         form-nodes)
        (-> % :values)))))

(defn set-submitting
  [bool {u :u}]
  (u #(assoc % :is-submitting? bool)))

(defn set-global-errors
  "Add errors coming from the external world.
  Those might be for example a bad server response.
  The errors are set under the key :global-errors and
  subsequently under the error message key itself.
  The global errors are dissoced from the state on each
  new submit."
  [errors {u :u}]
  (doseq [[k error] errors]
    (u #(assoc-in % [:global-errors k] error))))

(defn- clear-global-errors
  [u]
  (u #(dissoc % :global-errors)))

(defn run-validation
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

(defn validate-one
  [k values schema]
  (select-keys (schema values) [k]))

(defn validate-some
  [m values schema]
  (select-keys (schema values) (keys m)))

(defn validate-all
  [values schema]
  (schema values))

(defn validate-field
  [k {:keys [u s] [_ schema] :validation :as props}]
  (run-validation props (validate-one k (:values s) schema)))

(defn validate-form
  [{:keys [u s] [_ schema] :validation :as props}]
  (run-validation props (validate-all (:values s) schema)))

(defn set-touched
  [m {:keys [s u validation]
      [_ schema] :validation :as props}]
  (u #(update % :touched merge m))
  (when validation
    (run-validation props (validate-some m (:values s) schema))))

(defn set-field-touched
  [k {:keys [u s validation]
      [action schema] :validation :as props}]
  (u #(update-in % [:touched k] true))
  (when validation
    (run-validation props (validate-one k (:values s) schema))))

(defn set-values
  [m {:keys [u validation]
      [action schema] :validation :as props}]
  (u #(update % :values merge m))
  (when validation
    (run-validation props (validate-some m m schema))))

(defn set-field-value
  [k v {:keys [u validation]
          [action schema] :validation :as props}]
  (u #(update-in % [:values k] v))
  (when validation
    (run-validation props (validate-one k {k v} schema))))

(defn clear-state
  [{u :u}]
  (u nil))

(defn no-submit-on-enter
  [evt]
  (when (= (.-key evt) "Enter")
    (.preventDefault evt)))


(defn dispatch-validation
  []
  "get the schema for server, client and on-change, on-blur, on-submit")

(defn server-validation
  [k v props u]
  (u #(assoc-in % [:server-errors k] :resolving))
  ;; need of helper for this
  )

(defn http-request [values assoc-error?]
  ;; later in the callback
  (if true
    (assoc-error? true)
    (assoc-error? false))
  )

;; general validation rules:
;; - must be keyword
;; extract like this
(into {} (filter keyword {:a 1}))
;; must have unique keys so it's fine to convert to map

(defn validation [values]
  {:client
   {:on-change
    {"one"
     [[(= "hello" (get values "one")) {:whatever1 "one"}]]}
    :on-blur
    {"two"
     [[(= "hello" (get values "two")) {:whatever2 "two"}]]}
    :on-submit
    {"three"
     [[(= "hello" (get values "three")) {:whatever2 "three"}]]}}
   :server
   {:on-change
    {"four"
     [[http-request {:whatever1 "four"}]]}
    :on-blur
    {"five"
     [[(= "hello" (get values "five")) {:whatever2 "five"}]]}
    :on-submit
    {"six"
     [[(= "hello" (get values "six")) {:whatever2 "six"}]]}}})

(map :on-change (vals (validation {})))

(defn handle-change
  "API:
  Set the new input value.
  When the validation is fired on-change, run-validation
  is called with only the relevant input schema."
  [evt {:keys [u s validation]
        [action schema] :validation :as props}]
  (let [k (-> evt .-target .-name)
        v (element-value evt)]
    (u #(assoc-in % [:values k] v))
    (when (= action :on-change)
      (run-validation props (validate-one k {k v} schema)))))

(defn handle-blur
  "API:
  Set the input to touched.
  Run the validation when :on-blur is true"
  [evt {:keys [s u validation] [action schema] :validation :as props}]
  (let [k (-> evt .-target .-name)
        v (element-value evt)]
    (u #(assoc-in % [:touched k] true))
    (when validation
      (run-validation props (validate-one k {k v} schema)))))

(defn touch-all
  "Set all inputs to touched true.
  The input names are retrieved from the form node
  rather than the state because they might not have
  been set yet on submit."
  [{s :s u :u :as props}]
  ;; when the state gets more complicated with +add elements
  ;; traverse the whole map with something like walk
  (doseq [[k _] (:values s)]
    (u #(assoc-in % [:touched k] true))))

(defn handle-submit
  [evt on-submit {u :u s :s :as props}]
  (set-submitting true props)
  (touch-all props)
  (clear-global-errors u)
  ;; validate form sync
  (when (:validation props)
    (validate-form props))
  (on-submit
   evt
   {:is-invalid? (some some? (vals (:errors s)))
    :values (:values s)
    :dirty? (not= (:values s) (:initial-values props))
    :set-global-errors
    #(set-global-errors % props)
    :set-touched
    #(set-touched % props)
    :set-values
    #(set-values % props)
    :set-submitting
    #(set-submitting % props)
    :clear-state
    #(clear-state props)}))
