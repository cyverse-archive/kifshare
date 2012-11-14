(ns kifshare.config
  (:require [clojure.string :as string]
            [clj-jargon.jargon :as jargon]
            [clojure-commons.props :as prps]))

(def props (atom nil))

(defn local-init
  [local-config-path]
  (let [main-props (prps/read-properties local-config-path)]
    (reset! props main-props)))

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

(defn username
  []
  (or (get @props "kifshare.irods.user")
      "public"))

(defn prov-url
  []
  (get @props "kifshare.provenance.base-url"))

(defn prov-lookup-endpoint
  []
  (get @props "kifshare.provenance.lookup"))

(defn prov-register-endpoint
  []
  (get @props "kifshare.provenance.register"))

(defn prov-logging-endpoint
  []
  (get @props "kifshare.provenance.logging"))

(def jgcfg (atom nil))

(defn jargon-config [] @jgcfg)

(defn jargon-init
  []
  (reset! jgcfg
          (jargon/init
           (get @props "kifshare.irods.host")
           (get @props "kifshare.irods.port")
           (get @props "kifshare.irods.user")
           (get @props "kifshare.irods.password")
           (get @props "kifshare.irods.home")
           (get @props "kifshare.irods.zone")
           (get @props "kifshare.irods.defaultResource"))))

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