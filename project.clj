(defproject kifshare "0.1.0-SNAPSHOT"
  :description "iPlant Quickshare for iRODS"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.clojure/tools.cli "0.2.1"]
                 [noir "1.3.0-beta3"]
                 [org.iplantc/clj-jargon "0.1.1-SNAPSHOT"]
                 [org.iplantc/clojure-commons "1.2.0-SNAPSHOT"]
                 [slingshot "0.10.1"]]
  :iplant-rpm {:summary "kifshare",
               :release 1,
               :dependencies ["iplant-service-config >= 0.1.0-5"],
               :config-files ["log4j.properties"],
               :config-path "conf"}
  :main kifshare.server)

