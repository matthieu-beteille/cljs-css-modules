(ns cljs-css-modules.core-test
  (:require [clojure.test :refer :all]
            [cljs-css-modules.macro :refer :all]
            [cljs-css-modules.macro :refer :all]
            [cljs-css-modules.macro-style-component :as comp]
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

(def mobile 200)
(def dix "10px")

(deftest defstyle-macro
  (testing "defstyle macro should return a map containing an id for each class "
    (let [{:keys [map css] :as style} (defstyle test [[.class-1 {:margin "50px"}]
                                                      ["@keyframes keyframe-test" [:from {:a 50}]
                                                                                   [:to  {:b 50}]]
                                                      ["#id-test" {:margin dix}]
                                                      [.class-2 .lol {:margin "50px"}]
                                                      [.class-3 {:margin-top "60px"
                                                                :padding "50px"}]] true)]
      (is (contains? map :class-1))
      (is (contains? map :class-2))
      (is (contains? map :class-3))
      (is (contains? map :keyframe-test))
      (is (= (count map) 4)))))

;; style components tests

(deftest join-keywords
  (testing "join-keywords"
    (is (= (comp/join-keywords "-" [:symbol1 :symbol2])
           :symbol1-symbol2))))

(deftest defcomp-macro
  (testing "defstylecomponent"
    (let [{:keys [map css] :as res} (comp/defstylecomponent test

                                                   [{:id :mobile
                                                     :max-width mobile}
                                                    {:id :tablet
                                                     :min-width 400}
                                                    {:id :desktop
                                                     :min-width 800}]

                                                   {:header {:margin-top dix
                                                             :padding "60px"
                                                             :hover {:color "black"}
                                                             :mobile {:margin "50px"}
                                                             :tablet {:padding "70px"
                                                                      :active {:color "green"}
                                                                      :hover {:padding "100px"}}
                                                             :desktop {:margin "100px"}}
                                                    :container {:desktop {:margin-top 80}
                                                                :mobile {:margin-top 50}
                                                                :margin-top "10px"
                                                                :tablet {:margin-bottom 60}
                                                                :hover {:color "black"}}} true)]
      (is (= css (str ".header-test-id{margin-top:10px;padding:60px}"
                      ".header-test-id:hover{color:black}"
                      "@media(max-width:200){"
                      ".header-test-id{margin:50px}"
                      "}"
                      "@media(min-width:400){"
                      ".header-test-id{padding:70px}"
                      ".header-test-id:active{color:green}"
                      ".header-test-id:hover{padding:100px}"
                      "}"
                      "@media(min-width:800){.header-test-id{margin:100px}}"
                      ".container-test-id{margin-top:10px}"
                      ".container-test-id:hover{color:black}"
                      "@media(max-width:200){"
                      ".container-test-id{margin-top:50}}"
                      "@media(min-width:400){"
                      ".container-test-id{margin-bottom:60}"
                      "}"
                      "@media(min-width:800){"
                      ".container-test-id{margin-top:80}"
                      "}")))
      (is (= map {:container "container-test-id"
                  :header "header-test-id"}))
      (is (contains? map :header))
      (is (contains? map :container))
      (is (count map) 2)
      (is (string? css)))))
