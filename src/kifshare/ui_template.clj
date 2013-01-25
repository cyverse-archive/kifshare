(ns kifshare.ui-template
  (:use hiccup.core
        [hiccup.page :only [include-css include-js html5]]
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
     [:div {:id "irods-avus"} 
      [:div {:id "irods-avus-header"}
       [:h2 "Metadata"]]
      [:table {:id "irods-avus-data"}
       [:thead
        [:tr 
         [:th "Attribute"] 
         [:th "Value"] 
         [:th "Unit"]]]
       [:tbody
        (map irods-avu-row metadata)]]])))

(defn lastmod
  [ticket-info]
  (log/debug "entered kifshare.ui-template/lastmod")
  
  (html
   [:div {:id "lastmod-detail"}
    [:div {:id "lastmod-label"} 
     "Last Modified"]
    [:div {:id "lastmod"} 
     (:lastmod ticket-info)]]))

(defn filesize
  [ticket-info]
  (log/debug "entered kifshare.ui-template/filesize")
  
  (html
   [:div {:id "size-detail"}
    [:div {:id "size-label"} 
     "File Size"]
    [:div {:id "size"} 
     (FileUtils/byteCountToDisplaySize 
      (Long/parseLong (:filesize ticket-info)))]]))

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
   [:span {:id "ticket-info" :style "display: none;"}
    [:div {:id "ticket-info-map"}
     (json/generate-string
      (ui-ticket-info ticket-info))]]))

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
   [:div {:id "irods-instructions"}
    [:div {:id "irods-instructions-header"} 
     [:h2 "Using the i-commands"]]
    
    [:div {:id "clippy-irods-instructions"}
     (input-display "irods-command-line")
     [:span {:title "copy to clipboard"}
      [:div {:id "clippy-irods-wrapper"
             :class "clippy-irods"}]]]]))

(defn downloader-instr
  [ticket-id ticket-info]
  (log/debug "entered kifshare.ui-template/downloader-instr")
  
  (html
   [:div {:id "downloader-instructions"}
    [:div {:id "downloader-instructions-header"} 
     "Using wget or curl"]
    [:div {:id "clippy-wget-instructions"}
     (input-display "wget-command-line")
     [:span  {:title "copy to clipboard"}
      [:div {:id "clippy-wget-wrapper"
             :class "clippy-wget"}]]]
    
    [:div {:id "clippy-curl-instructions"}
     (input-display "curl-command-line")
     [:span {:title "copy to clipboard"}
      [:div {:id "clippy-curl-wrapper"
             :class "clippy-curl"}]]]]))

(defn menu
  [ticket-info]
  (html
   [:div {:id "menu"}
    [:ul
     [:li [:div {:id "logo-container"}
           [:img {:id "logo" :src "../img/powered_by_iplant_logo.png"}]]]
     [:li [:div [:h1 {:id "filename"} (:filename ticket-info)]]]
     [:li [:div {:id "download-container"}
           [:a {:href (str "d/" (:ticket-id ticket-info) "/" (:filename ticket-info))
                :id "download-link"}
            [:div {:id "download-link-area"}
             "Download!"]]]]]]))

(defn details
  [ticket-info]
  [:div {:id "details"}
   [:a {:name "details-section"}]
   [:div {:id "details-header"}
    [:h2 "File And Ticket Details"]]
   (lastmod ticket-info)
   (filesize ticket-info)])

(defn alt-downloads
  [ticket-info]
  (log/debug "entered kifshare.ui-template/alt-downloads")
  
  (html
   [:div {:id "alt-downloads-header"} 
    [:h2 "Downloading From The Command-Line"]]
   [:div {:id "alt-downloads"}
    (irods-instr ticket-info)
    (downloader-instr (:ticket-id ticket-info) ticket-info)]))

(defn landing-page
  [ticket-id metadata ticket-info]
  (log/debug "entered kifshare.ui-template/landing-page")
  
  (html
   [:head
    [:title (:filename ticket-info)]
    (map include-css (cfg/css-files))
    (map include-js (cfg/javascript-files))]
   [:body
    (template-map ticket-info)
    (menu ticket-info)
    [:div#wrapper {:id "page-wrapper" :class "container_12"}
     (details ticket-info)
     (irods-avu-table metadata)
     (alt-downloads ticket-info)]]))
