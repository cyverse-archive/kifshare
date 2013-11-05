(ns kifshare.controllers
  (:use [ring.util.response :only [redirect status]]
        [kifshare.config :only [jargon-config]]
        [kifshare.ui-template :only [landing-page]]
        [slingshot.slingshot :only [try+]]
        [clojure-commons.error-codes]
        [lamina.core :only [channel read-channel enqueue]])
  (:require [kifshare.tickets :as tickets]
            [compojure.response :as resp]
            [kifshare.common :as common]
            [cheshire.core :as cheshire]
            [clojure.tools.logging :as log]
            [clj-jargon.metadata :as jmeta]
            [clj-jargon.init :as jinit]
            [kifshare.errors :as errors]
            [kifshare.config :as cfg]
            [clj-http.client :as http]
            [clojure.string :as string]))

(def morbixon-channel (channel))

(defn provenance
  [uid event & {:keys [user category data]
                :or {user (cfg/username)
                     category "tickets"
                     data event}}]
  (when-not (string/blank? (cfg/morbixon-url))
    (let [svc-name    (or (cfg/service-name) "kifshare")
          svc-version (or (cfg/service-version) "0.1.0")
          prov-map    {:service svc-name
                       :version svc-version
                       :events [{:uid uid
                                 :event event
                                 :user user
                                 :category category
                                 :data data}]}]
      (enqueue morbixon-channel prov-map))))

(defn start-provenance-thread
  []
  (when-not (string/blank? (cfg/morbixon-url))
    (.start
     (Thread.
      (fn []
        (loop []
          (http/post
           (cfg/morbixon-url)
           {:body (cheshire/generate-string @(read-channel morbixon-channel))
            :content-type :json})
          (recur)))))))

(defn object-metadata
  [cm abspath]
  (log/debug "kifshare.controllers/object-metadata")

  (filterv
   #(not= (:unit %1) "ipc-system-avu")
   (jmeta/get-metadata cm abspath)))

(defn show-landing-page
  "Handles error checking and decides whether to show the
   landing page or an error page."
  [cm ticket-id ticket-info]
  (log/debug "entered kifshare.controllers/show-landing-page")
  (landing-page
   ticket-id
   (object-metadata cm (tickets/ticket-abs-path cm ticket-id))
   ticket-info))

(defn decide-on-page
  [cm ring-request ticket-id ticket-info]
  (log/debug "entered kifshare.controllers/decide-on-page")
  (if (common/show-html? ring-request)
    {:status 200 :body (show-landing-page cm ticket-id ticket-info)}
    (do (provenance ticket-id "redirect-to-file")
        (redirect (str "d/" ticket-id "/" (:filename ticket-info))))))

(defn error-map-response
  [request err-map]
  (if (common/show-html? request)
    (errors/error-html err-map)
    (errors/error-response err-map)))

(defn get-ticket
  "Determines whether to redirect to a download or show the landing page."
  [ticket-id ring-request]
  (log/debug "entered page kifshare.controllers/get-ticket")

  (jinit/with-jargon (jargon-config) [cm]
    (try+
     (tickets/check-ticket cm ticket-id)

     (let [ticket-info (tickets/ticket-info cm ticket-id)]
       (log/debug "Ticket Info:\n" ticket-info)
       (provenance ticket-id "show-web-ui" :data {:ticket-info ticket-info})
       {:status 200 :body (show-landing-page cm ticket-id ticket-info)})

     (catch error? err
       (log/error (format-exception (:throwable &throw-context)))
       (provenance ticket-id "error-get-ticket" :data {:error err})
       (error-map-response ring-request err))

     (catch Exception e
       (log/error (format-exception (:throwable &throw-context)))
       (provenance ticket-id "error-get-ticket"
                   :data {:error (format-exception (:throwable &throw-context))})
       (errors/error-response (unchecked &throw-context))))))

(defn download-file
  "Allows the caller to download a file associated with a ticket."
  [ticket-id filename ring-request]
  (log/debug "entered page kifshare.controllers/download-file")

  (try+
    (jinit/with-jargon (jargon-config) [cm :auto-close false]
      (let [ticket-info (tickets/ticket-info cm ticket-id)]
        (log/warn "Downloading " ticket-id " as " filename)
        (provenance ticket-id "download-file" :data {:ticket-info ticket-info})
        (tickets/download cm ticket-id)))

    (catch error? err
      (log/error (format-exception (:throwable &throw-context)))
      (provenance ticket-id "error-download-file" :data {:error err})
      (error-map-response ring-request err))

    (catch Exception e
      (log/error (format-exception (:throwable &throw-context)))
      (provenance ticket-id "error-download-file"
                  :data {:error (format-exception (:throwable &throw-context))})
      {:status 500 :body (cheshire/encode (unchecked &throw-context))})))

(defn download-ticket
  "Redirects the caller to the endpoint that allows them to download a ticket."
  [ticket-id ring-request]
  (log/debug "entered page kifshare.controllers/download-ticket")

  (try+
   (jinit/with-jargon (jargon-config) [cm]
     (let [ticket-info (tickets/ticket-info cm ticket-id)]
       (log/warn "Redirecting download for " ticket-id " to the /d/:ticket-id/:filename page.")
       (provenance ticket-id "download-by-ticket" :data {:ticket-info ticket-info})
       (redirect (str "../d/" ticket-id "/" (:filename ticket-info)))))

   (catch error? err
     (log/error (format-exception (:throwable &throw-context)))
     (provenance ticket-id "error-download-by-ticket" :data {:error err})
     (error-map-response ring-request err))

   (catch Exception e
     (log/error (format-exception (:throwable &throw-context)))
     (provenance ticket-id "error-download-by-ticket"
                 :data {:error (format-exception (:throwable &throw-context))})
     {:status 500 :body (cheshire/encode (unchecked &throw-context))})))
