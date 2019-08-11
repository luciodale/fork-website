(ns fork.forms.frontend.devcards.reframe
  (:require
   [devcards.core :as dc]
   [fork.reframe :as f]
   [fork.fork :as hooks]
   [re-frame.core :as rf]
   [cljs.pprint :refer [pprint]]
   [reagent.core :as r]
   [react :as react])
  (:require-macros
   [fork.forms.frontend.hicada :refer [html]]
   [devcards.core :refer [defcard defcard-doc]]))

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
              (f/set-external-errors :path {:server "error"}))}
     {:db db
      :dispatch-later [{:ms 1000 :dispatch [:http-response]}]})))

(rf/reg-event-db
 :http-response
 (fn [db _]
   db))

(rf/reg-event-db
 :mock-response
 (fn [db [_ value]]
   (prn value)
   (if (= value "hello")
     (-> db
         (f/set-waiting :path "list" false)
         (f/assoc-server-errors :path ["list" "key"] "message per dio"))
     (-> db
         (f/set-waiting :path "list" false)
         (f/dissoc-server-errors :path ["list" "key"])))))

(rf/reg-event-fx
 :server-validation
 (fn [{db :db} [_ {:keys [input errors values path] :as props}]]
   {:db (assoc-in db [path :waiting? input] true)
    :dispatch [:mock-response (get-in values ["list" 0 "foo"])]}))

(defn validation [values]
  {:client {}
   #_{:on-change
    {"one"
     [[(= (values "one") "heyy") :a "yo"]
      [(= (values "one") "heyy") :b "dkk"]]
     "list"
     (apply concat
            (map
             (fn [[idx {:strs [foo bar]}]]
               [[(not (empty? foo)) (str "foo" idx) "Foo can't be empty"]
                [(= "aia" foo) (str "foo1" idx) "aia can't be empty"]
                [(not (empty? bar)) (str "bar" idx) "Bar can't be empty"]])
             (values "list")))}}
   :server
   {:on-change
    {"list" #(rf/dispatch [:server-validation %])}}})

(defn multiple
  [{:keys [handle-change handle-blur
           input-array-errors
           add delete values array-key] :as props}
   {:keys [one two]}]
  [:div
   (map
    (fn [[idx value]]
      ^{:key idx}
      [:div
       [:div.field
        [:label.label one]
        [:input.input
         {:name one
          :value (value one)
          :on-change #(handle-change % idx)
          :on-blur #(handle-blur % idx)}]
        (for [[k error] (input-array-errors idx one [(str one idx) (str "foo1" idx)])]
          ^{:key k}
          [:div
           [:p.help error]])]
       [:div.field
        [:label.label two]
        [:input.input
         {:name two
          :value (value two)
          :on-change #(handle-change % idx)
          :on-blur #(handle-blur % idx)}]
        (for [[k error] (input-array-errors idx two [(str two idx)])]
          ^{:key k}
          [:div
           [:p.help error]])]
       [:button.button
        {:on-click #(delete % idx [(str one idx) (str "foo1" idx) (str two idx)])}
        "remove -"]])
    (get values array-key))
   [:button.button
    {:on-click add}
    "add +"]])

(defn pprint-str
  [x]
  (with-out-str (pprint x)))

(defn pprint-code
  [x]
  [:code
   {:style {:text-align "left"}}
   [:pre (pprint-str x)]])

(defn testing []
  [:div
   [f/fork {:initial-values {"one" "hello"
                             "two" "dont"
                             "new" false
                             "list" {0 {"foo" ""
                                        "bar" ""}}}
            :validation validation
            :prevent-default? true
            :clean-on-unmount? true
            :path :path
            :on-submit #(rf/dispatch [:submit %])}
    (fn [{:keys [values errors submitting?
                 submit-count handle-change
                 handle-blur handle-submit
                 waiting? state] :as props}]
      [:div
       [:div "local: "
        [pprint-code @state]]
       [:div "global: "
        [pprint-code @(rf/subscribe [:db])]]
       [:form
        {:on-submit handle-submit}
        (prn errors)
        #_[f/input props
         {:name "one"
          :label "Hello"
          :placeholder "whatever"
          :type "text"
          :class "foo"}]
        #_[f/textarea props
         {:name "two"
          :label "Hello"
          :placeholder "whatever"
          :class "foo"}]
        [f/input-array props
         {:name "list"
          :component multiple
          :args {:one "foo"
                 :two "bar"}}]
        #_[f/checkbox props
         {:name "new"
          :text "yo braa"}]
        [:button.button
         {:type "submit"
          :disabled (or waiting?
                        submitting?
                        errors)}
         "click me"]
        (when-let [err (get errors :server)]
          [:div err])]])]])

(defcard personal-profile-test
  (dc/reagent
   [testing]))

(defn foo []
  )

(defcard reagent-with-hooks
  (dc/reagent
   [foo]))
