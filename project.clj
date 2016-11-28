(defproject cljs-css-modules "0.2.1"
  :description "cljs-css-modules: css modules in clojurescript"

  :url "https://github.com/matthieu-beteille/cljs-css-modules"

  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.8.34"]
                 [garden "1.3.2"]]

  :test-paths ["test/clj"]

  :plugins [[lein-auto "0.1.2"]
            [lein-doo "0.1.6"]
            [lein-cljsbuild "1.1.4"]]

  :cljsbuild
  {:builds
   [{:id           "test"
     :source-paths ["src/" "test/cljs"]
     :compiler     {:output-to     "resources/public/test/app.test.js"
                    :output-dir    "resources/public/test/out"
                    :main          'cljs-css-modules.runner
                    :optimizations :none}}]}

  :auto {:default {:file-pattern #"\.(clj|cljs|cljc|edn)$"}})
