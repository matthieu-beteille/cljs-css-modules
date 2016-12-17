(ns cljs-css-modules.macro-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [cljs-css-modules.macro :refer-macros [defstyle]]))

(deftest defstyle-macro
  (testing "defstyle macro should return a map containing an id for each class "
    (defstyle style
      {:pretty-print? false}
      (at-keyframes "keyframe-1"
                    [:from {:top "50px"}]
                    [:to  {:top "150px"}])
      (at-media {:min-width "200px"
                 :max-width "500px"}
                [:.query-test {:margin "60px"}
                 [:&:hover {:color "black"}]]
                [:h2 {:padding "10px"}])
      (at-keyframes "animation-1"
                    [:from {:top "0px"}]
                    [:to {:top "200px"}])
      [".container" {:margin "50px"}
       ["a" {:color "blue"}]]
      [".class-1" {:margin "50px"}]
      ["@keyframes keyframe-2" [:from {:margin "50px"}]
       [:to  {:margin "100px"}]]
      ["#ida" {:margin "10px"}]
      [".class-2" ".lol" {:margin "50px"}]
      [".class-3" {:margin-top "60px"
                   :padding "50px"}] true)
    (is (= (.-innerHTML (.querySelector js/document "head > style"))
           (str
            "@keyframes keyframe-1--test{"
            "from{top:50px}"
            "to{top:150px}"
            "}"
            "@media(min-width:200px) and(max-width:500px){"
            ".query-test--test{margin:60px}"
            ".query-test--test:hover{color:black}"
            "h2{padding:10px}"
            "}"
            "@keyframes animation-1--test{"
            "from{top:0px}"
            "to{top:200px}"
            "}"
            ".container--test{margin:50px}"
            ".container--test a{color:blue}"
            ".class-1--test{margin:50px}"
            "@keyframes keyframe-2--test{"
            "from{margin:50px}to{margin:100px}"
            "}"
            "#ida{margin:10px}"
            ".class-2--test,.lol{margin:50px}"
            ".class-3--test{margin-top:60px;padding:50px}")))
    (is (= (:class-1 style) "class-1--test"))
    (is (= (:class-2 style) "class-2--test"))
    (is (= (:class-3 style) "class-3--test"))
    (is (= (:query-test style) "query-test--test"))
    (is (= (:keyframe-1 style) "keyframe-1--test"))
    (is (= (:keyframe-2 style) "keyframe-2--test"))
    (is (= (:container style) "container--test"))
    (is (= (:animation-1 style) "animation-1--test"))
    (is (= (count style) 8))))
