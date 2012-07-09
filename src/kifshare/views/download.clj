(ns kifshare.views.download
  (:require [kifshare.views.common :as common]
            [kifshare.tickets :as tickets]
            [clj-jargon.jargon :as jargon])
  (:use [noir.core :only [defpage]]
        [noir.response :only [status]]
        [clojure-commons.error-codes]
        [slingshot.slingshot :only [try+]]
        [clojure.data.json :only [json-str]]))


(defpage "/d/:ticket-id"
  "Defines a direct download page that bypasses the
   Accept header check. In other words, this is always
   a direct download."
  {:keys [ticket-id]}
  (try+
    (jargon/with-jargon
      (tickets/download ticket-id))
    (catch error? err
      (status 500 (json-str err)))
    (catch Exception e
      (status 500 (json-str (unchecked &throw-context))))))

