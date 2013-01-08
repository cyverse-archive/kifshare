(ns kifshare.errors
  (:use hiccup.core
        [ring.util.response :only [status]]
        [clojure.data.json]))

(def ERR_TICKET_EXPIRED "ERR_TICKET_EXPIRED")
(def ERR_TICKET_USED_UP "ERR_TICKET_USED_UP")
(def ERR_TICKET_NOT_FOUND "ERR_TICKET_NOT_FOUND")
(def ERR_TICKET_NOT_PUBLIC "ERR_TICKET_NOT_PUBLIC")

(defn ticket-expired [{:keys [ticket-id expired-date]}]
  (html [:div {:id "err-ticket-expired"}
   "Ticket " ticket-id " expired on " expired-date "."]))

(defn ticket-used-up [{:keys [ticket-id num-uses]}]
  (html [:div {:id "err-ticket-used-up"}
   "Ticket " ticket-id " cannot be used anymore. The maximum number of uses is " num-uses "."]))

(defn ticket-not-found [{:keys [ticket-id]}]
  (html [:div {:id "err-ticket-not-found"}
    "Ticket " ticket-id " does not exist."]))

(defn default-error [{:as err-map}]
  (html
   [:div {:id "err-default"}
    [:pre
     [:code (with-out-str (pprint-json err-map))]]]))

(defn error-response
  [err-map]
  (let [err-code (:error_code err-map)] 
    (cond
     (= err-code ERR_TICKET_NOT_FOUND)
     {:status 500 :body (ticket-not-found err-map)}
     
     (= err-code ERR_TICKET_EXPIRED)
     {:status 500 :body (ticket-expired err-map)}
     
     (= err-code ERR_TICKET_USED_UP)
     {:status 500 :body (ticket-used-up err-map)}
     
     :else
     {:status 500 :body (default-error err-map)})))
