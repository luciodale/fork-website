(ns fork.forms.frontend.routing
  (:require
   [accountant.core :as accountant]
   [bidi.bidi :as bidi]))

(def routing (atom nil))

(def routes
  ["/"
   [["" :index]
    ["docs" :docs]
    ["test" :test]
    ["demo" :demo]
    ["example" :example]]])

(defn href-go! [& params]
  (apply bidi/path-for routes params))

(defn go! [& params]
  (accountant/navigate! (apply bidi/path-for routes params)))
