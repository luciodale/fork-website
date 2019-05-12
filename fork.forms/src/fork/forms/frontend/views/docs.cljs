(ns fork.forms.frontend.views.docs
  (:require-macros
   [fork.forms.frontend.hicada :refer [html]])
  (:require
   [react :as r]))

(defn f [u s]
  (fn [evt]
    (u #(assoc % :a 3))
    (reduce
     (fn [_ [k v]]
       (if-not (coll? v)
         (u #(assoc % k v))
         (doseq [a v]
           (u #(assoc % a a)))))
     nil
     {:aaa 3 :bvb [:zz :bb] :ccc [:zzz :bbb]})))

(defn view []
  (html
   [:*
    (let [[state update-state] (r/useState {})
          u update-state]
      (prn state)
      [:div
       [:input
        {:on-change (f u state)}]
       (:a state)])]))
