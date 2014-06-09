(ns intentions.core-test
  (:require [clojure.test :refer :all]
            [intentions.core :refer :all]))

(defintent valid?
  :dispatch identity
  :combine  #(and %1 %2))

(defconduct valid? ::good [_] true)

(defconduct valid? ::bad [_] false)

(derive ::a ::good)
(derive ::b ::bad)
(derive ::c ::good)
(derive ::c ::bad)

(deftest test-intent
  (is (valid? ::a))
  (is (not (valid? ::b)))
  (is (not (valid? ::c))))

(deftest test-conducts
  (is (map? (conducts valid?)))
  (is (= (set (keys (conducts valid?))) #{::good ::bad})))

(deftest test-derive-all
  (derive-all ::foobar #{::foo ::bar})
  (is (isa? ::foobar ::foo))
  (is (isa? ::foobar ::bar)))
