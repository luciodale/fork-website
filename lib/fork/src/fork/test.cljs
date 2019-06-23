(ns fork.test
  (:require
   [fork.logic :as f]
   [cljs.test :refer-macros [deftest is testing run-tests]]))

(defn validation-0 [values]
  {:client
   {:on-change
    {"one"
     [[(= "one" (get values "one")) {:err ""}]]
     "two"
     [[(= "" (get values "two")) {:err ""}]]
     :general
     [[(> (+ (count (get values "one"))
             (count (get values "two"))) 5) {:err ""}]]}
    :on-blur
    {"one"
     [[(= "one" (get values "one")) {:err ""}]]
     "two"
     [[(= "two" (get values "two")) {:err ""}]]
     :general
     [[(> (+ (count (get values "one"))
             (count (get values "two"))) 5) {:err ""}]]}}})

(deftest retrieve-target-and-general-client
  (testing "Check that retrieve-target-and-general returns the right chunk of client validation"
    (let [props {:validation validation-0
                   :values {"two" "two"}}]
      (is (= (f/retrieve-target-and-general
              :client :on-change props {"one" "one"})
             {"one" [[true {:err ""}]]
              :general [[true {:err ""}]]}))
      (is (= (f/retrieve-target-and-general
              :client :on-submit props {"one" "one"})
             {})))
    (let [props {:validation validation-0
                   :values {"one" "one"
                            "two" "two"}}]
      (is (= (f/retrieve-target-and-general
              :client :on-blur props {"one" "one"
                                        "two" "two"})
             {"one" [[true {:err ""}]]
              "two" [[true {:err ""}]]
              :general [[true {:err ""}]]})))))

(defn validation-1
  [values]
  {:client
   {:on-change
    {"one"
     [[(= "not one" (values "one")) {:err "error"}]]
     "two"
     [[(= "" (values "two")) {:err2 "error2"}]
      [(= "" (values "two")) {:err22 "error22"}]]
     "three"
     [[(= "three" (values "three")) {:err3 "error3"}]]}
    :on-blur
    {"one"
     [[(= "" (values "one")) {:err1 "error1"}]]}
    :on-submit
    {:general
     [[(= 12 (+ (values "one") (values "two"))) {:general "general"}]]}}})

(def state (atom nil))

(def props {:u (partial swap! state)
            :validation validation-1
            :values {"one" "one"
                     "two" "two"}})

(deftest run-validation-client-0
  (testing "Check that the client side errors are properly added from the state"
    (let [_ (reset! state nil)
          schema (f/retrieve-target-and-general
                  :client :on-change props {"one" "user-input"})
          _ (f/run-validation-client props schema)]
      (is (= {:err "error"}
             (get (-> @state :errors) "one"))))))

(deftest run-validation-client-1
  (testing "Check that the client side errors are properly removed from the state"
    (let [schema (f/retrieve-target-and-general
                  :client :on-change props {"one" "not one"})
          _ (f/run-validation-client props schema)]
      (is (nil? (get (-> @state :errors) "one"))))))

(deftest run-validation-client-2
  (testing "Check that multiple errors are added and removed"
    (let [schema (f/retrieve-target-and-general
                  :client :on-change props {"two" "two"})
          _ (f/run-validation-client props schema)]
      (is (= {:err2 "error2" :err22 "error22"}
             (get (-> @state :errors) "two"))))))

(deftest run-validation-client-3
  (testing "Check that more than two handlers work well together"
    (let [schema (f/retrieve-target-and-general
                  :client :on-change props {"three" "three"})
          _ (f/run-validation-client props schema)
          schema2 (f/retrieve-target-and-general
                   :client :on-blur props {"one" "one"})
          _ (f/run-validation-client props schema2)]
      (is (and
           (nil? (get (-> @state :errors) "three"))
           (= {:err1 "error1"} (get (-> @state :errors) "one")))))))

(deftest run-validation-client-4
  (testing "Check that general validation fires properly along with input validation"
    (let [schema (f/retrieve-target-and-general
                  :client :on-submit props {"one" 5
                                            "two" 5})
          _ (f/run-validation-client props schema)]
      (is (= {:general "general"} (get (-> @state :errors) :general))))))

(defn http-request-0
  [props is-validation-passed?]
  (is-validation-passed? false :err-404))

(defn http-request-1
  [props is-validation-passed?]
  (is-validation-passed? true))

(defn validation-2
  [values]
  {:server
   {:on-change
    {"one"
     [[http-request-0 {:err-404 "404"
                       :err-403 "403"}]]
     "two"
     [[http-request-1 {:err-403 "403"}]]}}})

(defn set-is-waiting [{u :u} k bool]
  (if bool
    (u #(assoc-in % [:waiting? k] :waiting))
    (u #(update-in % [:waiting?] dissoc k))))

(defn is-resolved [{u :u :as props} input-key msg bool]
  (let [err-key (ffirst msg)]
    (set-is-waiting props input-key false)
    (if bool
      (u #(update-in % [:errors input-key] dissoc err-key))
      (u #(assoc-in % [:errors input-key err-key] (err-key msg))))))

(def props-1 {:validation validation-2
              :values {"one" "one"
                       "two" "two"}})

(defn run-validation-server
  [{u :u :as props} validation]
  (doseq [[input-key cond-coll] validation
          [func msg] cond-coll]
    (do
      (set-is-waiting props input-key true)
      (func #(is-resolved props input-key msg %)))))
