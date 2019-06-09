(ns fork.forms.frontend.views.docs-render-fork
  (:require-macros
   [fork.forms.frontend.hicada :refer [html]])
  (:require
   [fork.fork :as fork]
   [react :as r]))

(defn s
  [strings & [padd]]
  (html
   [:strong.el
    {:class
     (case padd
       :no-left "el-no-left"
       :no-right "el-no-right"
       :no "el-no"
       nil)}
    strings]))

(defn i
  [strings & [padd]]
  (html
   [:i.el
    {:class
     (case padd
       :no-left "el-no-left"
       :no-right "el-no-right"
       :no "el-no"
       nil)}
    strings]))

(defn num
  [word page-num & [padd]]
  (html
   [:span.el
    {:class
     (case padd
        :no-left "el-no-left"
        :no-right "el-no-right"
        :no "el-no"
        nil)}
    word
    [:span.page-num
     page-num]]))

(defn c
  [code & [padd]]
  (html
   [:code.pre-inline
    {:class
     (case padd
       :no-left "pre-inline-no-left"
       :no-right "pre-inline-no-right"
       :no "pre-inline-no"
       nil)}
    code]))

(defn description-0-0 []
  (html
   [:div.docs__content__fragment
    [:p "First, require" (s "fork") "in your namespace."]]))

(defn description-0-1 []
  (html
   [:div
    [:div.docs__content__fragment
     [:p "Then, call" (c "fork-form") "inside your component function"
      " and destructure the following keys:"
      (c "values" :no-right) ","
      (c "handle-change" :no-right) ", and"
      (c "handle-blur" :no-right) "."]]
    [:div.docs__content__fragment
     [:p "After that, write a basic HTML input element and use the destructured keys"
      " just like in the following snippet."]]]))

(defn description-0-2 []
  (html
   [:div
    [:div.docs__content__fragment
     [:p "Note that when the input value is" (num "assigned" "[10],")
      "an empty or default string should be given."]]
    ]))

(defn fork-code-0-0 []
  (html
   (let [[{:keys [values
                  handle-change
                  handle-blur]}]
         (fork/fork-form {:initial-values
                          {"input" "Type here!"}})]
     [:div.docs__content__fragment
      [:div.fragment__heading
       [:h5 "Output:"]]
      [:div.fragment__paragraph
       [:p (str "Value: " (get values "input"))]]
      [:form
       [:input.some-style
        {:name "input"
         :value (get values "input")
         :type "text"
         :on-change handle-change
         :on-blur handle-blur}]]])))

(defn code-snippet-0-on-submit
  [evt {:keys [values set-submitting]}]
  (.preventDefault evt)
  (js/alert values)
  (set-submitting false))
