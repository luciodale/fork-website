(ns fork.reframe
  (:require
   [fork.reframe-logic :as reframe]
   [reagent.core :as r]
   [re-frame.core :as rf]))

(defn fork
  [props components]
  (let [state (r/atom {:values (:initial-values props)})]
    (r/create-class
     {:component-will-unmount
      (fn []
        (when (:clean-on-unmount? props)
          (rf/dispatch [::reframe/clean (:path props)])))
      :reagent-render
      (fn []
        (let [props (merge props {:state state
                                  :initial-values
                                  (:initial-values props)})
              db @(rf/subscribe [::reframe/db (:path props)])]
          [components
           [{:state state
             :values (:values @state)
             :errors (reframe/errors (:errors @state) (:external-errors db))
             :touched (:touched @state)
             :submitting? (:submitting? db)
             :submit-count (:submit-count db)
             :set-values #(reframe/set-values % props)
             :disable #(reframe/local-disable props %)
             :enable #(reframe/local-enable props %)
             :disabled? #(reframe/disabled? (:disabled? @state) (:disabled? db) %)
             :handle-change #(reframe/handle-change % props)
             :handle-blur #(reframe/handle-blur % props)
             :handle-submit #(reframe/handle-submit % props)}]]))})))

(defn on-submit
  "Interceptor"
  [path]
  (reframe/on-submit path))

(defn clean
  "Interceptor"
  [path & sub-path]
  (reframe/clean path sub-path))

(defn disable
  [db path & ks]
  (reframe/global-disable db path ks))

(defn enable
  [db path & ks]
  (reframe/global-enable db path ks))

(defn set-submitting
  [db path bool]
  (reframe/set-submitting db path bool))

(defn set-external-errors
  [db path errors-map]
  (reframe/set-external-errors db path errors-map))

(defn input
  [{:keys [values errors touched handle-change handle-blur]}
   {:keys [label placeholder name type class]}]
  [:div.field {:class class}
   [:label.label label]
   [:div.control
    [:input.input
     {:name name
      :placeholder placeholder
      :type type
      :value (values name "")
      :on-change handle-change
      :on-blur handle-blur}]]
   (when (get touched name)
     (for [[k msg] (get errors name)]
       ^{:key k}
       [:p.help msg]))])

(defn textarea
  [{:keys [values errors touched handle-change handle-blur]}
   {:keys [label placeholder name class]}]
  [:div.field {:class class}
   [:label.label label]
   [:div.control
    [:textarea.textarea
     {:name name
      :value (values name "")
      :placeholder placeholder
      :on-change handle-change
      :on-blur handle-blur}]]
   (when (get touched name)
     (for [[k msg] (get errors name)]
       ^{:key k}
       [:p.help msg]))])

(defn checkbox
  [{:keys [values errors touched handle-change handle-blur]}
   {:keys [name class text]}]
  [:div.field {:class class}
   [:div.control
    [:label.checkbox
     [:input
      {:name name
       :type "checkbox"
       :checked (values name false)
       :on-change handle-change
       :on-blur handle-blur}]
     " " text]]
   (when (get touched name)
     (for [[k msg] (get errors name)]
       ^{:key k}
       [:p.help msg]))])
