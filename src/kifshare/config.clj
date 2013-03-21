(ns kifshare.config
  (:require [clojure.string :as string]
            [clj-jargon.jargon :as jargon]
            [clojure-commons.clavin-client :as cl]
            [clojure-commons.props :as prps]
            [clojure.tools.logging :as log]))

(def props (atom nil))

(def robots-txt (atom ""))

(defn robots-txt-path
  []
  (get @props "kifshare.app.robots-txt"))

(defn morbixon-url
  []
  (get @props "kifshare.app.morbixon-url"))

(defn service-name
  []
  (get @props "kifshare.app.service-name"))

(defn service-version
  []
  (get @props "kifshare.app.service-version"))

(defn robots-txt-content
  []
  @robots-txt)

(defn local-init
  [local-config-path]
  (let [main-props (prps/read-properties local-config-path)]
    (reset! props main-props)
    #_(reset! robots-txt (slurp (robots-txt-path)))))

(defn resources-root
  []
  (get @props "kifshare.app.resources-root"))

(defn de-url
  []
  (get @props "kifshare.app.de-url"))

(defn irods-url
  []
  (get @props "kifshare.app.irods-url"))

(defn logo-path
  []
  (get @props "kifshare.app.logo-path"))

(defn favicon-path
  []
  (get @props "kifshare.app.favicon-path"))

(defn de-import-flags
  []
  (get @props "kifshare.app.de-import-flags"))

(defn footer-text
  []
  (get @props "kifshare.app.footer-text"))

(defn curl-flags
  []
  (get @props "kifshare.app.curl-flags"))

(defn wget-flags
  []
  (get @props "kifshare.app.wget-flags"))

(defn iget-flags
  []
  (get @props "kifshare.app.iget-flags"))

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

(defn log-config
  []
  (log/warn "Configuration:")
  (doseq [k (keys @props)]
    (when-not (= k "kifshare.irods.password")
      (log/warn (str k " = " (get @props k))))))

(defn init []
  (log/debug "entered kifshare.config/init")

  (let [tmp-props (prps/parse-properties "zkhosts.properties")
        zkurl (get tmp-props "zookeeper")]
    (log/warn "zookeeper URL: " zkurl)

    (cl/with-zk
      zkurl
      (when-not (cl/can-run?)
        (log/error "THIS APPLICATION CANNOT RUN ON THIS MACHINE. SO SAYETH ZOOKEEPER.")
        (log/error "THIS APPLICATION WILL NOT EXECUTE CORRECTLY."))

      (reset! props (cl/properties "kifshare"))
      (reset! robots-txt (slurp (robots-txt-path)))))

  (log-config)

  ; Sets up the connection to iRODS through jargon-core.
  (jargon-init))