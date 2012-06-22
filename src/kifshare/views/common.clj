(ns kifshare.views.common
  (:require [clojure.string :as string])
  (:use [noir.core :only [defpartial]]
        [hiccup.page :only [include-css html5]]))

(defn parse-accept-headers
  [request]
  (string/split (get-in request [:headers "accept"]) #","))

(defn show-html?
  [request]
  (contains? (set (parse-accept-headers request)) "text/html"))

(defpartial layout [& content]
            (html5
              [:head
               [:title "kifshare"]
               (include-css "/css/reset.css")]
              [:body
               [:div#wrapper
                content]]))
