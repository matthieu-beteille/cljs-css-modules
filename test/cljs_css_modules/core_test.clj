(ns cljs-css-modules.core-test
  (:require [clojure.test :refer :all]
            [cljs-css-modules.macro :refer :all]
            [cljs-css-modules.core :refer :all]))

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
    (is (= :class (get-selector-key ".class" class-object)))
    (is (= :class (get-selector-key ".class[foo=bla]" class-object)))
    (is (= :class (get-selector-key ".class#id" class-object)))
    (is (= :class (get-selector-key ".class:pseudoclass" class-object)))
    (is (= :class1 (get-selector-key ".class1.class2" class-object)))
    (is (= :class (get-selector-key ".class > selector" class-object)))
    (is (= :class (get-selector-key ".class + selector" class-object)))
    (is (= :class (get-selector-key ".class selector" class-object)))
    (is (= :class (get-selector-key ".class" class-object)))))

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
    (is (= "@keyframes test--id" (localise-selector "id" "@keyframes test" keyframe-object)))))

(deftest get-keyframe-key
  (testing "It should create a symbol key with the name of the keyframe"
    (is (= :test (get-selector-key "@keyframes test" keyframe-object)))))
