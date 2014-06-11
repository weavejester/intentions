(ns intentions.core-test
  (:require [clojure.test :refer :all]
            [intentions.core :refer :all]))

(deftest test-intent
  (testing "preconditions"
    (is (thrown? AssertionError (make-intent)))
    (is (thrown? AssertionError (make-intent :dispatch type)))
    (is (thrown? AssertionError (make-intent :dispatch 1 :combine concat))))
  (testing "no matching method"
    (let [h (make-hierarchy)
          i (make-intent :dispatch type :combine concat)]
      (is (thrown? IllegalArgumentException (i ::a)))))
  (testing "usage"
    (let [h (-> (make-hierarchy)
                (derive ::a ::good)
                (derive ::b ::bad)
                (derive ::c ::good)
                (derive ::c ::bad))
          i (make-intent :dispatch  identity
                         :combine   #(and %1 %2),
                         :hierarchy h
                         :default   ::default)]
      (is (intent? i))
      (add-conduct i ::good (constantly true))
      (add-conduct i ::bad (constantly false))
      (add-conduct i ::default (constantly :maybe))
      (is (true? (i ::a)))
      (is (false? (i ::b)))
      (is (false? (i ::c)))
      (is (= (i ::d) :maybe)))))

(deftest test-prefer-conduct
  (let [h (-> (make-hierarchy)
              (derive ::b ::a)
              (derive ::c ::a)
              (derive ::d ::b)
              (derive ::d ::c))
        i (make-intent :dispatch  identity
                       :combine   merge
                       :hierarchy h)]
    (add-conduct i ::b (constantly {:value :b}))
    (add-conduct i ::c (constantly {:value :c}))
    (is (= (i ::d) {:value :c}))
    (prefer-conduct i ::b ::c)
    (is (= (i ::d) {:value :b}))))

(deftest test-conducts
  (let [h  (make-hierarchy)
        i  (make-intent :dispatch identity
                        :combine #(and %1 %2)
                        :hierarchy h)
        f1 (constantly true)
        f2 (constantly false)]
    (add-conduct i ::good f1)
    (add-conduct i ::bad f2)
    (add-conduct i ::maybe (constantly :maybe))
    (remove-conduct i ::maybe)
    (is (= (conducts i) {::good f1, ::bad f2}))))

(deftest test-macros
  (try
    (defintent i-test
      "Some docstring."
      {:author "Foo"}
      :dispatch type
      :combine  into)
    (is (= (-> #'i-test meta :doc) "Some docstring."))
    (is (= (-> #'i-test meta :author) "Foo"))
    (is (intent? i-test))

    (defconduct i-test Long [x] #{:int})
    (defconduct i-test Number [x] #{:num})

    (is (= (i-test 1) #{:int :num}))
    (finally
      (ns-unmap *ns* 'i-test))))
