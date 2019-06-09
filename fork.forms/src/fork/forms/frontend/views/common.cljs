(ns fork.forms.frontend.views.common
  (:require-macros
   [fork.forms.frontend.hicada :refer [html]])
  (:require
   [fork.forms.frontend.routing :as routing]
   [clojure.string :as string]
   [react :as r]))

(defn fixed-navbar []
  (html
   (let [[state update-state] (r/useState nil)]
     [:nav.navbar.fixed-navbar
      [:div.navbar-brand
       [:a.navbar-item.fixed-navbar__brand
        {:href (routing/href-go! :index)}
        [:img {:src "/images/logo-white.svg"
               :width "30"}]
        [:p.fixed-navbar__brand-name "Fork"]]
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
        [:div.navbar-item.fixed-navbar__item
         {:on-click #(routing/go! :docs)}
         [:a.fixed-navbar__link
          "Docs"]]
        [:div.navbar-item.fixed-navbar__item
         {:on-click #()}
         [:a.fixed-navbar__link
          "Installation"]]
        [:div.navbar-item.fixed-navbar__item
         [:a.fixed-navbar__link
          {:on-click #(routing/go! :demo)}
          "Demo"]]]]])))

(defn props-out! [props k]
  (goog.object/getValueByKeys props #js [(name k)]))

(defn go-to-id! [id]
  (js/window.location.assign (str "#" id)))

(defn highlight-code-block
  [snippet-ref snippet]
  (r/useEffect
   (fn []
     (when-let [snippet-ref
                (-> snippet-ref .-current)]
       (js/hljs.highlightBlock snippet-ref)
       (when snippet (js/hljs.lineNumbersBlock snippet-ref)))
     js/undefined)))

(defn code-snippet
  [props]
  (let [snippet (props-out! props :doc)
        [state update-state] (r/useState "Copy")
        snippet-element (r/useRef nil)]
    (highlight-code-block snippet-element snippet)
    (html
     [:div.code-snippet
      [:pre
       [:code.clj
        {:ref snippet-element}
        snippet]]
      [:a.button.code-snippet__copy
       {:class "btn-code"
        :data-clipboard-text snippet
        :on-click #(update-state "Copied")}
       [:span.icon
        [:i.far.fa-copy]]
       [:span state]]])))

(defn github-widget []
  (html
   [:div
    [:a.github-button
     {:href "https://github.com/luciodale/fork"
      :data-icon "octicon-star"
      :data-show-count true
      :aria-label "Star ntkme/github-buttons on GitHub"} "Star"]]))
