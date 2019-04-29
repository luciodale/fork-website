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

(defn fork
  [& [{:keys [on-submit]}]]
  (let [values (useLens state identity)]
    {:handle-change
     (logic/handle-change state)
     :values values}))
