(ns fork.forms.frontend.views.homepage
  (:require-macros
   [fork.forms.frontend.hicada :refer [html]])
  (:require
   [react :as r]
   [bidi.bidi :as bidi]
   [fork.forms.frontend.routing :as routing]
   [fork.forms.frontend.views.common :as common]))

(defn navbar-header []
  (html
   [:div.home-navbar
    [:div.home-navbar__center
     [:div.home-navbar__center__logo
      "Fork"]
     [:div.home-navbar__center__caption
      "Fully customizable forms with React Hooks"]
     [:div.home-navbar__center__actions
      [:a.button
       {:href (routing/href-go! :docs)}
       [:span.icon
        [:img {:src "/images/cljs.svg"}]]
       [:span "Learn Now"]]
      [:a.button
       [:span.icon
        [:i.fab.fa-github]]
       [:span "GitHub"]]]]]))

(defn a [])

(defn body []
  (html
   [:div.home-body
    [:div "Focus on the fun bits and leave the rest to Fork."]
    [:div "Fun to use"]
    [:div "Easy to understand"]
    [:div "Extra light"]
    [:div "Built to scale"]
    [:div "You are one function away from loving forms!"]
    ]))
[:div.docs__content__fragment
     [:p "Fork was not conceived to work as a full wrapper. In fact, it only provides a set of handy functions to abstract away the complex logic of forms. Let the journey begin!"]]
(defn view []
  (html
   [:div
    (common/fixed-navbar)
    (navbar-header)
    (body)]))
