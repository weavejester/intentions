(ns intentions.core-test
  #?@(:clj  [(:require [clojure.test :refer :all]
                       [intentions.core :as i])]
      :cljs [(:require-macros [cemerick.cljs.test :refer [is deftest testing]])
             (:require [cemerick.cljs.test :as t]
                       [intentions.core :as i :include-macros true])]))

(deftest test-intent
  (testing "preconditions"
    (is (thrown? #?(:clj AssertionError :cljs js/Error) (i/make-intent)))
    (is (thrown? #?(:clj AssertionError :cljs js/Error) (i/make-intent :dispatch type)))
    (is (thrown? #?(:clj AssertionError :cljs js/Error)
                 (i/make-intent :dispatch 1 :combine concat))))
  (testing "no matching method"
    (let [h (make-hierarchy)
          i (i/make-intent :dispatch type :combine concat)]
      (is (thrown? #?(:clj IllegalArgumentException :cljs js/Error) (i ::a)))))
  (testing "usage"
    (let [h (-> (make-hierarchy)
                (derive ::a ::good)
                (derive ::b ::bad)
                (derive ::c ::good)
                (derive ::c ::bad))
          i (i/make-intent :dispatch  identity
                           :combine   #(and %1 %2),
                           :hierarchy h
                           :default   ::default)]
      (is (i/intent? i))
      (i/add-conduct i ::good (constantly true))
      (i/add-conduct i ::bad (constantly false))
      (i/add-conduct i ::default (constantly :maybe))
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
        i (i/make-intent :dispatch  identity
                         :combine   merge
                         :hierarchy h)]
    (i/add-conduct i ::b (constantly {:value :b}))
    (i/add-conduct i ::c (constantly {:value :c}))
    (is (= (i ::d) {:value :c}))
    (i/prefer-conduct i ::b ::c)
    (is (= (i ::d) {:value :b}))))

(deftest test-conducts
  (let [h  (make-hierarchy)
        i  (i/make-intent :dispatch identity
                          :combine #(and %1 %2)
                          :hierarchy h)
        f1 (constantly true)
        f2 (constantly false)]
    (i/add-conduct i ::good f1)
    (i/add-conduct i ::bad f2)
    (i/add-conduct i ::maybe (constantly :maybe))
    (i/remove-conduct i ::maybe)
    (is (= (i/conducts i) {::good f1, ::bad f2}))))

(deftest test-macros
  (try
    (i/defintent i-test
      "Some docstring."
      {:author "Foo"}
      :dispatch identity
      :combine  into)
    #?(:clj (is (= (-> #'i-test meta :doc) "Some docstring.")))
    #?(:clj (is (= (-> #'i-test meta :author) "Foo")))
    (is (i/intent? i-test))

    (derive ::ab ::a)
    (derive ::ab ::b)

    (i/defconduct i-test ::a [x] #{:a})
    (i/defconduct i-test ::b [x] #{:b})

    (is (= (i-test ::ab) #{:a :b}))
    (finally
      #?(:clj (ns-unmap *ns* 'i-test)))))

(deftest test-redefs
  (try
    (i/defintent i-test :dispatch identity, :combine into)
    (i/defconduct i-test :a [_] #{:x})

    (let [f i-test]
      (i/defintent i-test :dispatch identity, :combine into)
      (i/defconduct i-test :a [_] #{:y})
      (is (= (f :a) #{:y})))

    (finally
      #?(:clj (ns-unmap *ns* 'i-test)))))
