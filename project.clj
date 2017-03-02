(defproject org.msync/properties-clj "0.4.0-SNAPSHOT"
  :description "A simple wrapper for properties files with useful hierarchical keys based property-maps"

  :min-lein-version "2.0.0"

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [midje "1.8.3" :exclusions [org.clojure/clojure]]

                 [org.clojure/data.xml "0.0.8"]
                 [org.clojure/data.zip "0.1.2"]]

  :profiles {
             :dev {
                   :dependencies [[midje "1.8.3" :exclusions [org.clojure/clojure]]]
                   :plugins [[lein-midje "3.1.3"]]
                   }
             }

  :deploy-repositories [["releases" :clojars]
                        ["snapshots" :clojars]]
  )
