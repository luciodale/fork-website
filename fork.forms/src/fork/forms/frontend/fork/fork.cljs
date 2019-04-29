(ns fork.fork
  (:require
   [fork.logic :as logic]
   [react :as r]))

(defonce fork-state (atom nil))

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
  [& [{:keys [on-submit]}]]
  (let [values (useLens fork-state identity)]
    {:handle-change
     (logic/handle-change fork-state)
     :handle-on-submit
     #(on-submit % values)
     :values values}))
