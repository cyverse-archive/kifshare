(ns kifshare.views.root
  (:require [kifshare.views.common :as common]
            [kifshare.tickets :as tickets]
            [kifshare.errors :as errors]
            [clj-jargon.jargon :as jargon]
            [clojure.tools.logging :as log])
  (:use [noir.core :only [defpage defpartial]]
        [noir.request :only [ring-request]]
        [noir.response :only [status redirect]]
        [clojure.data.json :only [json-str]]
        [slingshot.slingshot :only [try+]]
        [clojure-commons.error-codes]))

(defpartial irods-avu-row
  [mmap]
  (let [attr (:attr mmap)
        val  (:value mmap)
        unit (:unit mmap)]
    [:tr [:td attr] [:td val] [:td unit]]))

(defpartial kif-irods-avu
  [metadata]
  [:table {:id "irods-avus"}
   [:tr [:th "Attribute"] [:th "Value"] [:th "Unit"]]
   (map irods-avu-row metadata)])

(defpartial kif-usage-analytics
  [ticket-info]
  [:div {:id "usage-analytics"} 
   "Usage Analytics"
   [:div {:id "uses-limit"}
    (str "Uses Limit: " (:useslimit ticket-info))]
   [:div {:id "remaining-uses"} 
    (str "Remaining: " (:remaining ticket-info))]])

(defpartial kif-filename
  [ticket-info]
  [:div {:id "filename"}
   (:filename ticket-info)])

(defpartial kif-lastmod
  [ticket-info]
  [:div {:id "lastmod"}
   (:lastmod ticket-info)])

(defpartial kif-filesize
  [ticket-info]
  [:div {:id "filezize"}
   (:filesize ticket-info)])

(defpartial kif-download
  [ticket-id filename]
  [:div {:id "download-link"}
   [:a {:href (str "/d/" ticket-id)} filename]])

(defpartial landing-page
  [ticket-id metadata ticket-info]
  (common/layout
    (kif-filename ticket-info)
    (kif-lastmod ticket-info)
    (kif-filesize ticket-info)
    (kif-download ticket-id (:filename ticket-info))
    (kif-usage-analytics ticket-info)
    (kif-irods-avu metadata)))

(defn show-landing-page
  "Handles error checking and decides whether to show the
   landing page or an error page."
  [ticket-id]
  (try+
    (tickets/check-ticket ticket-id)
    (landing-page 
      ticket-id 
      (jargon/get-metadata (tickets/ticket-abs-path ticket-id))
      (tickets/ticket-info ticket-id))
    (catch error? err
      (log/warn err)
      (errors/error-response err))
    (catch Exception e
      (log/warn e)
      (errors/error-response (unchecked &throw-context)))))

(defn bare-download
  "Handles a bare ticket, no HTML involved. 
   This is a direct download of a file associated with a ticket.."
  [ticket-id]
  (try+
    (tickets/download ticket-id)
    (catch error? err
      (log/warn (json-str err))
      (status 500 (json-str err)))
    (catch Exception e
      (log/warn e)
      (status 500 (json-str (unchecked &throw-context))))))

(defpage "/:ticket-id"
  {:keys [ticket-id]}
  (jargon/with-jargon
    (if (common/show-html? (ring-request))
      (show-landing-page ticket-id)
      (bare-download ticket-id))))

