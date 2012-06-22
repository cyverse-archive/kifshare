(ns kifshare.tickets
  (:require [clj-jargon.jargon :as jargon])
  (:use [slingshot.slingshot :only [try+ throw+]]
        [kifshare.errors]
        [noir.response :only [status]]
        [clojure-commons.error-codes]))

(defn check-ticket
  [ticket-id]
  (if (not (jargon/ticket? @jargon/username ticket-id))
    (throw+ {:error_code ERR_TICKET_NOT_FOUND 
             :ticket-id ticket-id})
    
    (let [ticket-obj (jargon/ticket-by-id @jargon/username ticket-id)] 
      (cond
        (jargon/ticket-expired? ticket-obj)
        (throw+ {:error_code ERR_TICKET_EXPIRED 
                 :ticket-id ticket-id
                 :expired-date (str (.. ticket-obj getExpireTime getTime))})
        
        (jargon/ticket-used-up? ticket-obj)
        (throw+ {:error_code ERR_TICKET_USED_UP 
                 :ticket-id ticket-id
                 :num-uses (str (.getUsesLimit ticket-obj))})))))

(defn download
  [ticket-id]
  (check-ticket ticket-id)
  (status 200 (jargon/ticket-input-stream @jargon/username ticket-id)))