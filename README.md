# cljs-css-modules

First, if you don't know what's a css modules, here is the specification (you should read it first):  
https://github.com/css-modules/css-modules


In cljs you won't write pure CSS, but you'll use [garden](https://github.com/noprompt/garden) to write your style in cljs.
(It basically allows you to use any feature of pure css, so no worries you're not losing any power here).

The idea of cljs-css-modules is to localise every classes and animations you define through the ```defstyle``` macro.

## Usage

You need to define your style using the ```defstyle``` macro.

Your style will be written using [garden](https://github.com/noprompt/garden) syntax, so spend some time to check it out: https://github.com/noprompt/garden

Each time you'll define some style using ```style```, this style will be  localised (class, and animations), translated to CSS,
and automatically injected into the ```<head>``` tag. This works perfectly with figwheel, and you'll get live style reloading out of the box.

(If you come from javascript and you've used webpack before, it replaces *css-loader*, and *style-loader*)

Example:

Define your style:

```
(ns yourapp.ns1
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

   ["@keyframes test" {:start 60}]

   ; that's how you can re-use a defined animation
   [".title2" {:font-size 40
               :color "red"
               :animation (str (:test style) " 1s loop")}]])
```

The localised classes will be available in the style object created.

```(:container style) ;; => returns the unique generated class for ".container"```

To use your style, you just need to inject them wherever you need:

For instance with reagent:

```
(defn simple-component []
  [:div {:class-name (:container style)}
   [:h1 {:class-name (:title style)} "I am a big title"]
   [:h1 {:class-name (:title2 style)} "I am smaller title"]
   [:p {:class-name (:text style)}
    "Here goes some random text"]])
```

## License

Copyright © 2016 Matthieu Béteille

Distributed under the Eclipse Public License version 1.0
