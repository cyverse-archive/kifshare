(ns kifshare.views.root
  (:require [kifshare.views.common :as common]
            [kifshare.tickets :as tickets]
            [kifshare.config :as cfg]
            [kifshare.errors :as errors]
            [kifshare.provenance :as prov]
            [clj-jargon.jargon :as jargon]
            [clojure.tools.logging :as log]
            [clostache.parser :as prs])
  (:use [noir.core :only [defpage defpartial]]
        [noir.request :only [ring-request]]
        [noir.response :only [status redirect]]
        [clojure.data.json :only [json-str]]
        [slingshot.slingshot :only [try+]]
        [clojure-commons.error-codes]
        [kifshare.config :only [jargon-config]])
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

(defpartial irods-avu-table
  [metadata]
  (if (pos? (count metadata))
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
        (map irods-avu-row metadata)]]]]))

(defpartial uses-limit
  [ticket-info]
  [:div {:id "wrapper_useslimit"}
   [:div {:id "useslimit-label"}
    "Uses Limit"]
   [:div {:id "useslimit"} 
    (:useslimit ticket-info)]]
  (clear))

(defpartial remaining-uses
  [ticket-info]
  [:div {:id "wrapper_remaining"}
   [:div {:id "remaining-label"}
    "Remaining"]
   [:div {:id "remaining"}
    (:remaining ticket-info)]]
  (clear))

(defpartial filename
  [ticket-info]
  [:div {:id "wrapper_filename"}
   [:h1 {:id "filename"} 
    (:filename ticket-info)]])

(defpartial lastmod
  [ticket-info]
  [:div {:id "wrapper_lastmod"}
   [:div {:id "lastmod-label"} 
     "Last Modified"]
   [:div {:id "lastmod"} 
    (:lastmod ticket-info)]]
  (clear))

(defpartial filesize
  [ticket-info]
  [:div {:id "wrapper_filesize"}
   [:div {:id "filesize-label"} 
    "File Size"]
   [:div {:id "filesize"} 
    (FileUtils/byteCountToDisplaySize 
      (Long/parseLong (:filesize ticket-info)))]]
  (clear))

(defpartial download-button
  [ticket-id filename]
  [:div {:id "download_link_div"
         :class "grid_12"}
   [:div {:id "wrapper_download_link"
          :class "grid_4 push_4"}
    [:a {:href (str "d/" ticket-id "/" filename)
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

(defpartial input-display
  [id value]
  [:input {:id id
           :type "text"
           :size 70
           :maxlength 500
           :readonly false
           :value value}])

(defpartial irods-instr
  [ticket-info]
  [:div {:id "irods_instructions"}
   [:div {:id "header_irods_instr"} 
    "Using the i-commands"]
   
   [:div {:id "clippy-irods-instrs"}
    "sh> "
    (input-display "code_irods_instr" (irods-str ticket-info))
    [:span {:title "copy to clipboard"}
     [:div {:id "clippy-irods-wrapper"
            :class "clippy-irods"}
      (irods-str ticket-info)]]]])

(defpartial downloader-instr
  [ticket-id ticket-info]
  [:div {:id "downloader_instructions"}
   [:div {:id "header_downloader_instr"} 
    "Using wget or curl"]
   [:div {:id "clippy-wget-instrs"}
    "sh> "
    (input-display "wget_instr" (wget-str ticket-info))
    [:span  {:title "copy to clipboard"}
     [:div {:id "clippy-wget-wrapper"
            :class "clippy-wget"}
      (wget-str ticket-info)]]]
   
   [:div {:id "clippy-curl-instrs"}
    "sh> "
    (input-display "curl_instr" (curl-str ticket-info))
    [:span {:title "copy to clipboard"}
     [:div {:id "clippy-curl-wrapper"
            :class "clippy-curl"}
      (curl-str ticket-info)]]]])

(defpartial alt-downloads
  [ticket-id ticket-info]
  [:div {:id "alternative_downloads_header"} 
    "Other ways to download this file..."]
  [:div {:id "alternative_downloads"}
   [:div {:id "alternative_downloads_inner"}
    (irods-instr ticket-info)
    (downloader-instr ticket-id ticket-info)]])

(defpartial landing-page
  [ticket-id metadata ticket-info]
  (common/layout
    [:div {:id "file-info-wrapper"}
     [:div {:id "file-info-wrapper-inner"} 
      (filename ticket-info)
      (clear)
      (lastmod ticket-info)
      (clear)
      (filesize ticket-info)
      (clear)
      (uses-limit ticket-info)
      (clear)
      (remaining-uses ticket-info)]]
    (clear)
    (download-button ticket-id (:filename ticket-info))
    (clear)
    (irods-avu-table metadata)
    (clear)
    (alt-downloads ticket-id ticket-info)
    (clear)))

(defn object-metadata
  [cm abspath]
  (filterv
   #(not= (:unit %1) "ipc-system-avu")
   (jargon/get-metadata cm abspath)))

(defn show-landing-page
  "Handles error checking and decides whether to show the
   landing page or an error page."
  [cm ticket-id ticket-info]
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

(defpage "/:ticket-id"
  {:keys [ticket-id]}
  (jargon/with-jargon (jargon-config) [cm]
    (let [ticket-info (tickets/ticket-info cm ticket-id)] 
      (if (common/show-html? (ring-request))
        (show-landing-page cm ticket-id ticket-info)
        (redirect (str "d/" ticket-id "/" (:filename ticket-info)))))))

