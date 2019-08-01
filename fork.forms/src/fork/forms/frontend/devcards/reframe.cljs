(ns fork.forms.frontend.devcards.reframe
  (:require
   [re-frame.core :as rf]
   [fork.reframe :as f]
   [reagent.core :as reagent])
  (:require-macros
   [devcards.core :as dc :refer [defcard defcard-doc]]))

(rf/reg-sub
 :db
 (fn [db _]
   db))

(rf/reg-event-fx
 :submit
 [(f/on-submit :path)]
 (fn [{db :db} [_ props]]
   (if (:errors props)
     {:db (-> db
              (f/set-submitting :path false)
              (f/set-external-errors :path {:server "porco il boia ladrone"}))}
     {:db db
      :dispatch-later [{:ms 1000 :dispatch [:http-response]}]})))

(rf/reg-event-db
 :http-response
 (fn [db _]
   db))

(defn validation [values]
  {:client
   {:on-change
    {"one"
     [[(= (values "one") "heyy") :a "yo"]
      [(= (values "one") "heyy") :b "dkk"]]}}})

(defn testing []
  [:div
   [f/fork {:initial-values {"one" "hello"
                             "two" "dont"
                             "new" false}
            :validation validation
            :prevent-default? true
            :clean-on-unmount? true
            :path :path
            :on-submit #(rf/dispatch [:submit %])}
    (fn [[{:keys [values errors submitting?
                  submit-count handle-change
                  handle-blur handle-submit
                  state] :as props}]]
      [:div
       [:p "reframe state: " @(rf/subscribe [:db])]
       [:p "reframe form!!"]
       [:p "value: " values]
       [:form
        {:on-submit handle-submit}
        [f/input props
         {:name "one"
          :label "Hello"
          :placeholder "whatever"
          :type "text"
          :class "foo"}]
        [f/textarea props
         {:name "two"
          :label "Hello"
          :placeholder "whatever"
          :class "foo"}]
        [f/checkbox props
         {:name "new"
          :text "yo braa"}]
        [:button.button
         {:type "submit"}
         "click me bitch"]
        (when-let [err (get errors :server)]
          [:div err])]])]])

(defcard personal-profile-test
  ""
  (dc/reagent
   [testing]))
