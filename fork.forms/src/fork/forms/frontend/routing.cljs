(ns fork.forms.frontend.routing)

(def routing (atom nil))

(def routes
  ["/"
   [["" :index]
    ["docs" :docs]
    ["example" :example]]])
