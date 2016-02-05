(ns intentions.test-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [intentions.core-test]))

(doo-tests 'intentions.core-test)
