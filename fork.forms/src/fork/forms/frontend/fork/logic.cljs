(ns fork.logic
  (:require
   [clojure.string :as string]
   [react :as r]))

(defn element-name
  [evt]
  "converts the html name attribute
   from string to keyword, while trimming
   any ending or intermediate white space"
  (-> (transduce
       (comp
        (remove string/blank?))
       str
       (-> evt .-target .-name))
      (keyword)))

(defn element-value
  [evt]
  (let [type (-> evt .-target .-type)]
    (case type
      "checkbox"
      (-> evt .-target .-checked)
      (-> evt .-target .-value))))

(defn handle-change
  [fork-state]
  (fn [evt]
    (swap! fork-state assoc
           (element-name evt)
           (element-value evt))))
