(defproject kifshare "0.1.2-SNAPSHOT"
  :description "iPlant Quickshare for iRODS"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/tools.cli "0.2.1"]
                 [org.clojure/tools.logging "0.2.3"]
                 [org.clojure/core.memoize "0.5.3"]
                 [org.iplantc/clj-jargon "0.2.9-SNAPSHOT"]
                 [org.iplantc/clojure-commons "1.4.1-SNAPSHOT"]
                 [cheshire "5.0.1"]
                 [slingshot "0.10.1"]
                 [compojure "1.1.3"]
                 [ring/ring-jetty-adapter "1.1.6"]
                 [ring/ring-devel "1.1.6"]
                 [de.ubercode.clostache/clostache "1.3.0"]
                 [com.cemerick/url "0.0.6"]
                 [lamina "0.5.0-beta9"]
                 [clj-http "0.6.5"]]

  :ring {:init kifshare.config/init
         :handler kifshare.core/app}

  :profiles {:dev {:resource-paths ["build"]
                   :dependencies [[midje "1.4.0"]]
                   :plugins [[lein-midje "2.0.1"]]}}

  :iplant-rpm {:summary "kifshare",
               :dependencies ["iplant-service-config >= 0.1.0-5" "iplant-clavin"],
               :config-files ["log4j.properties"],
               :config-path "conf"}

  :plugins [[lein-ring "0.7.5"]
            [org.iplantc/lein-iplant-rpm "1.4.1-SNAPSHOT"]]

  :repositories {"iplantCollaborative"
                 "http://projects.iplantcollaborative.org/archiva/repository/internal/",

                 "renci.repository"
                 "http://ci-dev.renci.org/nexus/content/repositories/snapshots/",

                 "sonatype"
                 "http://oss.sonatype.org/content/repositories/releases"}

  :aot [kifshare.core]
  :main kifshare.core)
