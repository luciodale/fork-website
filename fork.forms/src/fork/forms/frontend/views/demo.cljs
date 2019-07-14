(ns fork.forms.frontend.views.demo
  (:require-macros
   [fork.forms.frontend.hicada :refer [html]])
  (:require
   [ajax.core :as ajax]
   [cljs.pprint :as p]
   [fork.forms.frontend.views.common :as common]
   [react :as r]
   [fork.fork :as fork]
   [clojure.string :as s])
  (:import
   [goog.async Debouncer]))

(defn debounce [f interval]
  (let [dbnc (Debouncer. f interval)]
    (fn [& args] (.apply (.-fire dbnc) dbnc (to-array args)))))

(defn highlight-code-block-dynamic
  [snippet-ref snippet-str toggled?]
  (r/useEffect
   (fn []
     (when-let [snippet-ref
                (-> snippet-ref .-current)]
       (js/hljs.highlightBlock snippet-ref))
     js/undefined) #js [snippet-str toggled?]))

(defn code-snippet
  [props]
  (let [[toggled? update-toggled?] (r/useState nil)
        snippet
        (dissoc (common/props-out! props :state)
                :on-change-server
                :on-change-client
                :on-blur-server
                :on-blur-client
                :client-cleared
                :validation-exists?
                :client-cleared-submit
                :on-submit-ready
                :can-submit)
        snippet-str (with-out-str
                      (p/pprint snippet))
        snippet-element (r/useRef nil)]
    (highlight-code-block-dynamic
     snippet-element snippet-str toggled?)
    (html
     [:div.code-snippet-demo
      [:div.button-inspect
       [:div.button
        {:on-click #(update-toggled? not)}
        "Inspect State"
        [:i.fas.fa-arrows-alt-v
         {:style {:padding-left "0.5em"}}]]]
      (when toggled?
        [:pre {:style {:padding "0"}}
         [:code.clj
          {:ref snippet-element}
          snippet-str]])])))

(defn pretty-input
  [{:keys [input-name
           placeholder
           type label
           icon-left]}
   {{:keys [values
            touched
            state
            errors
            handle-change
            handle-blur]} :props}]
  (html
   [:div.field
    [:label.label.fork-label label]
    [:div.control.has-icons-left.has-icons-right
     [:input.input
      {:name input-name
       :type type
       :value (get values input-name)
       :on-change handle-change
       :on-blur handle-blur}]
     [:span.icon.is-small.is-left
      [:i {:class (str "fas " icon-left)}]]
     [:span.icon.is-small.is-right
      [:i {:class (when (and (get touched input-name))
                    (cond
                      (get errors input-name) "fa-times fas text-red"
                      (not (s/blank? (values input-name)))
                      "fa-check fas text-green"))}]]]
    (when (and (get touched input-name) (get errors input-name))
      (for [[k msg] (get errors input-name)]
        [:p.help {:key k} msg]))]))

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
      :params {:email (s/trim (get values "email"))}
      :handler #(reg-handler % props)
      :format (ajax/transit-request-format)
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
     [[(re-matches #".+@.+" (s/trim (values "email")))
       {:client-email "Must be an email"}]]
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
          :on-submit reg-on-submit})
        _ (r/useEffect (fn []
                         (when (clojure.string/blank? ((:values props) "email"))
                           ((:clear-input-errors props) "email"))
                         identity)
                       #js [(:values props)])]
    (html
     [:div.demo-content.content
      [:div.demo__reg__container
       [:div.demo__reg__text.message
        [:div.message-header
         [:h4.title {:style {:color "white"
                             :margin-bottom "0"
                             :padding "0.2em"}}  "Registration:"]]
        [:div.message-body.demo__reg__message-body
         [:div
         "Being a common use case, the registration is presented as first"
          [:strong " fork component"] "."]
         [:br]
         [:div [:h5.title "Validation"]]
         [:div "The" [:strong " email "]"input involves both client and server side validation, taking place on change.  The client validation only ensures that the email has \"@\" between any char combination."
          " The server validation checks whether the inserted email is available for signing up."]
         [:br]
         [:div "Fork gives you the option to dispatch the server validation only when the client one succeeds."
          " In this way, you will not be loading your server with unwanted requests."]
         [:br]
         [:div "The list of already taken emails includes:"]
         [:div
          [:ul
           [:li "fork@form.com"]
           [:li "fork@clojure.com"]
           [:li "fork@cljs.com"]]]]]

       [:form.fork-card.demo__reg__card
        {:on-submit (:handle-submit props)}
        [:div.fork-title "Register"]
        [:div.is-divider.fork-divider]
        (pretty-input {:input-name "email"
                       :placeholder "your@email.com"
                       :type "text"
                       :label "Email"
                       :icon-left "fa-user"}
                      {:props props})
        (pretty-input {:input-name "password"
                       :type "password"
                       :label "Password"
                       :icon-left "fa-lock"}
                      {:props props})
        (pretty-input {:input-name "re-password"
                       :type "password"
                       :label "Confirm Password"
                       :icon-left "fa-lock"}
                      {:props props})
        (easy-submit {:class "reg__submit"}
                     {:props props})]]

      [:> code-snippet
       {:state (:state props)}]])))

(defn view [_]
  (let []
    (html
     [:div
      [:div.demo__reg
       [:div]
       [:> registration nil]]
      [:div.demo-content-2
       "heelo"]
      [:div.demo-content-3
       "heelo"]])))
