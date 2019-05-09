(ns fork.fork
  (:require
   [fork.logic :as logic]
   [react :as r]))

(defonce state (atom nil))

(defn- useLens
  [a f]
  (let [[value update-value] (r/useState (f @a))]
    (r/useEffect
     (fn []
       (let [k (gensym "useLens")]
         (add-watch a k
                    (fn [_ _ _ new-state]
                      (update-value (f new-state))))
         (fn []
           (remove-watch a k)))))
    value))

(defn fork-form
  [{:keys [on-submit]}]
  (let [deref-state (useLens state identity)
        values (:values deref-state)
        is-submitting? (:is-submitting? deref-state)
        ]
    (prn deref-state)
    {:values values
     :errors nil
     :touched nil
     :is-submitting? is-submitting?
     :handle-change
     (logic/handle-change state)
     :handle-on-submit
     (fn [evt]
       (logic/set-submitting state true)
       (on-submit
        evt values
        (fn [bool] (logic/set-submitting state bool))))}))
