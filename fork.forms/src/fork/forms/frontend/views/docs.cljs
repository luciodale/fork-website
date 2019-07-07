(ns fork.forms.frontend.views.docs
  (:require-macros
   [fork.forms.frontend.hicada :refer [html]])
  (:require
   [fork.forms.frontend.views.common :as common]
   [fork.forms.frontend.views.docs-render-fork :as render-fork]
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

(defn legend
  [toggled? update-toggle]
  (html
   [:div
    {:class
     (if toggled?
       "docs__legend--mobile" "docs__legend")
     :on-click #(when toggled?
                  (update-toggle not))}
    (legend-group
     "Quick Start"
     [["0" "Welcome"]
      ["1" "Component Setup"]
      ["2" "Submit Form"]
      ["3" "Validation Schema"]
      ["4" "Summary"]])
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
        {:id "0"}
        "Welcome:"]
       (render-fork/description-intro)
       [:h5.docs__content__subtitle
        {:id "1"}
        "Component Setup:"]
       (render-fork/description-0-0-0)
       [:> common/code-snippet {:doc (get docs 0)}]
       (render-fork/description-0-0-1)
       [:> common/code-snippet {:doc (get docs 1)}]
       (render-fork/description-0-0-2)
       [:> render-fork/fork-code-0-0-0 nil]
       (render-fork/description-0-0-3)
       [:h5.docs__content__subtitle
        {:id "2"}
        "Submit Form:"]
       (render-fork/description-0-1-0)
       [:> common/code-snippet {:doc (get docs 2)}]
       (render-fork/description-0-1-1)
       [:> common/code-snippet {:doc (get docs 3)}]
       (render-fork/description-0-1-2)
       #_[:> render-fork/fork-code-0-1-0 nil]
       [:h5.docs__content__subtitle
        {:id "3"}
        "Validation Schema:"]
       (render-fork/description-0-2-0)
       [:> common/code-snippet {:doc (get docs 4)}]
       (render-fork/description-0-2-1)
       [:> common/code-snippet {:doc (get docs 5)}]
       (render-fork/description-0-2-2)
       #_[:> render-fork/fork-code-0-2-0 {"n" 0}]
       (render-fork/description-0-2-3)
       [:> common/code-snippet {:doc (get docs 6)}]
       (render-fork/description-0-2-4)
       #_[:> render-fork/fork-code-0-2-0 {"n" 1}]
       [:h5.docs__content__subtitle
        {:id "4"}
        "Summary:"]


       ]



      [:div
       [:h2.docs__content__title "Whatever"
        [:span.is-divider.title-divider]]
       [:p "The bare minimum"]
       [:> common/code-snippet {:doc (get docs 2)}]
       ]])))

(defn view
  [docs]
  (let [[toggled? update-toggle] (r/useState nil)]
    (html
     [:div
      [:div.white-edge]
      [:div.docs
       [:div.toggle__navbar
        {:on-click
         (fn [_]
           (update-toggle not))}
        [:div "Menu"]
        [:i.fas.fa-bars.toggle__navbar-icon]]
        (legend toggled? update-toggle)
       [:div.docs__content
        [:> docs-render docs]]]
      [:div {:style {:width "100%"
                     :height "400px"}}]
      #_[:> fork-test nil]])))
