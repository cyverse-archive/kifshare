(ns kifshare.server
  (:require [noir.server :as server]
            [clojure.tools.cli :as cli]
            [clj-jargon.jargon :as jargon]
            [clojure-commons.props :as prps]
            [clojure-commons.clavin-client :as cl]
            [clojure.tools.logging :as log])
  (:use [clojure-commons.error-codes]))

(def props (atom nil))

(defn jargon-init
  []
  (jargon/init
    (get @props "kifshare.irods.host")
    (get @props "kifshare.irods.port")
    (get @props "kifshare.irods.user")
    (get @props "kifshare.irods.password")
    (get @props "kifshare.irods.home")
    (get @props "kifshare.irods.zone")
    (get @props "kifshare.irods.defaultResource")))

(defn init []
  (let [tmp-props (prps/parse-properties "zkhosts.properties")
        zkurl (get tmp-props "zookeeper")]
    (cl/with-zk
      zkurl
      (when-not (cl/can-run?)
        (log/warn "THIS APPLICATION CANNOT RUN ON THIS MACHINE. SO SAYETH ZOOKEEPER.")
        (log/warn "THIS APPLICATION WILL NOT EXECUTE CORRECTLY."))
      
      (reset! props (cl/properties "kifshare")))) 
  
  ; Sets up the connection to iRODS through jargon-core.
  (jargon-init))

(defn local-init
  [local-config-path]
  (let [main-props (prps/read-properties local-config-path)]
    (reset! props main-props)
    (jargon-init)))

(defn parse-args
  [args]
  (cli/cli
   args
    ["-c" "--config" 
     "Set the local config file to read from. Bypasses Zookeeper" 
     :default nil]
    ["-h" "--help" 
     "Show help." 
     :default false 
     :flag true]))

(server/load-views-ns 'kifshare.views)

(defn -main [& args]
  (let [[opts args help-str] (parse-args args)]
    (cond      
      (:help opts)
      (do (println help-str)
        (System/exit 0)))

    (if (:config opts)
      (local-init (:config opts))
      (init))
  
    (let [port (Integer/parseInt (get @props "kifshare.app.port"))
          mode (get @props "kifshare.app.mode")] 
      (server/start 
        port 
        {:mode (keyword mode)
         :ns 'kifshare}))))

