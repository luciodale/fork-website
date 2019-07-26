(ns fork.forms.frontend.devcards.main
  (:require
   [devcards.core]
   [reagent.core :as r])
  (:require-macros
   [devcards.core :as dc :refer [defcard defcard-doc]]))

(devcards.core/start-devcard-ui!)

(defcard personal-profile-body
  (dc/reagent
   [:div {:style {:height "400px"
                  :background "beige"}}
    "hello"])
  (r/atom nil)
  {:inspect-data true :history true})
