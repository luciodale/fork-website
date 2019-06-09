(ns fork.forms.frontend.views.docs
  (:require-macros
   [fork.forms.frontend.hicada :refer [html]])
  (:require
   [fork.forms.frontend.views.common :as common]
   [fork.forms.frontend.views.docs-render-fork :as render-fork]
   [fork.forms.frontend.routing :refer [vhost site-root]]
   [fork.fork :as fork]
   [react :as r]))

(defn legend-group
  [heading li-coll]
  (html
   [:div.docs__legend__group
    [:p.docs__legend__title
     heading]
    [:ul.group__ul
     (for [[id li] li-coll]
       [:li.group__li
        {:key id}
        [:a {:on-click #(common/go-to-id! id)} li]])]]))

(defn legend []
  (html
   [:div.docs__legend
    (legend-group
     "Quick-start"
     [["read-from-an-input"
       "Read from an input"]
      ["b" "Write an input"]])
    (legend-group
     "Whatever heading"
     [["c" "The bare minimum"]
      ["d" "Write an input"]])]))

(defn docs-render
  [props]
  (html
   (let [docs (common/props-out! props :docs)]
     [:div.content
      [:div.docs__content__section
       [:h2.docs__content__title "Quick Start:"
        [:span.is-divider.title-divider]]
       [:h5.docs__content__subtitle
        {:id "read-from-an-input"}
        "Read from an input"]
       (render-fork/description-0-0)
       [:> common/code-snippet {:doc (get docs 0)}]
       (render-fork/description-0-1)
       [:> common/code-snippet {:doc (get docs 1)}]
       (render-fork/description-0-2)
       [:> render-fork/fork-code-0-0 nil]]

      [:div
       [:h2.docs__content__title "Whatever"
        [:span.is-divider.title-divider]]
       [:p "The bare minimum"]
       [:> common/code-snippet {:doc (get docs 2)}]]])))

(defn view
  [docs]
  (html
   [:div
    [:div.white-edge]
    [:div.docs
     (legend)
     [:div.docs__content
      [:> docs-render docs]]]
    [:div {:style {:width "100%"
                   :height "400px"}}]
    #_[:> fork-test nil]]))
