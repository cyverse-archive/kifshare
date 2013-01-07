(ns kifshare.views.download
  (:require [kifshare.views.common :as common]
            [kifshare.provenance :as prov]
            [kifshare.tickets :as tickets]
            #_([clojure.tools.logging :as log])
            [clj-jargon.jargon :as jargon])
  (:use [noir.core :only [defpage]]
        [noir.response :only [status redirect]]
        [clojure-commons.error-codes]
        [slingshot.slingshot :only [try+]]
        [clojure.data.json :only [json-str]]
        [kifshare.config :only [jargon-config]]))


(defpage "/d/:ticket-id/:filename"
  {:keys [ticket-id filename]}
  (log/debug "entered page kifshare.views.download /d/:ticket-id/:filename")
  
  (try+
    (jargon/with-jargon (jargon-config) [cm]
      (let [ticket-info (tickets/ticket-info cm ticket-id)]
        #_(log/warn "Downloading " ticket-id " as " filename)
        (tickets/download cm ticket-id)))
    
    (catch error? err
      #_(log/error (format-exception (:throwable &throw-context)))
      (status 500 (json-str err)))
    
    (catch Exception e
      #_(log/error (format-exception (:throwable &throw-context)))
      (status 500 (json-str (unchecked &throw-context))))))

(defpage "/d/:ticket-id"
  {:keys [ticket-id]}
  (log/debug "entered page kifshare.views.download /d/:ticket-id")
  
  (jargon/with-jargon (jargon-config) [cm]
    (let [ticket-info (tickets/ticket-info cm ticket-id)]
      #_(log/warn "Redirecting download for " ticket-id " to the /d/:ticket-id/:filename page.")
      (redirect (str "/d/" ticket-id "/" (:filename ticket-info))))))
