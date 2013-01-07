(ns kifshare.controllers
  (:use [ring.util.response :only [redirect status]]
        [kifshare.config :only [jargon-config]]
        [kifshare.ui-template :only [landing-page]]
        [clojure.data.json :only [json-str]]
        [slingshot.slingshot :only [try+]]
        [clojure-commons.error-codes])
  (:require [kifshare.tickets :as tickets]
            [kifshare.common :as common]
            [clojure.tools.logging :as log]
            [clj-jargon.jargon :as jargon]
            [kifshare.errors :as errors]))

(defn object-metadata
  [cm abspath]
  (log/debug "kifshare.views.root/object-metadata")
  
  (filterv
   #(not= (:unit %1) "ipc-system-avu")
   (jargon/get-metadata cm abspath)))

(defn show-landing-page
  "Handles error checking and decides whether to show the
   landing page or an error page."
  [cm ticket-id ticket-info]
  (log/debug "entered kifshare.views.root/show-landing-page")
  
  (try+
   (tickets/check-ticket cm ticket-id)
   (landing-page
    ticket-id
    (object-metadata cm (tickets/ticket-abs-path cm ticket-id))
    ticket-info)

   (catch error? err
     (log/error (format-exception (:throwable &throw-context)))
     (errors/error-response err))

   (catch Exception e
     (log/error (format-exception (:throwable &throw-context)))
     (errors/error-response (unchecked &throw-context)))))

(defn get-ticket
  "Determines whether to redirect to a download or show the landing page."
  [ticket-id ring-request]
  (log/debug "entered page kifshare.views.root /:ticket-id")
  
  (jargon/with-jargon (jargon-config) [cm]
    (let [ticket-info (tickets/ticket-info cm ticket-id)]
      (log/debug "Ticket Info:\n" ticket-info)
      
      (if (common/show-html? ring-request)
        (show-landing-page cm ticket-id ticket-info)
        (redirect (str "d/" ticket-id "/" (:filename ticket-info)))))))

(defn download-file
  "Allows the caller to download a file associated with a ticket."
  [ticket-id filename]
  (log/debug "entered page kifshare.views.download /d/:ticket-id/:filename")
  
  (try+
    (jargon/with-jargon (jargon-config) [cm]
      (let [ticket-info (tickets/ticket-info cm ticket-id)]
        (log/warn "Downloading " ticket-id " as " filename)
        (tickets/download cm ticket-id)))
    
    (catch error? err
      (log/error (format-exception (:throwable &throw-context)))
      (status 500 (json-str err)))
    
    (catch Exception e
      (log/error (format-exception (:throwable &throw-context)))
      (status 500 (json-str (unchecked &throw-context))))))

(defn download-ticket
  "Redirects the caller to the endpoint that allows them to download a ticket."
  [ticket-id]
  (log/debug "entered page kifshare.views.download /d/:ticket-id")
  
  (jargon/with-jargon (jargon-config) [cm]
    (let [ticket-info (tickets/ticket-info cm ticket-id)]
      (log/warn "Redirecting download for " ticket-id " to the /d/:ticket-id/:filename page.")
      (redirect (str "/d/" ticket-id "/" (:filename ticket-info))))))