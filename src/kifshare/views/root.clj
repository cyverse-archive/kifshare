(ns kifshare.views.root
  (:require [kifshare.views.common :as common]
            [kifshare.tickets :as tickets]
            [kifshare.errors :as errors]
            [clj-jargon.jargon :as jargon])
  (:use [noir.core :only [defpage defpartial]]
        [noir.request :only [ring-request]]
        [noir.response :only [status redirect]]
        [clojure.data.json :only [json-str]]
        [slingshot.slingshot :only [try+]]
        [clojure-commons.error-codes]))

(defpartial landing-page
  "Generates the page that provides the download link
   to the file and any metadata associated with the file."
  [ticket-id]
  [:div {:id "download-link"}
   [:a {:href (str "/d/" ticket-id)} ticket-id]])

(defn show-landing-page
  "Handles error checking and decides whether to show the
   landing page or an error page."
  [ticket-id]
  (try+
    (tickets/check-ticket ticket-id)
    (landing-page ticket-id)
    (catch error? err
      (errors/error-response err))
    (catch Exception e
      (errors/error-response (unchecked &throw-context)))))

(defn bare-download
  "Handles a bare ticket, no HTML involved. 
   This is a direct download of a file associated with a ticket.."
  [ticket-id]
  (try+
    (tickets/download ticket-id)
    (catch error? err
      (status 500 (json-str err)))
    (catch Exception e
      (status 500 (json-str (unchecked &throw-context))))))

(defpage "/:ticket-id"
  "Defines the ticket download page. Decides whether to
   show the HTML landing page or do a direct download based
   on the Accept headers of the incoming request."
  {:keys [ticket-id]}
  (jargon/with-jargon
    (if (common/show-html? (ring-request))
      (show-landing-page ticket-id)
      (bare-download ticket-id))))

