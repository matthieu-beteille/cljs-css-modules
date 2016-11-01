# Css Modules in ClojureScript

[![Clojars Project](https://img.shields.io/clojars/v/cljs-css-modules.svg)](https://clojars.org/cljs-css-modules)

First if you don't know what's a css modules, you should definitely read the specification:
https://github.com/css-modules/css-modules

Using cljs-css-modules, you won't write pure CSS, but you'll use [garden](https://github.com/noprompt/garden) syntax to write your style in ClojureScript.   
([Garden](https://github.com/noprompt/garden) basically allows you to use any feature of pure css, so no worries you're not losing any power here).

The idea of cljs-css-modules is to localise every classes (and soon animations) you define through the ```defstyle``` macro.

## Example Project

gmp26 put together a repository porting a css-modules project over to a cljs-css-modules/cljs one.

You can check it out, to see how this library can be used in a real project:

- Original JS Repo:
https://github.com/css-modules/webpack-demo
- CLJS Repo:
https://github.com/gmp26/css-modules-tester

## Usage

Add this to your ```project.clj```:  

<img src="https://clojars.org/cljs-css-modules/latest-version.svg"/>

You need to define your style using the ```defstyle``` macro.

Your style will be written using [garden](https://github.com/noprompt/garden) syntax, so spend some time reading the [doc](https://github.com/noprompt/garden).

Each time you'll define some style using ```defstyle```, this style will be localised (every classes and animations), translated to CSS,
and automatically injected into the ```<head>``` tag. This works perfectly with figwheel, and you'll get live style reloading out of the box.

(If you come from the javascript world and you've used webpack before, it replaces both *css-loader* and *style-loader*).

*Example:*

Define your style:

```Clojure
(ns yourapp.namespace1
  (:require [cljs-css-modules.macro :refer-macros [defstyle]]))

(defstyle style

  (at-media {:max-width "200px"}

            [".mobile-style-1" {:margin "5px"}]

            [".mobile-style-2" {:margin "10px"}])

  [".container" {:background-color "blue"
                  :font-size 55}
    ["a" {:color "green"}]
    ["&:hover" {:background-color "black"}]]

   [".text" {:font-size 14
             :color "brown"}]

   (at-keyframes "keyframe-1" [:from {:a 50}]
                          [:to  {:b 50}])

   ["@keyframes keyframe-2" [:from {:a 50}]
                          [:to  {:b 50}]]

   [".title" {:background-color "blue"
              :font-size 60}]

   [".title2" {:font-size 40
               :color "red"}])
```

The localised classes/keyframes will be available in the style object created.
(Note: including classes in media queries).

```Clojure
(:container style) ;; => returns the unique generated class for ".container"
(:mobile-style-1 style) ;; => returns the unique generated class for ".mobile-style-1"
```

To use your style, you just need to inject them wherever you need:

For instance with reagent:

```Clojure
(defn simple-component []
  [:div {:class-name (:container style)}
   [:h1 {:class-name (:title style)} "I am a big title"]
   [:h1 {:class-name (:title2 style)} "I am smaller title"]
   [:p {:class-name (:text style)}
    "Here goes some random text"]])
```
## Media queries

To define a media query you need to use the ```(at-media)``` form, and nest your style in it.
It's the same as garden's syntax, here is the documentation:
https://github.com/noprompt/garden/wiki/Media-Queries

Example:

```Clojure
(defstyle style
  (at-media {:max-width "400px"}

            [".mobile-style" {:margin "5px"}])

  (at-media {:min-width "400px"
             :max-width "800px"}

            [".tablet-style-1" {:margin "5px"}]

            [".tablet-style-2" {:margin "10px"}]))
```

This will localise all the classes in your media queries, here: .mobile-style, .tablet-style-1, .tablet-style-2.

Note:
Using cljs-css-modules, you don't need to import the at-media function from garden's library. The macro will recognise the at-media symbol.

## Keyframes

To define an animation you need to use the ```(at-keyframes)``` form, or a string like "@keyframes animation-name":

Example:

```Clojure
(defstyle style

  (at-keyframes "animation-1"
                [:from {:top "0px"}]
                [:to {:top "200px"}])

  (at-keyframes "animation-2"
                [:from {:top "0px"}]
                [:to {:top "250px"}])

  ["@keyframes animation-3" [:from {:top "0px"}]
                            [:to {:top "250px"}]])
```

This will localise all the animations, here: animation-1, animation-2, animation-3.

Note:
Using cljs-css-modules, you don't need to import the at-keyframes function from garden's library. The macro will recognise the at-keyframes symbol.

## Note

You might want to consider https://github.com/mhallin/forest which is not based on garden and provides the same kind of features using its own DSL.

## License

Copyright © 2016 Matthieu Béteille

Distributed under the Eclipse Public License version 1.0
