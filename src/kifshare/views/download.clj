(ns kifshare.views.download
  (:require [kifshare.views.common :as common]
            [kifshare.provenance :as prov]
            [kifshare.tickets :as tickets]
            [clj-jargon.jargon :as jargon])
  (:use [noir.core :only [defpage]]
        [noir.response :only [status]]
        [clojure-commons.error-codes]
        [slingshot.slingshot :only [try+]]
        [clojure.data.json :only [json-str]]))


(defpage "/d/:ticket-id"
  {:keys [ticket-id]}
  (try+
    (let [ticket-info (tickets/ticket-info ticket-id)
          prov-uuid   (prov/prov-uuid ticket-info)]
      (prov/DOWNLOAD prov-uuid)
      (jargon/with-jargon
        (tickets/download ticket-id)))
    (catch error? err
      (status 500 (json-str err)))
    (catch Exception e
      (status 500 (json-str (unchecked &throw-context))))))

