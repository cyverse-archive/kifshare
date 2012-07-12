(ns kifshare.config
  (:require [clojure.string :as string]))

(def props (atom nil))

(defn init-config
  [new-props]
  (reset! props new-props))

(defn curl-flags
  []
  (get @props "kifshare.app.curl-flags"))

(defn wget-flags
  []
  (get @props "kifshare.app.wget-flags"))

(defn iget-flags
  []
  (get @props "kifshare.app.iget-flags"))

(defn external-url
  []
  (get @props "kifshare.app.external-url"))

(defn css-files
  []
  (mapv 
    string/trim 
    (string/split 
      (get @props "kifshare.app.css-files") 
      #",")))

(defn javascript-files
  []
  (mapv 
    string/trim 
    (string/split 
      (get @props "kifshare.app.javascript-files") 
      #",")))