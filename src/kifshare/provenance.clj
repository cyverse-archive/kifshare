(ns kifshare.provenance
  (:use [slingshot.slingshot :only [try+ throw+]])
  (:require [kifshare.config :as cfg]
            [cemerick.url :as url]
            [clj-http.client :as cl]
            [clojure.tools.logging :as log]
            [clojure.data.json :as json]))

(defn- join-lookup-url
  []
  (url/url (cfg/prov-url) (cfg/prov-lookup-endpoint)))

(defn- join-register-url
  []
  (url/url (cfg/prov-url) (cfg/prov-register-endpoint)))

(defn- join-logging-url
  []
  (url/url (cfg/prov-url) (cfg/prov-logging-endpoint)))

(def lookup-url (memoize join-lookup-url))
(def register-url (memoize join-register-url))
(def logging-url (memoize join-logging-url))

(def json-get (comp json/read-json :body cl/get))

(def excepts
  {:throw-exceptions true
   :throw-entire-message? true})

(defn do-get
  ([full-url]
    (do-get full-url :UUID))
  ([full-url extractor]
    (try+
      (extractor (json-get full-url excepts))
      (catch #(contains? % :status) {:keys [status body]}
        (log/warn (str "Received " status " doing a lookup on " full-url))
        nil)
      (catch Exception e
        (log/warn (str e))
        nil))))

(defn basic-lookup
  [irods-id]
  (do-get (str (assoc (lookup-url) :query {:service_object_id irods-id}))))

(defn basic-register
  [irods-id name desc]
  (let [qobj {:service_object_id irods-id :object_name name :object_desc desc}]
    (do-get (str (assoc (register-url) :query qobj)))))

(defn prov-event
  [event category uuid]
  {:uuid uuid
   :username (cfg/username)
   :service_name "kifshare"
   :event_name event
   :category_name category
   :request_ipaddress (.getHostAddress (java.net.InetAddress/getLocalHost))})

(defn basic-prov-log
  [event category uuid]
  (do-get 
    (str (assoc (logging-url) :query (prov-event))) 
    #(get-in % [:result :Status])))

(defn lookupsert
  [irods-id name desc]
  (let [maybe-uuid (basic-lookup irods-id)]
    (if-not maybe-uuid
      (basic-register irods-id name desc)
      maybe-uuid)))



