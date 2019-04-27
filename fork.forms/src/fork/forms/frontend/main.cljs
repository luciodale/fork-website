(ns ^:figwheel-hooks fork.forms.frontend.main
  (:require-macros
   [fork.forms.frontend.hicada :refer [html]])
  (:require [react-dom]))

(defn func []
  (html
   [:p "starting off with hooks"]))

(defn body []
  (html
   [:*
    [:div "body wrapper"]
    [:> func nil]]))

(defn mount
  []
  (let [section (js/document.getElementById "app")]
    (react-dom/render (body) section)))

;; This is called once
(defonce init (mount))

;; This is called every time you make a code change
(defn ^:after-load reload []
  (mount))
