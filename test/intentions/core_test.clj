(ns intentions.core-test
  (:require [clojure.test :refer :all]
            [intentions.core :refer :all]))

(deftest test-intent
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
    (is (= (i ::d) :maybe))))

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

(deftest test-derive-all
  (let [h (-> (make-hierarchy)
              (derive-all ::foobar #{::foo ::bar}))]
    (is (isa? h ::foobar ::foo))
    (is (isa? h ::foobar ::bar))))
