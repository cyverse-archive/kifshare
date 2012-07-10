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
        [clojure-commons.error-codes])
  (:import [org.apache.commons.io FileUtils]))

(defpartial clear
  []
  [:div {:class "clear"}])

(defpartial irods-avu-row
  [mmap]
  (let [attr (:attr mmap)
        val  (:value mmap)
        unit (:unit mmap)]
    [:tr 
     [:td attr] 
     [:td val] 
     [:td unit]]))

(defpartial kif-irods-avu
  [metadata]
  [:div {:id "irods_avus_container"}
   [:h3 {:id "irods_avus_header kif_header"} 
    "Metadata"]
   [:table {:id "irods_avus"}
    [:thead
     [:tr 
      [:th "Attribute"] 
      [:th "Value"] 
      [:th "Unit"]]]
    [:tbody
     (map irods-avu-row metadata)]]])

(defpartial kif-uses-limit
  [ticket-info]
  [:div#wrapper {:id "wrapper_useslimit"}
    [:label {:id "label_useslimit" 
             :for "useslimit"
             :class "grid_2 alpha"}
     "Uses Limit"]
    [:div {:id "useslimit"
           :class "grid_6 omega"} 
     (:useslimit ticket-info)]])

(defpartial kif-remaining-uses
  [ticket-info]
  [:div#wrapper {:id "wrapper_remaining"}
   [:label {:id "label_remaining" 
            :for "remaining"
            :class "grid_2 alpha"}
    "Remaining"]
   [:div {:id "remaining"
          :class "grid_6 omega"}
    (:remaining ticket-info)]])

(defpartial kif-filename
  [ticket-info]
  [:div#wrapper {:id "wrapper_filename"
                 :class "grid_6 push_3"}
   #_([:label {:id "label_filename" 
            :for "filename"
            :class "grid_2 alpha"} 
    "Filename"])
   [:h1 {:id "filename"} 
    (:filename ticket-info)]
   (clear)])

(defpartial kif-lastmod
  [ticket-info]
  [:div#wrapper {:id "wrapper_lastmod"}
   [:label {:id "label_lastmod" 
            :for "lastmod"
            :class "grid_2 alpha"} 
    "Last Modified"]
   [:div {:id "lastmod"
          :class "grid_6 omega"} 
    (:lastmod ticket-info)]
   (clear)])

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
      (Long/parseLong (:filesize ticket-info)))]
   (clear)])

(defpartial kif-download
  [ticket-id filename]
  [:div {:id "download_link_div"
         :class "grid_12"}
   [:div {:id "wrapper_download_link"
          :class "grid_4 push_4"}
    [:a {:href (str "/d/" ticket-id)
        :id "download_link"} 
    "Download!"]]])

(defpartial kif-irods-instr
  [ticket-info]
  [:div {:id "irods_instructions"
         :class "grid_8"}
   [:div {:id "header_irods_instr"
         :class "grid_4"} 
    "Using the i-commands"]
   (clear)
   [:code {:id "code_irods_instr" 
           :class "grid_6 push_1"}
    (str "iget " (:abspath ticket-info))]]
  (clear))

(defpartial kif-downloader-instr
  [ticket-id ticket-info]
  [:div {:id "downloader_instructions"
         :class "grid_8"}
   [:div {:id "header_downloader_instr"
         :class "grid_4"} 
    "Using wget or curl"]
   (clear)
   [:code {:id "code_downloader_instr" 
           :class "grid_6 push_1"}
    (str "curl -o " (:filename ticket-info) " http://thisurl.com/" ticket-id)]]
  (clear))

(defpartial landing-page
  [ticket-id metadata ticket-info]
  (common/layout
    (kif-filename ticket-info)
    
    [:div {:id "file_info" :class "grid_8 push_2"}
     (kif-lastmod ticket-info)
     (kif-filesize ticket-info)]
    
    [:div {:id "usage_analytics" :class "grid_8 push_2"}
     (kif-uses-limit ticket-info)
     (clear)
     (kif-remaining-uses ticket-info)]
    
    (clear)
    
    (kif-download ticket-id (:filename ticket-info))
    
    (clear)
    (kif-irods-avu metadata)
    (clear)
    [:div {:id "alternative_downloads"}
     [:h3 {:id "alternative_downloads_header"} 
      "Other ways to download this file..."]
     (kif-irods-instr ticket-info)
    (clear)
    (kif-downloader-instr ticket-id ticket-info)]
     
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

