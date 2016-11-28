(ns cljs-css-modules.runner
    (:require [doo.runner :refer-macros [doo-tests]]
              [cljs-css-modules.macro-test]))

(doo-tests
  'cljs-css-modules.macro-test)
