(ns cljs-css-modules.macro
  #?(:clj
     (:require
      [garden.core :refer [css]]
      [garden.stylesheet :refer [at-media at-keyframes]]))
  #?(:cljs
     (:require
      [garden.core :refer [css]]
      [garden.stylesheet :refer [at-media at-keyframes]]
      [cljs-css-modules.runtime])))

; for now we localise only simple class and keyframes
(def selectors-to-localise
  [{:id :class
    :regexp #"\.([^#.:\[\s]+)(.*)"
    :localise-fn #(str ".$1" "--" % "$2")
    :name-template "$1"
    :value-template "$1"}

   {:id :keyframe
    :regexp #"@keyframes (.+)"
    :localise-fn #(str "@keyframes $1--" %)
    :name-template "$1"
    :value-template "$1"}])

(defn should-be-localised
  [selector]
  (some (fn [selector-object]
          (if (re-matches (:regexp selector-object) selector)
              selector-object
              false)) selectors-to-localise))

(defn localise-selector
  [id selector {:keys [regexp localise-fn]}]
  (clojure.string/replace selector regexp (localise-fn id)))

(defn get-selector-key
  "Return the key to use to retrieve the actual selector value"
  [selector {:keys [regexp name-template]}]
  (keyword (clojure.string/replace selector
                                   regexp
                                   name-template)))

(defn get-selector-value
  "Return the actual selector value"
  [selector {:keys [regexp value-template]}]
  (name (clojure.string/replace selector
                                regexp
                                value-template)))

(defn process-style
  [id [fst & rest :as style]]
  (let [rules (into [] rest)
        s (name fst)
        should-be-localised (should-be-localised s)]
    (if should-be-localised
      (let [selector-object should-be-localised
            localised-selector (localise-selector id s selector-object)]
        {:localised true
         :selector-type (:id selector-object)
         :original-selector s
         :localised-selector localised-selector
         :style-object-key (get-selector-key s selector-object)
         :style-object-value (get-selector-value localised-selector selector-object)
         :garden-style style})
      {:localised false
       :garden-style style})))

(defn create-garden-style
  [{:keys [selector-type localised localised-selector garden-style style-object-value] :as item}]
  (if localised
    (case selector-type
      ; use at-keyframes from garden to manage key-frames
      :keyframe (do
                  (apply (partial at-keyframes style-object-value)
                         (rest garden-style)))
      ; default case
      (into [] (cons localised-selector
                     (rest garden-style))))
    garden-style))

(defn create-map
  [{:keys [selector localised style-object-key style-object-value]}]
  (if localised
    [style-object-key style-object-value]
    nil))

(defmacro defstyle
  [style-id [first second rest :as style] & [test-flag]]
  (let [inject-style-fn (symbol "cljs-css-modules.runtime" "inject-style!")
        id (gensym)
        processed-style (into [] (map (partial process-style id) style))
        style (into [] (map create-garden-style processed-style))
        map (into {} (map create-map processed-style))]
    (if test-flag
      {:map map
       :css (apply css style)} ;useful for testing
      `(do
         (def ~style-id ~map)
         (~inject-style-fn (apply css {:pretty-print? false} ~style)
           ~(str *ns*)
           ~(name style-id))))))
