(ns viime.runner
    (:require [doo.runner :refer-macros [doo-tests]]
              [viime.core-test]))

(doo-tests 'viime.core-test)
