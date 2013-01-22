(ns kifshare.ui-template
  (:use hiccup.core
        [kifshare.common :only [layout]])
  (:require [kifshare.config :as cfg]
            [clostache.parser :as prs]
            [clojure.tools.logging :as log]
            [cheshire.core :as json])
  (:import [org.apache.commons.io FileUtils]))

(defn clear
  []
  (log/debug "entered kifshare.ui-template/clear")
  (html [:div {:class "clear"}]))

(defn irods-avu-row
  [mmap]
  (log/debug "entered kifshare.ui-template/irods-avu-row")
  
  (html
   [:tr 
    [:td (:attr mmap)] 
    [:td (:value mmap)] 
    [:td (:unit mmap)]]))

(defn irods-avu-table
  [metadata]
  (log/debug "entered kifshare.ui-template/irods-avu-table")
  
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
  (log/debug "entered kifshare.ui-template/uses-limit")
  
  (html
   [:div {:id "wrapper_useslimit"}
    [:div {:id "useslimit-label"}
     "Uses Limit"]
    [:div {:id "useslimit"} 
     (:useslimit ticket-info)]]
   (clear)))

(defn remaining-uses
  [ticket-info]
  (log/debug "entered kifshare.ui-template/remaining-uses")
  
  (html
   [:div {:id "wrapper_remaining"}
    [:div {:id "remaining-label"}
     "Remaining"]
    [:div {:id "remaining"}
     (:remaining ticket-info)]]
   (clear)))

(defn filename
  [ticket-info]
  (log/debug "entered kifshare.ui-template/filename")
  
  (html
   [:div {:id "wrapper_filename"}
    [:h1 {:id "filename"} 
     (:filename ticket-info)]]))

(defn lastmod
  [ticket-info]
  (log/debug "entered kifshare.ui-template/lastmod")
  
  (html
   [:div {:id "wrapper_lastmod"}
    [:div {:id "lastmod-label"} 
     "Last Modified"]
    [:div {:id "lastmod"} 
     (:lastmod ticket-info)]]
   (clear)))

(defn filesize
  [ticket-info]
  (log/debug "entered kifshare.ui-template/filesize")
  
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
  (log/debug "entered kifshare.ui-template/download-button")
  
  (html
   [:div {:id "download_link_div"
          :class "grid_12"}
    [:div {:id "wrapper_download_link"
           :class "grid_4 push_4"}
     [:a {:href (str "d/" ticket-id "/" filename)
          :id "download_link"} 
      "Download!"]]]))

(defn ui-ticket-info
  [ticket-info]
  (assoc ticket-info
    :wget_template (cfg/wget-flags)
    :curl_template (cfg/curl-flags)
    :iget_template (cfg/iget-flags)))

(defn template-map
  [ticket-info]
  (log/debug "entered kifshare.ui-template/template-map")
  (html
   [:span {:id "ticket_info" :style "display: none;"}
    [:div {:id "ticket_info_map"}
     (json/generate-string
      (ui-ticket-info ticket-info))]]))

#_(defn wget-str
  [ticket-info]
  (log/debug "entered kifshare.ui-template/wget-str")
  (prs/render (cfg/wget-flags) (template-map ticket-info)))

#_(defn curl-str
  [ticket-info]
  (log/debug "entered kifshare.ui-template/curl-str")
  (prs/render (cfg/curl-flags) (template-map ticket-info)))

#_(defn irods-str
  [ticket-info]
  (log/debug "entered kifshare.ui-template/irods-str")
  (prs/render (cfg/iget-flags) (template-map ticket-info)))

(defn input-display
  [id]
  (log/debug "entered kifshare.ui-template/input-display")
  
  (html
   [:input
    {:id id
     :type "text"
     :size 70
     :maxlength 500
     :readonly false
     :value ""}]))

(defn irods-instr
  [ticket-info]
  (log/debug "entered kifshare.ui-template/irods-instr")
  
  (html
   [:div {:id "irods_instructions"}
    [:div {:id "header_irods_instr"} 
     "Using the i-commands"]
    
    [:div {:id "clippy-irods-instrs"}
     "sh> "
     (input-display "code_irods_instr")
     [:span {:title "copy to clipboard"}
      [:div {:id "clippy-irods-wrapper"
             :class "clippy-irods"}]]]]))

(defn downloader-instr
  [ticket-id ticket-info]
  (log/debug "entered kifshare.ui-template/downloader-instr")
  
  (html
   [:div {:id "downloader_instructions"}
    [:div {:id "header_downloader_instr"} 
     "Using wget or curl"]
    [:div {:id "clippy-wget-instrs"}
     "sh> "
     (input-display "wget_instr")
     [:span  {:title "copy to clipboard"}
      [:div {:id "clippy-wget-wrapper"
             :class "clippy-wget"}]]]
    
    [:div {:id "clippy-curl-instrs"}
     "sh> "
     (input-display "curl_instr")
     [:span {:title "copy to clipboard"}
      [:div {:id "clippy-curl-wrapper"
             :class "clippy-curl"}]]]]))

(defn alt-downloads
  [ticket-id ticket-info]
  (log/debug "entered kifshare.ui-template/alt-downloads")
  
  (html
   [:div {:id "alternative_downloads_header"} 
    "Other ways to download this file..."]
   [:div {:id "alternative_downloads"}
    [:div {:id "alternative_downloads_inner"}
     (irods-instr ticket-info)
     (downloader-instr ticket-id ticket-info)]]))

(defn landing-page
  [ticket-id metadata ticket-info]
  (log/debug "entered kifshare.ui-template/landing-page")
  
  (layout
   (html
    (template-map ticket-info)
    [:div {:id "file-info-wrapper"}
     [:div {:id "file-info-wrapper-inner"}
      (filename ticket-info)
      (clear)
      (lastmod ticket-info)
      (clear)
      (filesize ticket-info)
      #_(clear)
      #_(uses-limit ticket-info)
      #_(clear)
      #_(remaining-uses ticket-info)]]
    (clear)
    (download-button ticket-id (:filename ticket-info))
    (clear)
    (irods-avu-table metadata)
    (clear)
    (alt-downloads ticket-id ticket-info)
    (clear))))
