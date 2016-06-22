(ns cljs-css-modules.macro
  #?(:clj
     (:require
      [garden.core :refer [css]]))
  #?(:cljs
     (:require
      [garden.core :refer [css]]
      [cljs-css-modules.runtime])))

; for now we localise only simple class and keyframes
(def selectors-to-localise
  [{:id "class"
    :regexp #"\.([^#.:\[\s]+)(.*)"
    :localise-fn #(str ".$1" "--" % "$2")
    :name-template "$1"
    :value-template "$1"}

   {:id "keyframe"
    :regexp #"@keyframes (.+)"
    :localise-fn #(str "@keyframes $1--" %)
    :name-template "$1"
    :value-template "$1"}])

(defn should-be-localised
  [selector]
  (some (fn [selector-object] (if (re-matches (:regexp selector-object) selector)
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
  [id style]
  (let [rules (into [] (rest style))
        s (name (first style))
        should-be-localised (should-be-localised s)]
    (if should-be-localised
      (let [selector-object should-be-localised
            localised-selector (localise-selector id s selector-object)]
        {:localised true
         :original-selector s
         :localised-selector localised-selector
         :style-object-key (get-selector-key s selector-object)
         :style-object-value (get-selector-value localised-selector selector-object)
         :garden-style style})
      {:localised false
       :garden-style style})))

(defn create-garden-style
  [item]
  (if (:localised item)
    (into [] (cons (:localised-selector item)
                   (rest (:garden-style item))))
    (:garden-style item)))

(defn create-map
  [item]
  (if (:localised item)
    [(:style-object-key item) (:style-object-value item)]
    nil))

(defmacro defstyle
  [style-id style]
  (let [inject-style-fn (symbol "cljs-css-modules.runtime" "inject-style!")
        id (gensym)
        processed-style (into []  (map (partial process-style id) style))
        style (into [] (map create-garden-style processed-style))
        map (into {} (map create-map processed-style))]
    `(do
       (def ~style-id ~map)
       (~inject-style-fn (apply css ~style) ~(str *ns*) ~(name style-id)))))
