(ns kifshare.views.root
  (:require [kifshare.views.common :as common]
            [kifshare.tickets :as tickets]
            [kifshare.config :as cfg]
            [kifshare.errors :as errors]
            [clj-jargon.jargon :as jargon]
            [clojure.tools.logging :as log]
            [clostache.parser :as prs])
  (:use [noir.core :only [defpage defpartial]]
        [noir.request :only [ring-request]]
        [noir.response :only [status redirect]]
        [clojure.data.json :only [json-str]]
        [slingshot.slingshot :only [try+]]
        [clojure-commons.error-codes])
  (:import [org.apache.commons.io FileUtils]))

(defpartial clear
  []
  [:div {:class "clear"}])

(defpartial irods-avu-row
  [mmap]
  [:tr 
   [:td (:attr mmap)] 
   [:td (:value mmap)] 
   [:td (:unit mmap)]])

(defpartial kif-irods-avu
  [metadata]
  [:div {:id "wrapper_irods_avus"}
   [:div {:id "wrapper_irods_avus_inner"} 
    [:div {:id "irods_avus_header"}
     [:h3 "Metadata"]]
    [:table {:id "irods_avus"}
     [:thead
      [:tr 
       [:th "Attribute"] 
       [:th "Value"] 
       [:th "Unit"]]]
     [:tbody
      (map irods-avu-row metadata)]]]])

(defpartial kif-uses-limit
  [ticket-info]
  [:div {:id "wrapper_useslimit"}
   [:label {:id "label_useslimit" 
            :for "useslimit"
            :class "grid_2 alpha"}
    "Uses Limit"]
   [:div {:id "useslimit"
          :class "grid_6 omega"} 
    (:useslimit ticket-info)]])

(defpartial kif-remaining-uses
  [ticket-info]
  [:div {:id "wrapper_remaining"}
   [:label {:id "label_remaining" 
            :for "remaining"
            :class "grid_2 alpha"}
    "Remaining"]
   [:div {:id "remaining"
          :class "grid_6 omega"}
    (:remaining ticket-info)]])

(defpartial kif-filename
  [ticket-info]
  [:div {:id "wrapper_filename"
         :class "grid_6 push_3"}
   [:h1 {:id "filename"} 
    (:filename ticket-info)]])

(defpartial kif-lastmod
  [ticket-info]
  [:div {:id "wrapper_lastmod"}
   [:label {:id "label_lastmod" 
            :for "lastmod"
            :class "grid_2 alpha"} 
    "Last Modified"]
   [:div {:id "lastmod"
          :class "grid_6 omega"} 
    (:lastmod ticket-info)]])

(defpartial kif-filesize
  [ticket-info]
  [:div {:id "wrapper_filesize"}
   [:label {:id "label_filesize" 
            :for "filesize"
            :class "grid_2 alpha"} 
    "File Size"]
   [:div {:id "filesize"
          :class "grid_6 omega"} 
    (FileUtils/byteCountToDisplaySize 
      (Long/parseLong (:filesize ticket-info)))]])

(defpartial kif-download
  [ticket-id filename]
  [:div {:id "download_link_div"
         :class "grid_12"}
   [:div {:id "wrapper_download_link"
          :class "grid_4 push_4"}
    [:a {:href (str "/d/" ticket-id)
        :id "download_link"} 
    "Download!"]]])

(defn template-map
  [ticket-info]
  (merge
    ticket-info
    {:url (cfg/external-url)}))

(defn wget-str
  [ticket-info]
  (prs/render 
    (cfg/wget-flags) 
    (template-map ticket-info)))

(defn curl-str
  [ticket-info]
  (prs/render 
    (cfg/curl-flags) 
    (template-map ticket-info)))

(defn irods-str
  [ticket-info]
  (prs/render 
    (cfg/iget-flags) 
    (template-map ticket-info)))

(defpartial kif-irods-instr
  [ticket-info]
  [:div {:id "irods_instructions"}
   [:div {:id "header_irods_instr"} 
    "Using the i-commands"]
   
   [:div {:id "clippy-irods-wrapper"} 
    [:div {:class "clippy-irods"}
     (irods-str ticket-info)]
    [:code {:id "code_irods_instr"}
     (irods-str ticket-info)]]])

(defpartial kif-downloader-instr
  [ticket-id ticket-info]
  [:div {:id "downloader_instructions"}
   [:div {:id "header_downloader_instr"} 
    "Using wget or curl"]
   
   [:div {:id "clippy-wget-wrapper"} 
    [:div {:class "clippy-wget"}
     (wget-str ticket-info)]
    [:code {:id "wget_instr"}
     (wget-str ticket-info)]]
   
   #_(clear)
   
   [:div {:id "clippy-curl-wrapper"} 
    [:div {:class "clippy-curl"} 
     (curl-str ticket-info)]
    [:code {:id "code_downloader_instr"}
     (curl-str ticket-info)]]])

(defpartial kif-file-info
  [ticket-info]
  [:div {:id "file_info" :class "grid_8 push_2"}
   (kif-lastmod ticket-info)
   (clear)
   (kif-filesize ticket-info)
   (clear)])

(defpartial kif-usage-analytics
  [ticket-info]
  [:div {:id "usage_analytics" :class "grid_8 push_2"}
   (kif-uses-limit ticket-info)
   (clear)
   (kif-remaining-uses ticket-info)])

(defpartial kif-alt-downloads
  [ticket-id ticket-info]
  [:div {:id "alternative_downloads"}
   [:div {:id "alternative_downloads_inner"}
    [:h3 {:id "alternative_downloads_header"} 
    "Other ways to download this file..."]
    (kif-irods-instr ticket-info)
    (kif-downloader-instr ticket-id ticket-info)]])

(defpartial landing-page
  [ticket-id metadata ticket-info]
  (common/layout
    (kif-filename ticket-info)
    (clear)
    (kif-file-info ticket-info)
    (kif-usage-analytics ticket-info)
    (clear)
    (kif-download ticket-id (:filename ticket-info))
    (clear)
    (kif-irods-avu metadata)
    (clear)
    (kif-alt-downloads ticket-id ticket-info)
    (clear)))

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

