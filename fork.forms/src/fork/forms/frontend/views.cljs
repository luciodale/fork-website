(ns fork.forms.frontend.views
  (:require-macros
   [fork.forms.frontend.hicada :refer [html]])
  (:require
   [fork.forms.frontend.views.homepage :as homepage]
   [fork.forms.frontend.views.docs :as docs]
   [fork.forms.frontend.views.demo :as demo]
   [fork.forms.frontend.views.test :as test]
   [fork.forms.frontend.views.common :as common]
   [fork.forms.frontend.views.tests :as tests]
   [fork.forms.frontend.routing :refer [vhost site-root]]
   [ajax.core :as ajax]
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

(defn handler [[response snippets] update-state]
  (when response (update-state snippets)))

(defn http-snippets [update-state]
  (r/useEffect
    (fn [_]
      (ajax/ajax-request
       {:uri (str vhost site-root "snippets")
        :method :get
        :handler #(handler % update-state)
        :response-format (ajax/transit-response-format)})
      identity)
    #js []))

(defn main-panel
  [routing]
  (html
   (let [{:keys [handler params]}
         (useLens (.-routing routing) identity)
         [state update-state] (r/useState nil)]
     (http-snippets update-state)
     (cond
       (= :index handler) [:> homepage/view nil]
       :else
       (let []
         [:div
          [:> common/fixed-navbar nil]
          (case handler
            :index [:> homepage/view nil]
            :docs [:> docs/view {:docs state}]
            :demo [:> demo/view {}]
            :test (tests/view)
            :example (test/fork))])))))
