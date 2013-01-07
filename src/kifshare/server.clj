(ns kifshare.server
  (:gen-class)
  (:require [noir.server :as server]
            [clojure.tools.cli :as cli]
            [clj-jargon.jargon :as jargon]
            [clojure-commons.clavin-client :as cl]
            [clojure-commons.props :as prps]
            #_([clojure.tools.logging :as log])
            [kifshare.config :as cfg]
            [clojure.string :as string])
  (:use [clojure-commons.error-codes]))

(defn init []
  #_(log/debug "entered kifshare.server/init")
  (let [tmp-props (prps/parse-properties "zkhosts.properties")
        zkurl (get tmp-props "zookeeper")]
    #_(log/debug "zookeeper URL: " zkurl)
    (cl/with-zk
      zkurl
      (when-not (cl/can-run?)
        #_(log/warn "THIS APPLICATION CANNOT RUN ON THIS MACHINE. SO SAYETH ZOOKEEPER.")
        #_(log/warn "THIS APPLICATION WILL NOT EXECUTE CORRECTLY."))
      
      (reset! cfg/props (cl/properties "kifshare")))) 

  (cfg/log-config)
  
  ; Sets up the connection to iRODS through jargon-core.
  (cfg/jargon-init))

(defn parse-args
  [args]
  #_(log/debug "entered kifshare.server/parse-args")
  
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
  (println "WTF")
  (let [[opts args help-str] (parse-args args)]
    (cond      
      (:help opts)
      (do (println help-str)
        (System/exit 0))
      
      (:config opts)
      (do
        #_(log/warn "Reading local config: " (:config opts))
        (cfg/local-init (:config opts))
        (cfg/jargon-init))
      
      :else
      (init))
    
    (let [port (Integer/parseInt (string/trim (get @cfg/props "kifshare.app.port")))
          mode (get @cfg/props "kifshare.app.mode")]
      #_(log/warn "Configured listen port is: " port)
      #_(log/warn "Configured mode is: " mode)
      
      (server/start 
        port 
        {:mode (keyword mode)
         :ns 'kifshare}))))

