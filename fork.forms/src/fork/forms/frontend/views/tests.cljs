(ns fork.forms.frontend.views.tests
  (:require-macros
   [fork.forms.frontend.hicada :refer [html]])
  (:require
   [cljs.pprint :refer [pprint]]
   [fork.logic :as logic]
   [react :as r]))

(defn pprint-code
  [x]
  (html
   [:div
    [:pre.pprint-code
     [:code (with-out-str (pprint x))]]]))

(defn handle-change-value
  []
  (html
   [:div
    [:p "handle-change no validation:"]
    (let [[s u] (r/useState nil)]
      [:div
       (pprint-code s)
       [:input.s-margin
        {:value (or (-> s :values :input) "")
         :name :input
         :on-change #(logic/handle-change % {:u u})}]])]))

(defn schema [values]
  {:input [[(> (count (:input values)) 3)
            "input must be greater than 3"]
           [(= "hello" (:input values))
            "input must equal hello"]]
   :bella [[(> (count (:bella values)) 3)
            "BELLAAA"]]})

(defn handle-change-value+validation
  []
  (html
   [:div
    [:p "handle-change with validation:"]
    (let [[s u] (r/useState nil)
          v [:on-change schema]]
      [:div
       (pprint-code s)
       (for [k [:input :bella]]
         [:input.s-margin
          {:key k
           :value (or (-> s :values k) "")
           :name k
           :on-change #(logic/handle-change
                        %
                        {:u u
                         :s s
                         :validation v})}])])]))


(defn test-effect
  []
  (html
   [:div
    [:p "test effect"]
    (let [[state update-state] (r/useState {:values {:a 1 :b 2}})]
      (r/useEffect
       (fn []
         (prn "in effect")
         (set! js/document.title "aoaaa")
         #_(update-state #(assoc-in % [:values :c] 5))
         identity))
      [:div
       [:p "c: " (-> state :values :c)]
       [:p "a: " (-> state :values :a)]
       [:p "b: " (-> state :values :b)]
       [:button
        {:on-click (fn [_] (update-state #(update-in % [:values :a] inc)))}
        "Click to a"]
       [:button
        {:on-click (fn [_] (update-state #(update-in % [:values :b] inc)))}
        "Click to b"]
       [:button
        {:on-click (fn [_] (update-state #(update-in % [:values :c] inc)))}
        "Click to c"]])]))



(defn view []
  (html
   [:div
    [:> handle-change-value nil]
    [:> handle-change-value+validation nil]
    [:> test-effect nil]]))
