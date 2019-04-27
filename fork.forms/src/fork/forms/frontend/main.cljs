(ns ^:figwheel-hooks fork.forms.frontend.main
  (:require-macros
   [fork.forms.frontend.hicada :refer [html]])
  (:require [react-dom]
            [react :as r]))

(defn func []
  (html
   (let [[count set-count] (r/useState nil)]
     [:div
      [:p "My hook counter: " count]
      [:button
       {:on-click #(set-count (inc count))}
       "Click this shit"]])))

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
