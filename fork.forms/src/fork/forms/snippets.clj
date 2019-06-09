(ns fork.forms.snippets
  (:require
   [yada.yada :as yada]
   [integrant.core :as ig]))

(defn manage-blank
  [coll]
  (if (= 2 (-> coll :double-space inc))
    (-> coll
        (update :data conj nil)
        (assoc :double-space 0)
        (update :index inc))
    (update coll :double-space inc)))

(defn reset-double-space [coll]
  (assoc coll :double-space 0))

(defn attach-str [coll this]
  (update coll :data
          (fn [data]
            (update data (:index coll)
                    #(str % this "\n")))))

(defn parse-snippets
  [snippets]
  (with-open [file (clojure.java.io/reader snippets)]
    (:data
     (reduce
      (fn [{:keys [double-space data] :as coll} this]
        (cond-> coll
          (empty? this) manage-blank
          (not-empty this) reset-double-space
          true (attach-str this)))
      {:double-space 0
       :data []
       :index 0}
      (line-seq file)))))

(defmethod ig/init-key ::docs
  [_ snippets]
  (yada/resource
   {:id ::snippets
    :methods {:get
              {:produces ["application/transit+json"]
               :response (fn [ctx]
                           (parse-snippets snippets))}}}))
