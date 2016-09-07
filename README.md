# Css Modules in ClojureScript

[![Clojars Project](https://img.shields.io/clojars/v/cljs-css-modules.svg)](https://clojars.org/cljs-css-modules)

First if you don't know what's a css modules, you should definitely read the specification:
https://github.com/css-modules/css-modules

Using cljs-css-modules, you won't write pure CSS, but you'll use [garden](https://github.com/noprompt/garden) syntax to write your style in ClojureScript.   
([Garden](https://github.com/noprompt/garden) basically allows you to use any feature of pure css, so no worries you're not losing any power here).

The idea of cljs-css-modules is to localise every classes (and soon animations) you define through the ```defstyle``` macro.

## Example Project

gmp26 put together a repository porting an original css-modules project over to a cljs-css-modules/cljs one.

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

Each time you'll define some style using ```defstyle```, this style will be localised (every classes (and soon every animations)), translated to CSS,
and automatically injected into the ```<head>``` tag. This works perfectly with figwheel, and you'll get live style reloading out of the box.

(If you come from the javascript world and you've used webpack before, it replaces both *css-loader* and *style-loader*).

*Example:*

Define your style:

```Clojure
(ns yourapp.namespace1
  (:require [cljs-css-modules.macro :refer-macros [defstyle]]))

(defstyle style
  [[".container" {:background-color "blue"
                  :font-size 55}
    [:a {:color "green"}]
    [:&:hover {:background-color "black"}]]

   [".text" {:font-size 14
             :color "brown"}]

   [".title" {:background-color "blue"
              :font-size 60}]

   [".title2" {:font-size 40
               :color "red"}]])
```

The localised classes will be available in the style object created.

```Clojure
(:container style) ;; => returns the unique generated class for ".container"
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

## TODO

- localise keyframes
- compose feature?

## License

Copyright © 2016 Matthieu Béteille

Distributed under the Eclipse Public License version 1.0
