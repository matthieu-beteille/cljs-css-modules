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

; TODO: define types and use cljs.spec
; TODO: add some tests

; for now we localise only simple class and keyframes
(def selectors-to-localise
  [{:id :class
    :regexp #"\.([^#.:\[\s]+)(.*)"
    :localise-fn #(str ".$1" "--" % "$2")
    :name-template "$1"
    :value-template "$1"}

   {:id :keyframe
    :regexp #"@keyframes (.+)"
    :localise-fn #(str "$1--" %)
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

(defn is-media?
  [[first & rest :as style]]
  (and (list? style)
       (= first 'at-media)))

(defn is-keyframe?
  [[first & rest :as style]]
  (and (list? style)
       (= first 'at-keyframes)))

(defn process-style
  [id [fst & rest :as style]]
  (cond
    ; keyframe
    (is-keyframe? style)
    (let [[symbol name & style] style
          localised-name (str name "--" id)]
      {:type :at-keyframe
       :garden-style style
       :selector {:original name
                  :localised localised-name
                  :key (keyword name)
                  :value localised-name}})

    ;; media-query
    (is-media? style)
    (let [[symbol params & style] style]
      {:type :media
       :params params
       :nested-style style})

    ;; other selectors
    :else
    (let [rules (into [] rest)
          s (name fst)
          should-be-localised (should-be-localised s)]
      (if should-be-localised
        (let [selector-object should-be-localised
              localised-selector (localise-selector id s selector-object)]
          {:type (:id selector-object)
           :selector {:original s
                      :localised localised-selector
                      :key (get-selector-key s selector-object)
                      :value (get-selector-value localised-selector selector-object)}
           :garden-style style})
        {:type :not-localised
         :garden-style style}))))

(defn create-garden-style
  [id
   {:keys [map style] :as acc}
   item]
  (let [at-media (symbol "garden.stylesheet" "at-media")
        at-keyframes (symbol "garden.stylesheet" "at-keyframes")
        {:keys [selector
                garden-style
                nested-style
                type
                params
                style-object-value] :as item} (process-style id item)]
    (case type
      :class
      {:map (assoc map
                   (:key selector)
                   (:value selector))
       :style
       (conj style
             (into [] (cons (:localised selector)
                            (rest garden-style))))}

      ;unify at-keyframe and keyframe
      :at-keyframe
      (do
        {:map (assoc map (:key selector) (:value selector))
         :style
         (conj style
               `~(concat [at-keyframes (:localised selector)] garden-style))})

      :keyframe
      (do
        {:map (assoc map (:key selector) (:value selector))
         :style
         (conj style
              `~(concat [at-keyframes (:localised selector)] (rest garden-style)))})

      :media
      (let [{s :style m :map} (reduce (partial create-garden-style id)
                                      {:map {}
                                       :style []}
                                      nested-style)]
        {:map (merge map m)
         :style (conj style `(~at-media ~params ~s))})

      :not-localised
      {:map map
       :style (conj style garden-style)}

      acc)))

(defmacro defstyle
  [style-id & [fst :as style]]
  (let [compiler-opts? (or (symbol? fst) (map? fst))
        compiler-opts (if compiler-opts? fst {})
        test-flag (= true (last style))
        style (cond
                (and compiler-opts? test-flag) (rest (drop-last style))
                test-flag (drop-last style)
                compiler-opts? (rest style)
                :else style)
        css (symbol "garden.core" "css")
        inject-style-fn (symbol "cljs-css-modules.runtime" "inject-style!")
        id (if test-flag "test" (gensym))
        {:keys [style map]} (reduce (partial create-garden-style id)
                                    {:map {}
                                     :style []}
                                    style)]
    (if test-flag
      {:map map
       :css `(apply ~css ~compiler-opts ~style)} ;useful for testing
      `(do
         (def ~style-id ~map)
         (~inject-style-fn (apply ~css ~compiler-opts ~style)
           ~(str *ns*)
           ~(name style-id))))))
