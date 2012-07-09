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
  [:div#wrapper {:id "div-usage-analytics"}
   [:div {:id "header-usage-analytics"
          :class "grid_4"}
    "Usage Analytics"]
   
   [:div {:class "clear"}]
   
   [:div#wrapper {:id "div-uses-limit"}
    [:label {:id "label-useslimit" 
             :for "useslimit"
             :class "grid_2 alpha"}
     "Uses Limit"]
    [:div {:id "useslimit"
           :class "grid_2 omega"} 
     (:useslimit ticket-info)]]
   
   [:div {:class "clear"}]
   
   [:div#wrapper {:id "div-remaining-uses"}
    [:label {:id "label-remaining" 
             :for "remaining"
             :class "grid_2 alpha"}
     "Remaining"]
    [:div {:id "remaining"
           :class "grid_2 omega"}
     (:remaining ticket-info)]]
   [:div {:class "clear"}]])

(defpartial kif-filename
  [ticket-info]
  [:div#wrapper {:id "div-filename"}
   [:label {:id "label-filename" 
            :for "filename" 
            :class "grid_2 alpha"} 
    "Filename"]
   [:div {:id "filename" 
          :class "grid_2 omega"} 
    (:filename ticket-info)]
   [:div {:class "clear"}]])

(defpartial kif-lastmod
  [ticket-info]
  [:div#wrapper {:id "div-lastmod"}
   [:label {:id "label-lastmod" 
            :for "lastmod"
            :class "grid_2 alpha"} 
    "Last Modified"]
   [:div {:id "lastmod"
          :class "grid_2 omega"} 
    (:lastmod ticket-info)]
   [:div {:class "clear"}]])

(defpartial kif-filesize
  [ticket-info]
  [:div#wrapper {:id "div-filesize"}
   [:label {:id "label-filesize" 
            :for "filesize"
            :class "grid_2 alpha"} 
    "File Size"]
   [:div {:id "filesize"
          :class "grid_2 omega"} 
    (:filesize ticket-info)]
   [:div {:class "clear"}]])

(defpartial kif-download
  [ticket-id filename]
  [:div {:id "div-download-link"}
   [:a {:href (str "/d/" ticket-id)} filename]])

(defpartial kif-irods-instr
  [ticket-info]
  [:div {:id "div-irods-instructions"}
   "Using the i-commands"
   [:code
    (str "iget " (:abspath ticket-info))]])

(defpartial kif-downloader-instr
  [ticket-id ticket-info]
  [:div {:id "div-downloader-instructions"}
   "Using wget or curl"
   [:code
    (str "curl -o " (:filename ticket-info) " http://thisurl.com/" ticket-id)]])

(defpartial landing-page
  [ticket-id metadata ticket-info]
  (common/layout
    [:div {:id "div-file-info" :class "grid_4"}
     (kif-filename ticket-info)
     (kif-lastmod ticket-info)
     (kif-filesize ticket-info)]
    
    [:div {:id "div-usage" :class "grid_4 push_4"}
     (kif-usage-analytics ticket-info)] 
    
    [:div {:class "clear"}]
    
    (kif-download ticket-id (:filename ticket-info))
    
    (kif-irods-avu metadata)
    (kif-irods-instr ticket-info)
    (kif-downloader-instr ticket-id ticket-info)))

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

