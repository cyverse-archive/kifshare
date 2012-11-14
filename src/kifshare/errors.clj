(ns kifshare.errors
  (:use [noir.core :only [defpartial]]
        [noir.response :only [status]]
        [clojure.data.json]))

(def ERR_TICKET_EXPIRED "ERR_TICKET_EXPIRED")
(def ERR_TICKET_USED_UP "ERR_TICKET_USED_UP")
(def ERR_TICKET_NOT_FOUND "ERR_TICKET_NOT_FOUND")
(def ERR_TICKET_NOT_PUBLIC "ERR_TICKET_NOT_PUBLIC")

(defpartial ticket-expired [{:keys [ticket-id expired-date]}]
  [:div {:id "err-ticket-expired"}
   "Ticket " ticket-id " expired on " expired-date "."])

(defpartial ticket-used-up [{:keys [ticket-id num-uses]}]
  [:div {:id "err-ticket-used-up"}
   "Ticket " ticket-id " cannot be used anymore. The maximum number of uses is " num-uses "."])

(defpartial ticket-not-found [{:keys [ticket-id]}]
  [:div {:id "err-ticket-not-found"}
   "Ticket " ticket-id " does not exist."])

(defpartial default-error [{:as err-map}]
  [:div {:id "err-default"}
   [:pre
    [:code (with-out-str (pprint-json err-map))]]])

(defn error-response
  [err-map]
  (let [err-code (:error_code err-map)] 
    (cond
      (= err-code ERR_TICKET_NOT_FOUND)
      (status 500 (ticket-not-found err-map))
      
      (= err-code ERR_TICKET_EXPIRED)
      (status 500 (ticket-expired err-map))
      
      (= err-code ERR_TICKET_USED_UP)
      (status 500 (ticket-used-up err-map))
      
      :else
      (status 500 (default-error err-map)))))