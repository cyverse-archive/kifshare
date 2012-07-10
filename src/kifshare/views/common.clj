(ns kifshare.views.common
  (:require [clojure.string :as string])
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

(def js-includes
  {:jquery (include-js "https://ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js")
   :dtable (include-js "http://ajax.aspnetcdn.com/ajax/jquery.dataTables/1.8.2/jquery.dataTables.min.js")
   :kif.js (include-js "/js/kif.js")})

(def css-includes
  {#_(:default (include-css "/css/default.css"))
   :reset   (include-css "/css/reset.css")
   :960     (include-css "/css/960.css")
   :kif     (include-css "/css/kif.css")
   :dtable  (include-css "http://ajax.aspnetcdn.com/ajax/jquery.dataTables/1.8.2/css/jquery.dataTables.css")})

(defpartial html-head []
  [:head
   [:title "iPlant Public Downloads"]
   (map #(get css-includes %) (keys css-includes))
   (map #(get js-includes %) (keys js-includes))])

(defpartial layout [& content]
            (html5
              (html-head)
              [:body
               [:div#wrapper {:class "container_12"}
                content]]))
