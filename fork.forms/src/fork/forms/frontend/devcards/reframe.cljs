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

(defn validation [values]
  {:client
   {:on-change
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
             (values "list")))}}})

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

(defn simple
  [{:keys [handle-change handle-blur add delete values input-key errors touched]}]
  [:div(map
        (fn [[idx value]]
          ^{:key idx}
          [:div
           [:div
            [:input.input
             {:value value
              :on-change #(handle-change % idx)
              :on-blur #(handle-blur % idx)}]
            (let [error (get-in errors [input-key idx])]
              (when (and (or (get-in touched [input-key idx]) (true? (get touched input-key)))
                         error)
                [:p.help error]))]
           [:button.button
            {:on-click #(delete % idx)}
            "remove -"]])
        (get values input-key))
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
                 state] :as props}]
      [:div
       [:div "state: "
        [pprint-code @state]]
       [:form
        {:on-submit handle-submit}
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
        #_[f/input-array props
         {:name "simple"
          :component simple}]
        #_[f/checkbox props
         {:name "new"
          :text "yo braa"}]
        [:button.button
         {:type "submit"}
         "click me "]
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
