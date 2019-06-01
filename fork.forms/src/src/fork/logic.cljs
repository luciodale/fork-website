(ns fork.logic
  (:require
   [clojure.string :as s]
   [react :as r]))

(defn element-name
  "Convert input name from string to keyoword.
  If the inputs are generated in a loop, js reads
  the dynamic input name as ':input', hence the split fn
  is used to make the convertion always consistent"
  [evt]
  (keyword
   (last (s/split (-> evt .-target .-name) ":"))))

(defn element-value
  [evt]
  (let [type (-> evt .-target .-type)]
    (case type
      "checkbox"
      (-> evt .-target .-checked)
      (-> evt .-target .-value))))

(defn nodes-name [props]
  (reduce
   (fn [coll node]
     (let [node-name (.-name node)]
       (if-not (empty? node-name)
         (assoc coll (keyword node-name) node-name)
         coll)))
   {}
   (.values
    js/Object
    (.-elements
     (js/document.getElementById (:form-id props))))))

(defn set-submitting
  [u bool]
  (u #(assoc % :is-submitting? bool)))

(defn set-global-errors
  "Add errors coming from the external world.
  Those might be for example a bad server response.
  The errors are set under the key :global-errors and
  subsequently under the error message key itself.
  The global errors are dissoced from the state on each
  new submit."
  [u errors]
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
  [{s :s u :u} schema]
  (doseq [[input-key cond-coll] schema
          [bool msg] cond-coll
          :let [err-key (ffirst msg)]]
    (cond
      bool
      (u #(update-in % [:errors input-key]
                     (fn [m] (not-empty (dissoc m err-key)))))
      :else
      (u #(assoc-in % [:errors input-key err-key] (err-key msg))))))

(defn set-touched
  [u m]
  (u #(update % :touched merge m)))

(defn set-values
  [u {[action schema] :validation :as props} m]
  (u #(update % :values merge m))
  (when (= action :on-change)
    (doseq [[k v] m]
      (run-validation props {k (k (schema {k v}))}))))

(defn clear-state
  [{u :u}]
  (u nil))

(defn no-submit-on-enter
  [evt]
  (when (= (.-key evt) "Enter")
    (.preventDefault evt)))

;; Run this shit whenever the number of form elements change!!!
(defn effect-run-validation
  "Run validation on component-did-mount.
  It ensures the schema is validated when submit
  is fired without touching any input"
  [props evaluated-schema]
  (r/useEffect
   (fn []
     (run-validation props evaluated-schema)
     identity)
   #js []))

(defn handle-change
  "API:
  Set the new input value.
  When the validation is fired on-change, run-validation
  is called with only the relevant input schema."
  [evt {u :u [action schema] :validation :as props}]
  (let [k (element-name evt)
        v (element-value evt)]
    (u #(assoc-in % [:values k] v))
    (when (= action :on-change)
      (run-validation props {k (k (schema {k v}))}))))

(defn handle-blur
  "API:
  Set the input to touched.
  Run the validation when :on-blur is true"
  [evt {u :u [action schema] :validation :as props}]
  (let [k (element-name evt)
        v (element-value evt)]
    (u #(assoc-in % [:touched k] true))
    (when (= action :on-blur)
      (run-validation props {k (k (schema {k v}))}))))

(defn touch-all
  "Set all inputs to touched true.
  The input names are retrieved from the form node
  rather than the state because they might not have
  been set yet on submit."
  [props u]
  (doseq [[k _] (nodes-name props)]
    (u #(assoc-in % [:touched k] true))))

(defn handle-on-submit
  [evt on-submit {u :u s :s :as props}]
  (set-submitting u true)
  (touch-all props u)
  (clear-global-errors u)
  (on-submit
   evt
   {:errors? (some some? (vals (:errors s)))
    :values (:values s)
    :dirty? nil ;TODO
    :set-global-errors
    #(set-global-errors u %)
    :set-touched
    #(set-touched u %)
    :set-values
    #(set-values u props %)
    :set-submitting
    #(set-submitting u %)
    :clear-state
    #(clear-state {:u u})}))


(comment
  "- set-global-error to allow a server error
   - don't add it in :errors but create new key
   - remember to clean :global-errors at each submit"

  "Consider mapping over the form nodes in the initial
  effect on did mount rather than in submit. Nope.
  If some form element is generated on the fly, they
  won't be picked up on component-did-mount")
