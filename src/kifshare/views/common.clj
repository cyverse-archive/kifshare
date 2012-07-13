(ns kifshare.views.common
  (:require [clojure.string :as string]
            [kifshare.config :as cfg])
  (:use [noir.core :only [defpartial]]
        [hiccup.page :only [include-css include-js html5]]))

(defn parse-accept-headers
  "Parses out the accept headers and returns a list
   of the acceptable content types."
  [request]
  (string/split (get-in request [:headers "accept"]) #","))

(defn show-html?
  "Checks to see if 'text/html' is in the list of
   acceptable content-types in the Accept header."
  [request]
  (contains? (set (parse-accept-headers request)) "text/html"))

(defpartial html-head []
  [:head
   [:title "iPlant Public Downloads"]
   (map include-css (cfg/css-files))
   (map include-js (cfg/javascript-files))])

(defpartial layout [& content]
            (html5
              (html-head)
              [:body
               [:div#wrapper {:id "page-wrapper" :class "container_12"}
                content]]))
