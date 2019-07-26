(ns fork.forms.frontend.views.docs-render-fork
  (:require-macros
   [fork.forms.frontend.hicada :refer [html]])
  (:require
   [fork.forms.frontend.views.common :as common]
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

(defn description-intro []
  (html
   [:div
    [:div.docs__content__fragment
     [:p "Hey there! Curious about Fork? You are in the right place. This quick tutorial will walk you through the basics of the library and give you some magic power to build your forms in a heartbeat, while keeping it fully customizable, really!"]]]))

(defn description-0-0-0 []
  (html
   [:div.docs__content__fragment
    [:p "First, require" (s "Fork") "in your namespace."]]))

(defn description-0-0-1 []
  (html
   [:div
    [:div.docs__content__fragment
     [:p "Then, call" (c "(f/fork ...)") "inside your function component"
      " and destructure the following keys:"
      (c "values" :no-right) ","
      (c "handle-change" :no-right) ", and"
      (c "handle-blur" :no-right) "."]]
    [:div.docs__content__fragment
     [:p [:strong  "Always "] "initiate your form" (num "values" "[8]")
      "to let Fork know about them. If your specific use case does not require"
      " your elements to have default values, then initiate them anyways just like this"
      " example:" (c "{\"input\" \"\"}" :no-right) "."
      " After that, write your input element using the Fork handlers as follows."]]]))

(defn description-0-0-2 []
  (html
   [:div
    [:div.docs__content__fragment
     [:p "Note that the name attribute must be of type" (num "string" "[11]")
      "because HTML treats it as such. If the keyword" (c ":input") "was instead used,"
      " it would have been automatically cast to: " (c "\"input\"" :no-right) "."]]]))

(defn fork-code-0-0-0 []
  (let [[{:keys [values
                 handle-change
                 handle-blur]}]
        (fork/fork {:initial-values
                    {"input" "Type here!"}})]
    (html
     [:div.docs__content__fragment.play-time
      [:div.fragment__heading
       [:p [:strong "Play Time:"]]]
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
     [:p "To read the input back, simply get it from the" (c "values") "map just like this:"
      (c "(get values \"input\")" :no-right) "."]]]))

(defn description-0-1-0 []
  (html
   [:div
    [:div.docs__content__fragment
     [:p "Write your custom" [:strong " submit "] "function so that it takes a map of utilities"
      " coming straight from Fork."
      " For the time being, let's consider only a couple of them, as shown below."]]]))

(defn description-0-1-1 []
  (html
   [:div
    [:div.docs__content__fragment
     [:p "Let's now wire up the submit handler and our component by passing the former to"
      (num "Fork" "[12]" :no-right) "."]]]))

(defn description-0-1-2 []
  (html
   [:div
    [:div.docs__content__fragment
     [:p "Take a moment to look at what's new."
      " First we destructured two extra handlers, being" (c "handle-submit")
      "and" (c "submitting?" :no-right) "."
      " Then," (c "handle-submit") "was specified to be the" [:strong " on-submit "] "action"
      " for the form component."
      " Last, a simple button was added right at the end of the form."]]
    [:div.docs__content__fragment
     [:p "You can also choose to prevent the form from being automatically submitted to the server by adding a" (c "prevent-default?" :no-right) (num "option" "[11]" :no-right) "."]]]))

(defn on-submit-0-1-0
  [{:keys [values set-submitting]}]
  (set-submitting false)
  (js/alert values))

(defn fork-code-0-1-0 []
  (html
   (let [[{:keys [values
                  state
                  handle-change
                  handle-blur
                  handle-submit
                  submitting?]}]
         (fork/fork {:initial-values
                     {"input" "Type here!"}
                     :prevent-default? true
                     :on-submit on-submit-0-1-0})]
     [:div.docs__content__fragment.play-time
      [:div.fragment__heading
       [:p [:strong "Play Time:"]]]
      [:form.fork-form
       {:on-submit handle-submit}
       [:input.some-style
      {:name "input"
       :value (get values "input")
       :type "text"
       :on-change handle-change
       :on-blur handle-blur}]
       [:div
        [:button.button.is-success.fork-button-docs
         {:type "submit"
          :disabled submitting?}
         "Submit!"]]]])))

(defn description-0-2-0 []
  (html
   [:div
    [:div.docs__content__fragment
     [:p "Fork does not force you to write validation, but if required, "
      "it can be included very easily thanks to a highly" [:strong " customizable "]
      "schema like approach."]]
    [:div.docs__content__fragment
     [:p "As Fork will provide the"
      [:strong " values "] "parameter"
      " when calling your function,"
      " validating inputs against different fields"
      " will stay organized and clean."]]
    [:div.docs__content__fragment
     [:p "First, choose between" (c ":client")
      "and/or" (c ":server") "side validation, and then"
      " add any of the"
      (c ":on-change" :no-right) ","
      (c ":on-blur" :no-right) ", and"
      (c ":on-submit") "built-in handlers. "]]
    [:div.docs__content__fragment
     [:p "After that, write the name of the input you want to validate, and "
      "give it a vector of vectors to accomodate for"
      [:strong " multiple checks "] "per field. "]]
    [:div.docs__content__fragment
     [:p
      " Build your logic at index 0 and pass your error keyword and message at index 1 and 2, respectively."
      " Keep in mind that each error is added whenever your logic returns"
      (c "false") "or" (c "nil" :no-right) "."]]]))

(defn description-0-2-1 []
  (html
   [:div
    [:div.docs__content__fragment
     [:p "The error messages are associated and dissociated from the state"
      " via their keys to not rely on a string match approach."
      " This will allow your forms to support multiple languages very safely."]]
    [:div.docs__content__fragment
     [:p " You can freely name the keywords for your error messages, but bare in mind that they must be unique, when they fall within the same input validation."]]
    [:div.docs__content__fragment
     [:p "At this point, pass your validation function to" (num "Fork" "[15]")
      "and include" (c "errors") "and" (c "touched")
      "to the destructured" (num "keys" "[5-6]" :no-right) "."]]]))

(defn description-0-2-2 []
  (html
   [:div
    [:div.docs__content__fragment
     [:p "The use of" (c "touched") "allows you
to display the errors only after the" [:strong " on-blur "] "effect has been fired."]]]))

(defn description-0-2-3 []
  (html
   [:div
    [:div.docs__content__fragment
     [:p "Oh no! If you noticed, the input can be submitted even when errors are detected. You can solve this issue by using" (c "invalid?") "in your submit handler. This is how:"]]]))

(defn description-0-2-4 []
  (html
   [:div
    [:div.docs__content__fragment
     [:p "Give it a try now. Fingers crossed!"]]]))

(defn validation
  [values]
  {:client
   {:on-change
    {"input"
     [[(> (count (values "input")) 5)
       :smaller-than-5
       "Must be bigger than 5"]
      [(= (values "input") "hello fork!")
       :must-equal-text
       "Must equal \"hello fork!\""]]}}})

(defn on-submit-0-2-1
  [{:keys [values set-submitting
           invalid?]}]
  (set-submitting false)
  (when-not invalid?
    (js/alert values)))

(defn fork-code-0-2-0
  [handler]
  (let [n (common/props-out! handler "n")
        [{:keys [values
                 errors
                 touched
                 handle-change
                 handle-blur
                 handle-submit
                 submitting?]}]
        (fork/fork {:initial-values
                    {"input" "Type here!"}
                    :prevent-default? true
                    :on-submit
                    (case n
                      0 on-submit-0-1-0
                      1 on-submit-0-2-1)
                    :validation validation})]
    (html
     [:div.docs__content__fragment.play-time
      [:div.fragment__heading
       [:p [:strong "Play Time:"]]]
      [:form.fork-form
       {:on-submit handle-submit}
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
       [:div
        [:button.button.is-success.fork-button-docs
         {:type "submit"
          :disabled submitting?}
         "Submit!"]]]])))


(defn api-params []
  (html
   [:div
    [:div.docs__content__fragment
     "It all starts with "
     (c "(fork { ... })" :no-right) "."
     " Call it in a "[:strong"let"]" and give it the following parameters."]]))

(defn api-initial-values []
  (html
   [:div
    [:div.docs__content__fragment
     "A way to let Fork know of your form input names and their initial values."
     " If your use case does not require defined initial values, you still have to initialize them with empty strings."
     [:br] " Example:" [:br]
     (c (str"{\"input\" \"initial-value\" "
            "\"sec-input\" \"\"}")
        :no-left)]]))

(defn api-on-submit []
  (html
   [:div
    [:div.docs__content__fragment
     "Define a function which takes one map as parameter and give it to Fork through the"
     (c ":on-submit")"key."
     " This function is called when you submit your form, so all the related logic should go in here."]]))

(defn api-prevent-default []
  (html
   [:div
    [:div.docs__content__fragment
     "If you do not want your form to be submitted automatically to your server, you can use this option and pass false."]]))

(defn api-validation []
  (html
   [:div
    [:div.docs__content__fragment
     "Define a function that takes one parameter, being the values of your form. This function has to return a map that follows the following rules:"
     [:ul
      [:li "nannr"]]]]))

(defn api-state []
  (html
   [:div
    [:div.docs__content__fragment
     "A map coming straight from react. All the fork logic is organized in here. "]]))

(defn api-update-state []
  (html
   [:div
    [:div.docs__content__fragment
     "Handler to manually update the fork state. You should never use this unless you truly know what you are doing."]]))

(defn api-values []
  (html
   [:div
    [:div.docs__content__fragment
     "A map with all your form values organized by name. "
     "An example would be:"
     (c "{\"my-input-name\" \"input-value\"}"
        :no-right) "."]]))

(defn api-errors []
  (html
   [:div
    [:div.docs__content__fragment
     "A map with the input errors."
     " "]]))


(defn description-0-2-5 []
  (html
   [:div
    [:div.docs__content__fragment
     [:p "This quick tutorial should be enough to get you started with fork"]]
    [:div.docs__content__fragment
     [:p]]]))

(defn description-01 []
  (html
   [:div
    [:div.docs__content__fragment
     [:p
      "Fork is great because it validates your form strictly according to the built-in handlers. This means that if your app includes some expensive validation that you want to perform exlusively on blur, Fork will not fake it by calling your function on-change and using the on-blur event just to display the erros."


      "server side validation"
      ""]]
    [:div.docs__content__fragment
     [:p]]]))

(defn description-0 []
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
[:div.docs__content__fragment
     [:p "At the moment, Fork works with any react version equal or above 16.8. If you are a Reagent user, don't worry... you haven't been forgotten!
A Fork version working compatible with older versions of react will be available soon!"]]
