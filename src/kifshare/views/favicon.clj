(ns kifshare.views.favicon
  (:use [noir.core :only [defpage pre-route]] 
        [noir.response :only [status redirect]]))

(pre-route "/favicon.ico"
  []
  (status 404 "I ain't got no favicon.ico, hyuk."))