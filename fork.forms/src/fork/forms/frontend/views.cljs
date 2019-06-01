(ns fork.forms.frontend.views
  (:require-macros
   [fork.forms.frontend.hicada :refer [html]])
  (:require
   [fork.forms.frontend.views.homepage :as homepage]
   [fork.forms.frontend.views.docs :as docs]
   [fork.forms.frontend.views.test :as test]
   [fork.forms.frontend.views.tests :as tests]
   [react :as r]))

(defn- useLens
  [a f]
  (let [[value update-value] (r/useState (f @a))]
    (r/useEffect
     (fn []
       (let [k (gensym "useLens")]
         (add-watch a k
                    (fn [_ _ _ new-state]
                      (update-value (f new-state))))
         (fn []
           (remove-watch a k)))))
    value))

(defn main-panel
  [routing]
  (html
   (let [{:keys [handler params]}
         (useLens (.-routing routing) identity)]
     (case handler
       :index [:> homepage/view nil]
       #_[:> test/fork nil]
       :docs [:> docs/view nil]
       :test [:> tests/view nil]
       :example [:div "welcome to examples"]
       [:div "nothing found"]))))
