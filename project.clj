(defproject org.msync/properties-clj "0.2.0-SNAPSHOT"
  :description "A simple wrapper for properties files with useful hierarchical keys based property-maps"

  :min-lein-version "2.0.0"

  :dependencies [[org.clojure/clojure "1.4.0"]
                 [midje "1.5-alpha5" :exclusions [org.clojure/clojure]]]

  :dev-dependencies [[lein-midje "3.0-alpha2"]]

  :warn-on-reflection true
  )
