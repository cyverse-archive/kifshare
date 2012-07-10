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
  [:div {:id "irods_avus_container"
         :class "grid_8 kif_section"}
   [:table {:id "irods_avus"}
    [:thead
     [:tr 
      [:th "Attribute"] 
      [:th "Value"] 
      [:th "Unit"]]]
    [:tbody
     (map irods-avu-row metadata)]]])

(defpartial kif-usage-analytics
  [ticket-info]
  [:div#wrapper {:id "div-usage-analytics"}
   [:div {:id "header-usage-analytics"
          :class "grid_4"}
    [:label {:id "label-usage-analytics"
             :class "grid_4"}     
     "Usage Analytics"]]
   
   (clear)
   
   [:div#wrapper {:id "div-uses-limit"}
    [:label {:id "label-useslimit" 
             :for "useslimit"
             :class "grid_2 alpha"}
     "Uses Limit"]
    [:div {:id "useslimit"
           :class "grid_2 omega"} 
     (:useslimit ticket-info)]]
   
   (clear)
   
   [:div#wrapper {:id "div-remaining-uses"}
    [:label {:id "label-remaining" 
             :for "remaining"
             :class "grid_2 alpha"}
     "Remaining"]
    [:div {:id "remaining"
           :class "grid_2 omega"}
     (:remaining ticket-info)]]
   (clear)])

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
   (clear)])

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
   (clear)])

(defpartial kif-filesize
  [ticket-info]
  [:div {:id "div-filesize"}
   [:label {:id "label-filesize" 
            :for "filesize"
            :class "grid_2 alpha"} 
    "File Size"]
   [:div {:id "filesize"
          :class "grid_2 omega"} 
    (:filesize ticket-info)]
   (clear)])

(defpartial kif-download
  [ticket-id filename]
  [:div {:id "div-download-link"
         :class "grid_4"}
   [:a {:href (str "/d/" ticket-id)} filename]])

(defpartial kif-irods-instr
  [ticket-info]
  [:div {:id "div-irods-instructions"
         :class "grid_12 kif_section"}
   [:h3 {:id "header-irods-instr"
         :class "grid_4"} "Using the i-commands"]
   (clear)
   [:code {:id "code-irods-instr" :class "grid_8 push_2"}
    (str "iget " (:abspath ticket-info))]]
  (clear))

(defpartial kif-downloader-instr
  [ticket-id ticket-info]
  [:div {:id "div-downloader-instructions"
         :class "grid_12 kif_section"}
   [:h3 {:id "header-downloader-instr"
         :class "grid_4"} "Using wget or curl"]
   (clear)
   [:code {:id "code-downloader-instr" 
           :class "grid_8 push_2"}
    (str "curl -o " (:filename ticket-info) " http://thisurl.com/" ticket-id)]]
  (clear))

(defpartial landing-page
  [ticket-id metadata ticket-info]
  (common/layout
    [:div {:id "div-file-info" :class "kif_section grid_4"}
     (kif-filename ticket-info)
     (kif-lastmod ticket-info)
     (kif-filesize ticket-info)]
    
    [:div {:id "div-usage" :class "grid_4 push_4 kif_section"}
     (kif-usage-analytics ticket-info)] 
    
    (clear)
    
    (kif-irods-avu metadata)
    
    (kif-download ticket-id (:filename ticket-info))
    
    (clear)
    
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

