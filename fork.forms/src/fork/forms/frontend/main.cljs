(ns ^:figwheel-hooks fork.forms.frontend.main
  (:require-macros
   [fork.forms.frontend.hicada :refer [html]])
  (:require
   [accountant.core :as accountant]
   [bidi.bidi :as bidi]
   [fork.forms.frontend.views :as views]
   [fork.forms.frontend.routing :refer [routing routes]]
   [react-dom]))

(defn mount-root
  []
  (let [section (js/document.getElementById "app")]
    (react-dom/render
     (html
      [:> views/main-panel {:routing routing}]) section)))

(defn path-exists?
  [route]
  (boolean (bidi/match-route routes route)))

(defn init-route
  [{:keys [handler params] :as route}]
  (swap! routing assoc
         :handler handler
         :params params))

(defn ^:export init []
  (accountant/configure-navigation!
   {:nav-handler #(init-route (bidi/match-route routes %))
    :path-exists? path-exists?})
  (accountant/dispatch-current!)
  (do (mount-root) true))

;; This is called every time you make a code change
(defn ^:after-load reload []
  (init))

(defonce run (init))
