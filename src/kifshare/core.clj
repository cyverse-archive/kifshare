(ns kifshare.core
  (:gen-class)
  (:use compojure.core
        kifshare.config
        [ring.middleware
         params
         keyword-params
         nested-params
         multipart-params
         cookies
         session
         stacktrace]
        [slingshot.slingshot :only [try+ throw+]])
  (:require [clojure.tools.cli :as cli]
            [clojure.java.io :as io]
            [clj-jargon.jargon :as jargon]
            [clojure-commons.clavin-client :as cl]
            [clojure-commons.props :as prps]
            [clojure-commons.file-utils :as ft]
            [clojure.tools.logging :as log]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [ring.adapter.jetty :as jetty]
            [ring.util.response :as resp]
            [kifshare.config :as cfg]
            [kifshare.controllers :as controllers]
            [clojure.string :as string])
  (:use [clojure-commons.error-codes]))

;;(prps/find-resources-file
;;                           (cfg/favicon-path))

(defn keep-alive
  [resp]
  (assoc-in resp [:headers "Connection"] "keep-alive"))

(defn caching
  [resp]
  (assoc-in resp [:headers "Cache-Control"]
            (str (cfg/client-cache-scope) ", max-age=" (cfg/client-cache-max-age))))

(defn kif-static-resp
  [req resp]
  (-> resp
      keep-alive
      caching))

(defroutes kifshare-routes
  (GET "/favicon.ico"
       req
       (kif-static-resp req (resp/file-response (cfg/favicon-path) {:root (cfg/resources-root)})))

  (GET "/resources/:rsc-name"
       [rsc-name :as req]
       (kif-static-resp req (resp/file-response rsc-name {:root (cfg/resources-root)})))

  (GET "/resources/css/:rsc-name"
       [rsc-name :as req]
       (let [resource-root (ft/path-join (cfg/resources-root) (cfg/css-dir))]
         (kif-static-resp req (resp/file-response rsc-name {:root resource-root}))))

  (GET "/resources/js/:rsc-name"
       [rsc-name :as req]
       (let [resource-root (ft/path-join (cfg/resources-root) (cfg/js-dir))]
         (kif-static-resp req (resp/file-response rsc-name {:root resource-root}))))

  (GET "/resources/flash/:rsc-name"
       [rsc-name :as req]
       (let [resource-root (ft/path-join (cfg/resources-root) (cfg/flash-dir))]
         (kif-static-resp req (resp/file-response rsc-name {:root resource-root}))))

  (GET "/resources/img/:rsc-name"
       [rsc-name :as req]
       (let [resource-root (ft/path-join (cfg/resources-root) (cfg/img-dir))]
         (kif-static-resp req (resp/file-response rsc-name {:root resource-root}))))

  (GET "/robots.txt"
       req
       (kif-static-resp req
                        (resp/file-response (cfg/robots-txt-path) {:root (cfg/resources-root)})))

  (GET "/d/:ticket-id/:filename" [ticket-id filename :as request]
       (controllers/download-file ticket-id filename request))

  (GET "/d/:ticket-id" [ticket-id :as request]
       (controllers/download-ticket ticket-id request))

  (GET "/:ticket-id" [ticket-id :as request]
       (controllers/get-ticket ticket-id request))

  (route/resources "/")

  (route/not-found "Not found!"))

(defn site-handler [routes]
  (-> routes
      wrap-multipart-params
      wrap-keyword-params
      wrap-nested-params
      wrap-params
      wrap-stacktrace))

(defn parse-args
  [args]
  (log/debug "entered kifshare.core/parse-args")

  (cli/cli
   args
    ["-c" "--config"
     "Set the local config file to read from. Bypasses Zookeeper"
     :default nil]
    ["-h" "--help"
     "Show help."
     :default false
     :flag true]))

(def app
  (site-handler kifshare-routes))

(defn -main
  [& args]
  (log/debug "entered kifshare.core/-main")

  (let [[opts args help-str] (parse-args args)]
    (cond
      (:help opts)
      (do (println help-str)
        (System/exit 0))

      (:config opts)
      (do
        (log/warn "Reading local config: " (:config opts))
        (cfg/local-init (:config opts))
        (cfg/jargon-init))

      :else
      (init))

    (controllers/start-provenance-thread)

    (let [port (Integer/parseInt (string/trim (get @cfg/props "kifshare.app.port")))]
      (log/warn "Configured listen port is: " port)
      (jetty/run-jetty app {:port port}))))
