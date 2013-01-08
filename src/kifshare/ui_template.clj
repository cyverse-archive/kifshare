(ns kifshare.ui-template
  (:use hiccup.core
        [kifshare.common :only [layout]])
  (:require [kifshare.config :as cfg]
            [clostache.parser :as prs]
            [clojure.tools.logging :as log])
  (:import [org.apache.commons.io FileUtils]))

(defn clear
  []
  (html [:div {:class "clear"}]))

(defn irods-avu-row
  [mmap]
  (html
   [:tr 
    [:td (:attr mmap)] 
    [:td (:value mmap)] 
    [:td (:unit mmap)]]))

(defn irods-avu-table
  [metadata]
  (if (pos? (count metadata))
    (html
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
         (map irods-avu-row metadata)]]]])))

(defn uses-limit
  [ticket-info]
  (html
   [:div {:id "wrapper_useslimit"}
    [:div {:id "useslimit-label"}
     "Uses Limit"]
    [:div {:id "useslimit"} 
     (:useslimit ticket-info)]]
   (clear)))

(defn remaining-uses
  [ticket-info]
  (html
   [:div {:id "wrapper_remaining"}
    [:div {:id "remaining-label"}
     "Remaining"]
    [:div {:id "remaining"}
     (:remaining ticket-info)]]
   (clear)))

(defn filename
  [ticket-info]
  (html
   [:div {:id "wrapper_filename"}
    [:h1 {:id "filename"} 
     (:filename ticket-info)]]))

(defn lastmod
  [ticket-info]
  (html
   [:div {:id "wrapper_lastmod"}
    [:div {:id "lastmod-label"} 
     "Last Modified"]
    [:div {:id "lastmod"} 
     (:lastmod ticket-info)]]
   (clear)))

(defn filesize
  [ticket-info]
  (html
   [:div {:id "wrapper_filesize"}
    [:div {:id "filesize-label"} 
     "File Size"]
    [:div {:id "filesize"} 
     (FileUtils/byteCountToDisplaySize 
      (Long/parseLong (:filesize ticket-info)))]]
   (clear)))

(defn download-button
  [ticket-id filename]
  (html
   [:div {:id "download_link_div"
          :class "grid_12"}
    [:div {:id "wrapper_download_link"
           :class "grid_4 push_4"}
     [:a {:href (str "d/" ticket-id "/" filename)
          :id "download_link"} 
      "Download!"]]]))

(defn template-map
  [ticket-info]
  (log/debug "kifshare.views.root/template-map")
  (merge ticket-info {:url (cfg/external-url)}))

(defn wget-str
  [ticket-info]
  (log/debug "entered kifshare.views.root/wget-str")
  (prs/render (cfg/wget-flags) (template-map ticket-info)))

(defn curl-str
  [ticket-info]
  (log/debug "entered kifshare.views.root/curl-str")
  (prs/render (cfg/curl-flags) (template-map ticket-info)))

(defn irods-str
  [ticket-info]
  (log/debug "entered kifshare.views.root/irods-str")
  (prs/render (cfg/iget-flags) (template-map ticket-info)))

(defn input-display
  [id value]
  (html
   [:input
    {:id id
     :type "text"
     :size 70
     :maxlength 500
     :readonly false
     :value value}]))

(defn irods-instr
  [ticket-info]
  (html
   [:div {:id "irods_instructions"}
    [:div {:id "header_irods_instr"} 
     "Using the i-commands"]
    
    [:div {:id "clippy-irods-instrs"}
     "sh> "
     (input-display "code_irods_instr" (irods-str ticket-info))
     [:span {:title "copy to clipboard"}
      [:div {:id "clippy-irods-wrapper"
             :class "clippy-irods"}
       (irods-str ticket-info)]]]]))

(defn downloader-instr
  [ticket-id ticket-info]
  (html
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
       (curl-str ticket-info)]]]]))

(defn alt-downloads
  [ticket-id ticket-info]
  (html
   [:div {:id "alternative_downloads_header"} 
    "Other ways to download this file..."]
   [:div {:id "alternative_downloads"}
    [:div {:id "alternative_downloads_inner"}
     (irods-instr ticket-info)
     (downloader-instr ticket-id ticket-info)]]))

(defn landing-page
  [ticket-id metadata ticket-info]
  (log/warn "in landing-page")
  (layout
   (html
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
    (clear))))
