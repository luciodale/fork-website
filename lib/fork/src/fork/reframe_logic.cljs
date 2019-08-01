(ns fork.reframe-logic
  (:require
   [re-frame.core :as rf]))

(defn element-value
  [evt]
  (let [type (-> evt .-target .-type)]
    (case type
      "checkbox"
      (-> evt .-target .-checked)
      (-> evt .-target .-value))))

(defn generate-error-map
  [resolved-validation]
  (zipmap (keys resolved-validation)
          (map
           (fn [vec-validation]
             (apply merge
                    (keep
                     (fn [[bool k msg]]
                       (when-not bool {k msg}))
                     vec-validation)))
           (vals resolved-validation))))

(defn validation->error-map
  ([resolved-validation]
   (generate-error-map resolved-validation))
  ([resolved-validation keys-seq]
   (->> resolved-validation
        (filter (fn [[validation-key _]]
                  (or (keyword? validation-key)
                      (some #(= validation-key %) keys-seq))))
        (into {})
        (generate-error-map))))

(defmulti validate (fn [[location selector] _] [location selector]))

(defmethod validate [:client :on-change]
  [_ {:keys [state validation input-key input-value]}]
  (-> (assoc (:values @state) input-key input-value)
           validation :client :on-change
           (validation->error-map (vector input-key))))

(defmethod validate [:client :on-blur]
  [_ {:keys [state validation input-key input-value]}]
  (-> (assoc (:values @state) input-key input-value)
      validation :client :on-blur
      (validation->error-map (vector input-key))))

(defmethod validate [:client :on-submit]
  [_ {:keys [state validation]}]
  (let [resolved-validation (-> (:values @state) validation :client)
        validation-on-change (:on-change resolved-validation)
        validation-on-blur (:on-blur resolved-validation)
        validation-on-submit (:on-submit resolved-validation)
        whole-validation (merge-with
                          ;; replace with into with next cljs release
                          #(reduce conj %1 %2)
                          validation-on-change
                          validation-on-blur
                          validation-on-submit)]
    (validation->error-map whole-validation)))

(defn errors
  [errors & [external-errors]]
  (not-empty (into {} (filter second
                              (merge-with merge
                                          errors external-errors)))))

(defn set-values
  [new-values {:keys [state]}]
  (swap! state update :values merge new-values))

(defn disable-logic
  [current-set ks]
  (apply conj ((fnil into #{}) current-set) ks))

(defn enable-logic
  [current-set ks]
  (apply disj current-set ks))

(defn local-disable
  [{state :state} & ks]
  (swap! state update :disabled? #(disable-logic % ks)))

(defn local-enable
  [{state :state} & ks]
  (swap! state update :disabled? #(enable-logic % ks)))

(defn global-disable
  [db path & [ks]]
  (update-in db [path :disabled?] #(disable-logic % ks)))

(defn global-enable
  [db path & [ks]]
  (update-in db [path :disabled?] #(enable-logic % ks)))

(defn disabled?
  [local global k]
  (get (clojure.set/union local global) k))

(defn handle-change
  [evt {:keys [state validation] :as props}]
  (let [input-key (-> evt .-target .-name)
        input-value (element-value evt)]
    (if validation
      (let [error-map (validate [:client :on-change]
                                {:state state
                                 :validation validation
                                 :input-key input-key
                                 :input-value input-value})]
        (swap! state #(-> %
                          (assoc-in [:values input-key] input-value)
                          (update :errors merge error-map))))
      (swap! state assoc-in [:values input-key] input-value))))

(defn handle-blur
  [evt {:keys [state validation] :as props}]
  (let [input-key (-> evt .-target .-name)
        input-value (element-value evt)]
    (if validation
      (let [error-map (validate [:client :on-blur]
                                {:state state
                                 :validation validation
                                 :input-key input-key
                                 :input-value input-value})]
        (swap! state #(-> %
                          (assoc-in [:touched input-key] true)
                          (update :errors merge error-map))))
      (swap! state assoc-in [:touched input-key] true))))

(defn touch-all
  [state]
  (assoc state :touched
         (let [input-names (keys (:values state))]
           (zipmap input-names
                   (take (count input-names)
                         (repeat true))))))

(defn handle-submit
  [evt {:keys [state on-submit validation
               prevent-default? initial-values]}]
  (when prevent-default? (.preventDefault evt))
  (if validation
    (let [error-map (validate [:client :on-submit]
                              {:state state
                               :validation validation})]
      (swap! state #(-> %
                        (touch-all)
                        (update :errors merge error-map))))
    (swap! state #(touch-all %)))
  (on-submit
   {:errors (errors (:errors @state))
    :values (:values @state)
    :dirty? (not= (:values @state) initial-values)}))

(defn on-submit [path]
  (rf/->interceptor
   :id :on-submit
   :before (fn [context]
             (-> context
                 (assoc-in [:coeffects :db path :submitting?] true)
                 (update-in [:coeffects :db path :submit-count] inc)
                 (update-in [:coeffects :db path] dissoc :external-errors)))))

(defn clean [path & [sub-path]]
  (rf/->interceptor
   :id :clean
   :after (fn [context]
            (if sub-path
              (update-in context (concat [:effects :db path]
                                         (butlast sub-path)) dissoc (last sub-path))
              (update-in context [:effects :db] dissoc path)))))

(rf/reg-sub
 ::db
 (fn [db [_ path]]
   (get db path)))

(rf/reg-event-db
 ::clean
 (fn [db [_ path]]
   (dissoc db path)))

(defn set-submitting
  [db path bool]
  (assoc-in db [path :submitting?] bool))

(defn set-external-errors
  [db path errors-map]
  (update-in db [path :external-errors] merge errors-map))
