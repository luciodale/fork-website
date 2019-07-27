(ns fork.forms.frontend.devcards.reagent
  (:require
   [reagent.core :as reagent])
  (:require-macros
   [devcards.core :as dc :refer [defcard defcard-doc]]))

(defcard personal-profile-body
  (dc/reagent
   [:div {:style {:height "400px"
                  :background "beige"}}
    "hello"])
  (reagent/atom nil)
  {:inspect-data true :history true})
