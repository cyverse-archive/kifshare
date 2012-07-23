(ns kifshare.server
  (:gen-class)
  (:require [noir.server :as server]
            [clojure.tools.cli :as cli]
            [clj-jargon.jargon :as jargon]
            [clojure-commons.clavin-client :as cl]
            [clojure-commons.props :as prps]
            [clojure.tools.logging :as log]
            [kifshare.config :as cfg]
            [clojure.string :as string])
  (:use [clojure-commons.error-codes]))

(def props (atom nil))

(defn jargon-init
  []
  (jargon/init
    (get @cfg/props "kifshare.irods.host")
    (get @cfg/props "kifshare.irods.port")
    (get @cfg/props "kifshare.irods.user")
    (get @cfg/props "kifshare.irods.password")
    (get @cfg/props "kifshare.irods.home")
    (get @cfg/props "kifshare.irods.zone")
    (get @cfg/props "kifshare.irods.defaultResource")))

(defn init []
  (let [tmp-props (prps/parse-properties "zkhosts.properties")
        zkurl (get tmp-props "zookeeper")]
    (cl/with-zk
      zkurl
      (when-not (cl/can-run?)
        (log/warn "THIS APPLICATION CANNOT RUN ON THIS MACHINE. SO SAYETH ZOOKEEPER.")
        (log/warn "THIS APPLICATION WILL NOT EXECUTE CORRECTLY."))
      
      (reset! cfg/props (cl/properties "kifshare")))) 
  
  ; Sets up the connection to iRODS through jargon-core.
  (jargon-init))

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
        (System/exit 0))
      
      (:config opts)
      (do
        (cfg/local-init (:config opts))
        (jargon-init))
      
      :else
      (init))
    
    (let [port (Integer/parseInt (string/trim (get @cfg/props "kifshare.app.port")))
          mode (get @cfg/props "kifshare.app.mode")] 
      (server/start 
        port 
        {:mode (keyword mode)
         :ns 'kifshare}))))

