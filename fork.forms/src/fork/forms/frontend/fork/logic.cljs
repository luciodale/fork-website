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

(defn set-submitting
  [u bool]
  (u #(assoc % :is-submitting? bool)))

(defn run-validation
  "schema: {:one [[true 'error msg'][true 'error 2']]
            :two [[false 'error msg']]}
  Loop over the input keys and nested vectors to update
  the :errors key in the state. A true value implies that
  the input is error free."
  [u schema]
  (doseq [[input-key cond-coll] schema
          [bool msg] cond-coll]
    (if bool
      (u #(update-in % [:errors input-key]
                     (fn [ers] (not-empty (disj ers msg)))))
      (u #(update-in % [:errors input-key]
                     (fnil conj #{}) msg)))))

(defn set-touched
  [u m]
  (u #(update % :touched merge m)))

(defn set-values
  [u {[action schema] :validation} m]
  (u #(update % :values merge m))
  (when (= action :on-change)
    (doseq [[k v] m]
      (run-validation u {k (k (schema {k v}))}))))

(defn values
  "API:
  Retrieve one input or all input values.
  If nil is observed when retrieving one input,
  '' is returned instead. (js complains about nil input values)"
  [state input]
  (cond
    input
    ((fnil identity "")
     (-> state :values input))
    :else
    (-> state :values)))

(defn clear-state
  [{u :u}]
  (u nil))

(defn no-submit-on-enter
  [evt]
  (when (= (.-key evt) "Enter")
    (.preventDefault evt)))


(defn effect-run-validation
  "Run validation on component-did-mount.
  It ensures the schema is validated when submit
  is fired without touching any input"
  [u evaluated-schema]
  (r/useEffect
   (fn []
     (run-validation u evaluated-schema)
     identity)
   #js []))

(defn handle-change
  "API:
  Set the new input value.
  When the validation is fired on-change, run-validation
  is called with only the relevant input schema."
  [evt {u :u [action schema] :validation}]
  (let [k (element-name evt)
        v (element-value evt)]
    (u #(assoc-in % [:values k] v))
    (when (= action :on-change)
      (run-validation u {k (k (schema {k v}))}))))

(defn handle-blur
  "API:
  Set the input to touched.
  Run the validation when :on-blur is true"
  [evt {u :u [action schema] :validation}]
  (let [k (element-name evt)
        v (element-value evt)]
    (u #(assoc-in % [:touched k] true))
    (when (= action :on-blur)
      (run-validation u {k (k (schema {k v}))}))))

(defn touch-all
  "Set all inputs to touched true.
  The input names are retrieved from the form node
  rather than the state because they might not have
  been set yet on submit."
  [evt u]
  (let [input-nodes (.values
                     js/Object
                     (-> evt .-target .-elements))
        input-keys (mapv #(.-name %) input-nodes)]
    (doseq [k (remove empty? input-keys)]
      (u #(assoc-in % [:touched (keyword k)] true)))))

(defn handle-on-submit
  [evt on-submit {u :u s :s :as props}]
  (set-submitting u true)
  (touch-all evt u)
  (on-submit
   evt
   {:errors? (some some? (vals (:errors s)))
    :values (:values s)
    :dirty? nil ;TODO
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
   - remember to clean :global-errors at each submit")
