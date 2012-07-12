(defproject kifshare "0.1.0-SNAPSHOT"
  :description "iPlant Quickshare for iRODS"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.clojure/tools.cli "0.2.1"]
                 [noir "1.3.0-beta3"]
                 [org.iplantc/clj-jargon "0.1.1-SNAPSHOT"]
                 [org.iplantc/clojure-commons "1.2.0-SNAPSHOT"]
                 [slingshot "0.10.1"]
                 [org.clojure/tools.logging "0.2.3"]
                 [de.ubercode.clostache/clostache "1.3.0"]
                 [log4j/log4j "1.2.16"]]
  :iplant-rpm {:summary "kifshare",
               :dependencies ["iplant-service-config >= 0.1.0-5"],
               :config-files ["log4j.properties"],
               :config-path "conf"}
  :plugins [[org.iplantc/lein-iplant-rpm "1.3.0-SNAPSHOT"]]
  :repositories {"iplantCollaborative"
                 "http://projects.iplantcollaborative.org/archiva/repository/internal/",
                 "renci.repository"
                 "http://ci-dev.renci.org/nexus/content/repositories/snapshots/",
                 "sonatype"
                 "http://oss.sonatype.org/content/repositories/releases"}
  :main kifshare.server)

