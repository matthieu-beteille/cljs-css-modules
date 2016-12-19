(ns cljs-css-modules.core-test
  (:require [clojure.test :refer :all]
            [cljs-css-modules.macro :refer :all]
            [cljs-css-modules.core :refer :all]))

; style tests

(def class-object (get selectors-to-localise 0))
(def keyframe-object (get selectors-to-localise 1))

(deftest should-be-localised-success
  (testing "should-be-localised function"
    (are [selector] (= class-object (should-be-localised selector))
      ".class1",
      ".container",
      ".navbar"
      ".navbar:hover")))

(deftest should-be-localised-failure
  (testing "should-be-localised error"
    (are [selector] (= nil (should-be-localised selector))
      "div",
      "a",
      "img")))

(deftest localise-class
  (testing "It should localise a class properly"
    (is (= ".class--id" (localise-selector "id" ".class" class-object)))
    (is (= ".class--id[foo=bla]" (localise-selector "id" ".class[foo=bla]" class-object)))
    (is (= ".class--id#id" (localise-selector "id" ".class#id" class-object)))
    (is (= ".class--id:pseudoclass" (localise-selector "id" ".class:pseudoclass" class-object)))
    (is (= ".class1--id.class2" (localise-selector "id" ".class1.class2" class-object)))
    (is (= ".class--id > selector" (localise-selector "id" ".class > selector" class-object)))
    (is (= ".class--id + selector" (localise-selector "id" ".class + selector" class-object)))
    (is (= ".class--id selector" (localise-selector "id" ".class selector" class-object)))))

(deftest get-class-key
  (testing "It should create a symbol key with the name of the class"
    (are [key selector] (= key (get-selector-key selector class-object))
    :class ".class"
    :class ".class[foo=bla]"
    :class ".class#id"
    :class ".class:pseudoclass"
    :class1 ".class1.class2"
    :class ".class > selector"
    :class ".class + selector"
    :class ".class selector"
    :class ".class")))

(deftest get-class-value
  (testing "It should create a value key with the name of the class (without the .)"
    (is (= "class" (get-selector-value ".class" class-object)))
    (is (= "class" (get-selector-value ".class[foo=bla]" class-object)))
    (is (= "class" (get-selector-value ".class#id" class-object)))
    (is (= "class" (get-selector-value ".class:pseudoclass" class-object)))
    (is (= "class1" (get-selector-value ".class1.class2" class-object)))
    (is (= "class" (get-selector-value ".class > selector" class-object)))
    (is (= "class" (get-selector-value ".class + selector" class-object)))
    (is (= "class" (get-selector-value ".class selector" class-object)))
    (is (= "class" (get-selector-value ".class" class-object)))))

(deftest localise-keyframe
  (testing "It should localise a keyframe properly"
    (is (= "test--id" (localise-selector "id" "@keyframes test" keyframe-object)))))

(deftest get-keyframe-key
  (testing "It should create a symbol key with the name of the keyframe"
    (is (= :test (get-selector-key "@keyframes test" keyframe-object)))))

(def mobile 200)
(def dix "10px")

(deftest defstyle-macro
  (testing "defstyle macro should return a map containing an id for each class "
    (defstyle test
      {:pretty-print? false}
      (at-keyframes "keyframe-1"
                    [:from {:top "50px"}]
                    [:to  {:top "150px"}])
      (at-media {:min-width "500px"
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
      ["#ida" {:margin dix}]
      [".class-2" ".lol" {:margin "50px"}]
      [".class-3" {:margin-top "60px"
                   :padding "50px"}] true)
    (let [{:keys [map css] :as style} test]
      (is (= css
             (str
              "@keyframes keyframe-1--test{"
              "from{top:50px}"
              "to{top:150px}"
              "}"
              "@media(min-width:500px) and (max-width:500px){"
              ".query-test--test{margin:60px}"
              ".query-test--test:hover{color:black}"
              "h2{padding:10px}"
              "}"
              "@keyframes animation-1--test{"
              "from{top:0}"
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
      (is (= (:class-1 map) "class-1--test"))
      (is (= (:class-2 map) "class-2--test"))
      (is (= (:class-3 map) "class-3--test"))
      (is (= (:query-test map) "query-test--test"))
      (is (= (:keyframe-1 map) "keyframe-1--test"))
      (is (= (:keyframe-2 map) "keyframe-2--test"))
      (is (= (:container map) "container--test"))
      (is (= (:animation-1 map) "animation-1--test"))
      (is (= (count map) 8)))))
