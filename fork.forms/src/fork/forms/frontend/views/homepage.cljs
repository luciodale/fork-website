(ns fork.forms.frontend.views.homepage
  (:require-macros
   [fork.forms.frontend.hicada :refer [html]])
  (:require
   [react :as r]
   [fork.forms.frontend.views.common :as common]))

(defn fixed-navbar []
  (html
   (let [[state update-state] (r/useState nil)]
     [:nav.navbar.fixed-navbar
      [:div.navbar-brand
       [:a.navbar-item.fixed-navbar__brand
        "Logo"]
       [:a.navbar-burger.burger.fixed-navbar__burger
        {:class state
         :role "button"
         :aria-label "menu"
         :aria-expanded false
         :on-click
         (fn [] (update-state #(when (nil? %) "is-active")))}
        [:span {:aria-hidden true}]
        [:span {:aria-hidden true}]
        [:span {:aria-hidden true}]]]
      [:div.navbar-menu.fixed-navbar__menu
       {:class state}
       [:div.navbar-end
        [:div.navbar-item
         [:a.fixed-navbar__link "Docs"]]
        [:div.navbar-item
         [:a.fixed-navbar__link "Demo"]]]]])))

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
       [:span.icon
        [:img {:src "/images/cljs.svg"}]]
       [:span "Learn Now"]]
      [:a.button
       [:span.icon
        [:i.fab.fa-github]]
       [:span "GitHub"]]
      #_[:div.home-navbar__center__github-stars
       [:a.github-button
        {:href "https://github.com/luciodale/fork"
         :data-icon "octicon-star"
         :data-show-count true
         :aria-label "Star ntkme/github-buttons on GitHub"} "Star"]]]]]))

(def a
  "(defn second []
  (let [a 1]
  (+ a 4)))")

(defn body []
  (html
   [:div.home-body
    (common/code-snippet a)

    ]))

(defn view []
  (html
   [:div
    (fixed-navbar)
    (navbar-header)
    (body)]))
