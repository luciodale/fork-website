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

(defn description-0-0-0 []
  (html
   [:div.docs__content__fragment
    [:p "First, require" (s "fork") "in your namespace."]]))

(defn description-0-0-1 []
  (html
   [:div
    [:div.docs__content__fragment
     [:p "Then, call" (c "fork") "inside your function component"
      " and destructure the following keys:"
      (c "values" :no-right) ","
      (c "handle-change" :no-right) ", and"
      (c "handle-blur" :no-right) "."]]
    [:div.docs__content__fragment
     [:p [:strong  "Always "] "initiate your form" (num "values" "[8]")
      "to let fork know about them. If your specific use case does not require"
      " your elements to have defualt values, then initiate them anyways just like this"
      " example:" (c "{\"input\" \"\"}" :no-right) "."
      " After that, write your input element using the fork handlers as follows."]]]))

(defn description-0-0-2 []
  (html
   [:div
    [:div.docs__content__fragment
     [:p "Note that the name attribute must be of type" (num "string" "[11]")
      "because HTML treats it as such. If the keyword" (c ":input") "was instead used,"
      " it would have been automatically cast to: " (c "\"input\"" :no-right) "."]]]))

(defn fork-code-0-0-0 []
  (html
   (let [[{:keys [values
                  handle-change
                  handle-blur]}]
         (fork/fork {:initial-values
                          {"input" "Type here!"}})]
     [:div.docs__content__fragment
      [:div.fragment__heading
       [:p [:strong "Output:"]]]
      [:div.fragment__paragraph
       [:p [:i (str "Value: " (get values "input"))]]]
      [:form
       [:input.some-style
        {:name "input"
         :value (get values "input")
         :type "text"
         :on-change handle-change
         :on-blur handle-blur}]]])))

(defn description-0-0-3 []
  (html
   [:div
    [:div.docs__content__fragment
     [:p "To read the input back, simply get it from the" (c "values") "map."]]]))

(defn description-0-1-0 []
  (html
   [:div
    [:div.docs__content__fragment
     [:p "Write your custom" [:strong " submit "] "function so that it takes two parameters."
      " The first one is the submit event, and the second one is a map of utilities"
      " coming straight from fork."]]
    [:div.docs__content__fragment
     [:p "For the time being, let's consider only a couple of them, as shown below."]]]))

(defn description-0-1-1 []
  (html
   [:div
    [:div.docs__content__fragment
     [:p "Let's now wire up the submit handler and our component by passing the former to"
      (num "fork" "[11]" :no-right) "."]]]))

(defn description-0-1-2 []
  (html
   [:div
    [:div.docs__content__fragment
     [:p "Take a moment to look at what's new."
      " First we destructured two extra handlers, being" (c "handle-submit")
      "and" (c "is-submitting?" :no-right) "."
      " Then," (c "handle-submit") "was specified to be the" [:strong " on-submit "] "action"
      " for the form component."
      " Last, a simple button was added right at the end of the form."]]]))

(defn on-submit-0-1-0
  [evt {:keys [values set-submitting]}]
  (.preventDefault evt)
  (js/setTimeout
   (fn [_]
     (js/alert values)
     (set-submitting false)) 300))

(defn fork-code-0-1-0 []
  (html
   (let [[{:keys [values
                  handle-change
                  handle-blur
                  handle-submit
                  is-submitting?]}]
         (fork/fork {:initial-values
                     {"input" "Type here!"}
                     :on-submit on-submit-0-1-0})]
     [:div.docs__content__fragment
      [:div.fragment__heading
       [:p [:strong "Output:"]]]
      [:form
       {:style {:margin-bottom "1.5em"}
        :on-submit handle-submit}
     [:input.some-style
      {:name "input"
       :value (get values "input")
       :type "text"
       :on-change handle-change
       :on-blur handle-blur}]
       [:button.button.is-success
        {:style {:margin-left "1em"}
         :type "submit"
         :disabled is-submitting?}
      "Submit!"]]])))

(defn description-0-2-0 []
  (html
   [:div
    [:div.docs__content__fragment
     [:p "Fork does not force you to write validation, but if required, "
      "it can be included very easily thanks to a highly" [:strong " customizable "]
      "schema like approach."]]
    [:div.docs__content__fragment
     [:p "As fork will call your function by giving it the form values, "
      "validating inputs against different fields"
      " will stay organized and clean."]]
    [:div.docs__content__fragment
     [:p "First, write the name of the input you want to validate. "
      "Second, give it a vector of vectors to accomodate for"
      [:strong " multiple checks "] "per field. "
      " Build your logic at index 0 and pass a map of errors at index 1."
      " Keep in mind that the errors are added whenever your logic returns"
      (c "false") "or" (c "nil" :no-right) "."]]]))

(defn description-0-2-1 []
  (html
   [:div
    [:div.docs__content__fragment
     [:p "The error messages are associated and dissociated from the state"
      " via their keys for multi-language support."
      " You can freely name your keywords, as they are exclusively used to avoid"
      " a string match implementation in the internal logic."]]
    [:div.docs__content__fragment
     [:p "At this point, pass your validation function to" (num "fork" "[14]")
      "and include" (c "errors") "and" (c "touched")
      "to the destructured" (num "keys" "[5-6]" :no-right) "."]]]))

#_(defn description-0-2-1 []
  (html
   [:div
    [:div.docs__content__fragment
     [:p ""
      " via their keys for multi-language support."
      " You can freely name your keywords, as they are exclusively used to avoid"
      " a string match implementation in the internal logic."]]
    [:div.docs__content__fragment
     [:p "At this point, pass your validation function to" (num "fork" "[14]")
      "and include" (c "errors") "and" (c "touched")
      "to the destructured" (num "keys" "[5-6]" :no-right) "."]]]))

(defn validation
  [values]
  {"input"
   [[(> (count (values "input")) 5)
     {:smaller-than-5
      "Must be bigger than 5"}]
    [(= (values "input") "hello fork!")
     {:must-equal-text
      "Must equal hello fork!"}]]})

(defn fork-code-0-2-0 []
  (html
   (let [[{:keys [values
                  errors
                  touched
                  handle-change
                  handle-blur
                  handle-submit
                  is-submitting?]}]
         (fork/fork {:initial-values
                     {"input" "Type here!"}
                     :on-submit on-submit-0-1-0
                     :validation [:on-change validation]})]
     [:div.docs__content__fragment
      [:div.fragment__heading
       [:p [:strong "Output:"]]]
      [:form
       {:style {:display "flex"
                :margin-bottom "1.5em"}
        :on-submit handle-submit}
       [:div
        [:input.some-style
         {:name "input"
          :value (get values "input")
          :type "text"
          :on-change handle-change
          :on-blur handle-blur}]
        (when (and (get errors "input") (get touched "input"))
          (for [[k msg] (get errors "input")]
            [:p.help {:key k}
             msg]))]
       [:button.button.is-success
        {:style {:margin-left "1em"}
         :type "submit"
         :disabled is-submitting?}
      "Submit!"]]])))

(defn description-0-2-2 []
  (html
   [:div
    [:div.docs__content__fragment
     [:p "- keywords for multilanguage"
      "- vectors for multi validation"
      "- true to be valid -wire up with the rest"
      "- errors map from fork"
      "- on blur on change"]]
    [:div.docs__content__fragment
     [:p ""]]]))
