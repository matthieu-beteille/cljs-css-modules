(ns cljs-css-modules.macro-style-component
  #?(:clj
     (:require
      [garden.core :refer [css]]
      [garden.stylesheet :refer [at-media at-keyframes]]))
  #?(:cljs
     (:require
      [garden.stylesheet :refer [at-media at-keyframes]]
      [garden.core :refer [css]]
      [cljs-css-modules.runtime])))

(defn join-keywords
  [join-char keywords]
  (keyword (clojure.string/join join-char (map name keywords))))

(defn build-style-map
  [bps class acc [key style]]
  (let [viewport (some #(when (= key (:id %)) %) bps)]
    (cond
      (and viewport (map? style))
      ; recursive call, without any breakpoints to deal with nested pseudo-classes
      (let [{:keys [style nested-style]}  (reduce (partial build-style-map [] class)
                                                  {:nested-style []
                                                   :responsive-style []
                                                   :style {}}
                                                   style)]
        (update-in acc
                    [:responsive-style]
                    conj
                    {:type :media-query
                     :id (:id viewport)
                     :params (dissoc viewport :id)
                     :content (into [] (concat [[class style]] nested-style))}))
      (map? style) (update-in acc
                              [:nested-style]
                              conj
                              [class [(join-keywords ":" [:& key]) style]])
      :else (update-in acc
                       [:style]
                       assoc
                       key
                       style))))

(defn localise-class
  [id key with-dot?]
  (str (when with-dot? ".") (name key) "-" id))

(defn gen-garden-style
  [bps id [key style]]
  (let [localised-class (localise-class id key true)
        {:keys [nested-style responsive-style style]} (reduce (partial build-style-map bps localised-class)
                                                              {:nested-style []
                                                               :responsive-style []
                                                               :style {}}
                                                               style)
        sorted-responsive-style (mapcat (fn [{:keys [id]}]
                                          (filter #(do
                                                     (= id (:id %)))
                                                  responsive-style))
                                        bps)]
    ; order matters here to respect the "mobile first" approach, the media-queries will be declared
    ; according to the order with which the breakpoints are provided
    (concat [[localised-class style]] nested-style (into [] sorted-responsive-style))))

(defmacro defstylecomponent
  [style-id bps style-map & [test-flag]]
  (let [id (if test-flag "test-id" (gensym))
        css (symbol "garden.core" "css")
        at-media-symbol (symbol "garden.stylesheet" "at-media")
        inject-style-fn (symbol "cljs-css-modules.runtime" "inject-style!")
        garden-style (into [] (mapcat (partial gen-garden-style bps id) style-map))
        class-map (into {} (map (fn [[key val]]
                                 [key (localise-class id key false)]) style-map))
        css-expression `(~css {:pretty-print? false}
                              (map #(if (and (map? %) (= (:type %) :media-query))
                                    (~at-media-symbol
                                      (:params %)
                                      (:content %)) ; if media-query create it
                                    %) ; else use garden style straight away
                              ~garden-style))]
    (if test-flag
      {:css css-expression
       :map class-map}
      `(do
         (def ~style-id ~class-map)
         (~inject-style-fn ~css-expression
           ~(str *ns*)
           ~(name style-id))))))
