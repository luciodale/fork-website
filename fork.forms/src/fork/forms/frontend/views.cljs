(ns fork.forms.frontend.views
  (:require-macros
   [fork.forms.frontend.hicada :refer [html]])
  (:require
   [fork.forms.frontend.views.homepage :as homepage]
   [fork.forms.frontend.views.docs :as docs]
   [fork.forms.frontend.views.demo :as demo]
   [fork.forms.frontend.views.test :as test]
   [fork.forms.frontend.views.common :as common]
   [ajax.core :as ajax]
   [react :as r]))

(defn use-lens
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
       {:uri "/snippets"
        :method :get
        :handler #(handler % update-state)
        :response-format (ajax/transit-response-format)})
      identity)
    #js []))

(defn main-panel
  [routing]
  (html
   (let [{:keys [handler params]}
         (use-lens (common/props-out! routing :routing) identity)
         [state update-state] (r/useState nil)]
     (http-snippets update-state)
     (when state
       (cond
         (= :index handler) [:> homepage/view nil]
         :else
         (let []
           [:div
            [:> common/fixed-navbar nil]
            (case handler
              :index [:> homepage/view nil]
              :docs [:> docs/view {:docs state}]
              :demo [:> demo/view nil]
              :test [:> test/view nil]
              :else
              [:div "handler:" (str handler)])]))))))
