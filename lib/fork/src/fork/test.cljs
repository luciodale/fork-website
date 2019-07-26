(ns fork.test
  (:require
   [fork.logic :as logic]
   [cljs.test :refer-macros [deftest is testing run-tests]]))

;; Unit Tests
(def state (atom nil))
(def u (partial swap! state))

(deftest set-external-errors
  (testing ""
    (reset! state nil)
    (let [errors {:error-one "message"
                  :error-two "message"}
          props {:u u}]
      (logic/set-external-errors errors props)
      (is (= errors (:external-errors @state))))))

(deftest clear-external-errors
  (testing ""
    (reset! state {:external-errors
                   {:error "message"}})
    (logic/clear-external-errors u)
    (is (empty? @state))))

(deftest errors
  (testing ""
    (reset! state
            {:errors
             {"input-one" {:errors-1 "msg"}
              "input-two" nil}
             :server-errors
             {"input-one" {:server-errors-1 "msg"}
              "input-two" nil
              "input-three" {:server-errors-2 "msg"
                             :server-errors-3 "msg"}}
             :external-errors
             {:on-submit-500 "server error"}})
    (is (= (logic/errors @state)
           {"input-one" {:errors-1 "msg"
                         :server-errors-1 "msg"}
            "input-two" nil
            "input-three" {:server-errors-2 "msg"
                           :server-errors-3 "msg"}
            :on-submit-500 "server error"}))))

(deftest validation-submap
  (testing ""
    (reset! state
            {:s {:values {"input-one" "input-one"
                          "input-two" "input-two"}}
             :validation
             (fn [values]
               {:client
                {:on-change
                 {"input-one"
                  [[(= (values "input-one")
                       "input-one")
                    :input-one "msg"]]
                  "input-two"
                  [[(= (values "input-two")
                       "input-two")
                    :input-two "msg"]]
                  :general
                  [[(= 1 1) :general "msg"]]}}})})
    (is (=
         {"input-one"
          [[true :input-one "msg"]]
          :general
          [[true :general "msg"]]}
         (logic/validation-submap
          :client :on-change @state "input-one")))))

(deftest run-validation-client
  (testing ""
    (reset! state {:errors {"three" {:k1 "msg"}}
                   :values {}})
    (let [validation-submap
          {"one" [[(= 1 3) :k1 "error 1"]
                  [(= 1 2) :k2 "error 2"]]
           "two" [[(= 1 1) :k3 "error 1"]]
           :gen [[(= 1 2) :gen1 "msg"]
                 [true :gen2 "msg"]]}]
      (logic/run-validation-client
       {:u u} validation-submap)
      (= @state
         {:values {}
          :errors
          {"three" {:k1 "msg"}
           "one" {:k2 "error 2" :k1 "error 1"}
           "two" nil
           :gen {:gen1 "msg"}}}))))

(run-tests)
