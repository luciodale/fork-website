(ns ^:figwheel-hooks fork.forms.frontend.main
  (:require-macros
   [fork.forms.frontend.hicada :refer [html]])
  (:require
   [fork.forms.frontend.views :as views]
   [react-dom]
   [react :as r]))

(defn mount-root
  []
  (let [section (js/document.getElementById "app")]
    (react-dom/render
     (views/main-panel :index) section)))

;; This is called once
(defonce init
  (do (mount-root) true))

;; This is called every time you make a code change
(defn ^:after-load reload []
  (mount-root))
