(ns fork.forms.frontend.views.demo
  (:require-macros
   [fork.forms.frontend.hicada :refer [html]])
  (:require
   [ajax.core :as ajax]
   [cljs.pprint :as p]
   [fork.forms.frontend.views.common :as common]
   [react :as r]
   [fork.fork :as fork])
  (:import
   [goog.async Debouncer]))

(defn debounce [f interval]
  (let [dbnc (Debouncer. f interval)]
    (fn [& args] (.apply (.-fire dbnc) dbnc (to-array args)))))

(defn highlight-code-block-dynamic
  [snippet-ref snippet]
  (r/useEffect
   (fn []
     (when-let [snippet-ref
                (-> snippet-ref .-current)]
       (js/hljs.highlightBlock snippet-ref))
     js/undefined) #js [snippet]))

(defn code-snippet
  [props]
  (let [snippet
        (common/props-out! props :state)
        snippet-element (r/useRef nil)]
    (highlight-code-block-dynamic
     snippet-element snippet)
    (html
     [:div.code-snippet-demo
      [:pre
       [:code.clj
        {:ref snippet-element}
        snippet]]])))

(defn easy-input
  [{:keys [input-name
           placeholder
           type label
           icon-left]}
   {{:keys [values
            touched
            errors
            handle-change
            handle-blur]} :props}]
  (html
   [:div.field
    [:label.label.fork-label label]
    [:div.control.has-icons-left
     [:input.input
      {:name input-name
       :placeholder placeholder
       :type type
       :value (get values input-name)
       :on-change handle-change
       :on-blur handle-blur}]
     [:span.icon.is-small.is-left
      [:i {:class (str "fas " icon-left)}]]]
    (when (and (get touched input-name)
               (get errors input-name))
      (for [[k msg] (get errors input-name)]
        [:p.help {:key k} msg]))]))

(defn easy-submit
  [{:keys [class]}
   {{:keys [is-submitting?
            submit-count
            is-invalid?]} :props}]
  (html
   [:button.button
    {:type "submit"
     :class (str class " "
                 (when is-submitting?
                   "is-loading"))
     :disabled (and is-invalid?
                    (> submit-count 0))}
    "Submit"]))

(defn reg-handler
  [[response body] {:keys [is-validation-passed?
                           set-waiting-for-server]}]
  (set-waiting-for-server "email" false)
  (is-validation-passed? (:validation body) :server-email))

(defn reg-server-validation
  [{:keys [values errors set-waiting-for-server] :as props}]
  (if (:client-email (get errors "email"))
    (set-waiting-for-server "email" false)
    (ajax/ajax-request
     {:uri  "/reg-validation"
      :method :post
      :params {:email (get values "email")}
      :handler #(reg-handler % props)
      :format  (ajax/transit-request-format)
      :response-format (ajax/transit-response-format)})))

(defn reg-on-submit
  [{:keys [set-submitting
           values is-invalid?]}]
  (set-submitting false)
  (when-not is-invalid?
    (js/alert values)))

(defn reg-validation
  [values]
  {:client
   {:on-change
    {"email"
     [[(re-matches #".+@.+" (values "email"))
       {:client-email "Must be email"}]]
     "password"
     [[(> (count (values "password")) 6)
       {:err "Must be longer than 6 chars"}]]
     "re-password"
     [[(= (values "password")
          (values "re-password"))
       {:err "Must match password"}]]}}
   :server
   {:on-change
    {"email"
     [[reg-server-validation {:server-email "Your email is already taken!"}]]}}})

(defn registration [_]
  (let [[props]
        (fork/fork
         {:initial-values {"email" ""
                           "password" ""
                           "re-password" ""}
          :prevent-default? true
          :validation reg-validation
          :on-submit reg-on-submit})]
    (html
     [:div.demo__reg__container

      [:form.fork-card.demo__reg__card
       {:on-submit (:handle-submit props)}
       [:div.fork-title "Register"]
       [:div.is-divider.fork-divider]
       (easy-input {:input-name "email"
                    :placeholder "your@email.com"
                    :type "text"
                    :label "Email"
                    :icon-left "fa-user"}
                   {:props props})
       (easy-input {:input-name "password"
                    :type "password"
                    :label "Password"
                    :icon-left "fa-lock"}
                   {:props props})
       (easy-input {:input-name "re-password"
                    :type "password"
                    :label "Confirm Password"
                    :icon-left "fa-lock"}
                   {:props props})
       (easy-submit {:class "reg__submit"}
                    {:props props})]
      [:> code-snippet
       {:state
        (with-out-str
          (p/pprint
           (:state props)))}]])))

(defn view [_]
  (let []
    (html
     [:div
      [:div.demo__reg
       [:div "sdkldsjfndalfjnadlfjnadlfjn
sdlnsdljfnsdljfnsdljfndsljn"]
       [:> registration nil]]
      [:div.demo-content-2
       "heelo"]
      [:div.demo-content-3
       "heelo"]])))
