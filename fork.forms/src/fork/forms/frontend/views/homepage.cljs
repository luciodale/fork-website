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

(defn a []
  "(defn fixed-navbar []
  (html
   (let [[state update-state] (r/useState nil)]
     [:nav.navbar.fixed-navbar
      [:div.navbar-brand
       [:a.navbar-item.fixed-navbar__brand
        {:href (routing/href-go! :index)}
        [:img {:src \"/images/logo-white.svg\"
               :width \"30\"}]
        [:p.fixed-navbar__brand-name \"Fork\"]]
       [:a.navbar-burger.burger.fixed-navbar__burger
        {:class state
         :role \"button\"
         :aria-label \"menu\"
         :aria-expanded false
         :on-click
         (fn [] (update-state #(when (nil? %) \"is-active\")))}
        [:span {:aria-hidden true}]
        [:span {:aria-hidden true}]
        [:span {:aria-hidden true}]]]
      [:div.navbar-menu.fixed-navbar__menu
       {:class state}
       [:div.navbar-end
        [:div.navbar-item.fixed-navbar__item
         {:on-click #(routing/go! :docs)}
         [:a.fixed-navbar__link
          \"Docs\"]]
        [:div.navbar-item.fixed-navbar__item
         [:a.fixed-navbar__link \"Demo\"]]]]])))")

(defn body []
  (html
   [:div.home-body
    (common/code-snippet (a))
    ]))

(defn view []
  (html
   [:div
    (common/fixed-navbar)
    (navbar-header)
    (body)]))
