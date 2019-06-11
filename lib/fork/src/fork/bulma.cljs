(ns fork.bulma
  (:require
   [react :as r]))

(defn field-errors
  [name touched errors]
  (when (and (name touched) (name errors))
    (for [[k error] (name errors)]
      (r/createElement
       "p" #js {:className "help" :key k}
       error))))

(defn field
  [{{:keys [label name type]} :user
    {:keys [values touched errors handle-change handle-blur]} :handlers}]
  (r/createElement
   "div" #js {:className "field"}
   (r/createElement
    "label" #js {:className "label"} label)
   (r/createElement
    "div"  #js {:className "control"}
    (r/createElement
     "input" #js {:className "input"
                  :name name
                  :onChange handle-change
                  :onBlur handle-blur
                  :value (name values "")
                  :type type}))
   (field-errors name touched errors)))

(defn framework
    [handlers]
  {:field #(field {:user % :handlers handlers})})



#_(defn pretty-input
  [{:keys [id type label icon-left icon-right-success icon-right-danger]}
   {{:keys [values touched errors handle-change handle-blur]} :props}]
  [:div.field
   [:label.label label]
   [:div.control.has-icons-left.has-icons-right
    [:input.input
     {:name id
      :type type
      :value (id values "")
      :on-change handle-change
      :on-blur handle-blur}]
    [:span.icon.is-small.is-left
     [:i {:class (str "fas " icon-left)}]]
    [:span.icon.is-small.is-right
     [:i {:class (cond
                   (and (id touched)
                        (id errors)) icon-right-danger
                   (id touched) icon-right-success
                   :else nil)}]]]
   (when (and (id touched) (id errors))
     (for [error (vals (id errors))]
       ^{:key error}
       [:p.help
        error]))])
