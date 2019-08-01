(ns fork.test.reframe
  (:require
   [fork.reframe-logic :as reframe]
   [cljs.test :refer-macros [deftest is testing run-tests]]))

(defn evt [name value]
  (clj->js  {"target" {"name" name
                       "value" value}}))

(def state (atom nil))

(deftest client-validation
  (testing "Generate error map from evaluated validation"
    (is (= {"input" {:a "a" :b "b"}
            "checkbox" {:a "a" :b "b"}}
           (reframe/validation->error-map
            (-> ((fn [_] {:client {:on-change {"input" [[nil :a "a"]
                                                        [nil :b "b"]]
                                               "checkbox" [[nil :a "a"]
                                                           [nil :b "b"]]}}}))
                :client :on-change))))
    (is (= {"input" {:b "b"}
            "checkbox" {:b "b"}}
           (reframe/validation->error-map
            (-> ((fn [_] {:client {:on-change {"input" [[true :a "a"]
                                                        [nil :b "b"]]
                                               "checkbox" [[true :a "a"]
                                                           [nil :b "b"]]}}}))
                :client :on-change))))
    (is (= {"input" nil
            "checkbox" {:b "b"}
            :general {:c "c"}}
           (reframe/validation->error-map
            (-> ((fn [_] {:client {:on-change {"input" [[true :a "a"]
                                                        [true :b "b"]]
                                               "checkbox" [[true :a "a"]
                                                           [nil :b "b"]]
                                               :general [[nil :c "c"]
                                                         [true :a "a"]]}}}))
                :client :on-change))))))

(deftest handle-change

  (reset! state nil)

  (testing "Update input value without validating"
    (reframe/handle-change (evt "input" "hello")
                           {:state state})
    (is (= {"input" "hello"} (:values @state))))

  (reset! state nil)

  (testing "Update input value with validation"
    (reframe/handle-change (evt "input" "hello")
                           {:state state
                            :validation (fn [values]
                                          {:client
                                           {:on-change
                                            {"input" [[false :foo "bar"]]}}})})
    (is (and (= {"input" "hello"} (:values @state))
             (= {"input" {:foo "bar"}} (:errors @state))))))

(deftest handle-blur

  (reset! state nil)

  (testing "Blur input without validating"
    (reframe/handle-blur (evt "input" "hello")
                         {:state state})
    (is (= {"input" true} (:touched @state))))

  (reset! state nil)

  (testing "Update input value with validation"
    (reframe/handle-blur (evt "input" "hello")
                         {:state state
                          :validation (fn [values]
                                        {:client
                                         {:on-blur
                                          {"input" [[false :foo "bar"]]}}})})
    (is (and (= {"input" true} (:touched @state))
             (= {"input" {:foo "bar"}} (:errors @state))))))

(run-tests)
