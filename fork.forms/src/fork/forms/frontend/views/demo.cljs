(ns fork.forms.frontend.views.demo
  (:require-macros
   [fork.forms.frontend.hicada :refer [html]])
  (:require
   [ajax.core :as ajax]
   [clojure.edn :as edn]
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

(defn reg-description []
  (html
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
       [:li "fork@cljs.com"]]]]]))

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
       (reg-description)
       [:form.fork-card.demo__card
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

(defn weather-description []
  (html
   [:div.demo__reg__text.message
    [:div.message-header
     [:h4.title {:style {:color "white"
                         :margin-bottom "0"
                         :padding "0.2em"}}  "Weather Forecast:"]]
    [:div.message-body.demo__reg__message-body
     [:div
      "Dynamic search with auto completion feature..."]
     [:br]
     [:div [:h5.title "City Value"]]
     [:div "Fork is used to first set the value from the list of suggested cities, and to pass it to the"
      " weather forecast api..."]
     ]]))

(defn cities-http
  [update-cities update-requested]
  (update-requested true)
  (ajax/ajax-request
   {:uri  "https://raw.githubusercontent.com/lutangar/cities.json/master/cities.json"
    :method :get
    :format (ajax/json-request-format)
    :response-format (ajax/json-response-format)
    :handler (fn [[resp body]]
               (update-cities (map (fn [city]
                                     {"name" (str (city "name")
                                                 " - "
                                                 (city "country"))
                                      "lat" (city "lat")
                                      "lng" (city "lng")}) body))
               (update-requested false))}))

(defn weather-http
  [set-submitting update-weather lat lng]
  (ajax/ajax-request
   {:uri  (str "/weather?"
               "lat=" lat
               "&lng=" lng)
    :method :get
    :format (ajax/transit-request-format)
    :response-format (ajax/json-response-format)
    :handler (fn [[ok body]]
               (set-submitting false)
               (update-weather
                (if ok body :error)))}))

(defn filter-cities
  [cities city]
  (when-not (s/blank? city)
    (let [pattern (re-pattern (str "(?i)" city))
          word-count (count city)]
      (->> cities
           (filter
            #(re-matches
              pattern
              (subs (get % "name") 0 word-count)))
           (take 15)
           (into #{})))))

(defn weather-card
  [data city]
  (html
   (let [main (get data "main")
         weather (first (get data "weather"))
         temp (get main "temp")
         pressure (get main "pressure")
         humidity (get main "humidity")
         min (get main "temp_min")
         max (get main "temp_max")
         wind (get (get data "wind") "speed")
         description (get weather "description")
         icon (get weather "icon")]
     [:div.weather-card__container
      (if (:error data)
        [:div "Oops something went wrong!"]
        [:div
         [:div.weather-card__city-icon
          [:h5.h-content
           [:i.fas.fa-thermometer-half]
           " " (.toFixed temp) " ° C"]
          [:img
           {:src (str "https://openweathermap.org/img/wn/"
                      icon "@2x.png")}]]
         [:h4 {:style {:text-align "center"}} city]
         [:div.weather-card__min-max
          [:h5.h-content
           [:i.fas.fa-thermometer-empty]
           [:strong " Min "] (.toFixed min) " °C"]
          [:h5.h-content
           [:i.fas.fa-thermometer-full]
           [:strong " Max "](.toFixed max) " °C"]]
         [:div.weather-card__speed-humidity
          [:h5.h-content
           [:i.fas.fa-wind]
           [:strong " Wind "] wind]
          [:h5.h-content
           [:i.fas.fa-tint]
           [:strong " Humidity "] humidity "%"]]])])))


(defn weather [_]
  (let [[cities update-cities] (r/useState nil)
        [requested? update-requested] (r/useState nil)
        [{:keys [state values
                 handle-change
                 set-values] :as props}]
        (fork/fork
         {:initial-values {"city" ""}
          :prevent-default? true})
        [is-clicked? update-is-clicked] (r/useState "")
        city (values "city")
        matches (filter-cities cities city)
        [chosen-city update-chosen-city] (r/useState nil)
        [weather update-weather] (r/useState nil)
        [is-submitting? set-submitting] (r/useState nil)
        [city-view update-city-view] (r/useState nil)]
    (html
     [:div.demo-content.content
      [:div.demo__reg__container
       (weather-description)
       [:div.fork-card.demo__card
        [:div.weather__title-group
         [:div.fork-title
          {:style {:margin-bottom "0"}}
          "Weather"]
         [:button.button
          {:disabled (when requested? true)
           :class (cond
                    (and (not cities) requested?)
                    "is-loading"
                    :else "")
           :on-click #(do
                        (.preventDefault %)
                        (when-not cities
                          (cities-http update-cities
                                       update-requested)))}
          (if cities
            (str (count cities) " " "Cities!")
            "Get Cities")]]
        [:div.is-divider.fork-divider]

        [:div.field.has-addons
         [:div.control.has-icons-left
          {:style {:width "100%"}}
          [:input.input
           {:name "city"
            :auto-complete "off"
            :disabled (not cities)
            :placeholder "Malibu"
            :type "text"
            :value (get values "city")
            :on-change handle-change}]
          [:span.icon.is-small.is-left
           [:i.fas.fa-city]]]
         [:div.control
          [:a.button.is-primary
           {:style {:width "6em"}
            :disabled (or (not= (s/trim (or chosen-city ""))
                                (s/trim city))
                          (s/blank? city)
                          is-submitting?
                          (not cities))
            :on-click #(do
                         (update-weather nil)
                         (update-city-view chosen-city)
                         (set-submitting true)
                         (weather-http
                          set-submitting
                          update-weather
                          (values "lat")
                          (values "lng")))}
           "Go!"]]]
        (when (and (seq matches)
                   (not= (s/trim (or chosen-city ""))
                         (s/trim city)))
          [:div.suggestion-weather
           (for [x matches
                 :let [city-name (x "name")]]
             (html
              [:div.suggestion-weather__city
               {:key (gensym)
                :on-click #(do
                             (update-chosen-city city-name)
                             (set-values {"city" city-name
                                          "lat" (x "lat")
                                          "lng" (x "lng")}))}
               city-name]))])
        [:div.
         {:class (when is-submitting?
                   "fork-is-loading")}
         (when weather
           (weather-card weather city-view))]]]
      [:> code-snippet
       {:state state}]])))

(defn view [_]
  (let []
    (html
     [:div
      [:div.demo__reg
       [:div]
       [:> registration nil]]
      [:div.demo__weather
       [:> weather nil]]
      [:div.demo-content-3
       "heelo"]])))
