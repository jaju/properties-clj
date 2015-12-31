(defproject org.msync/properties-clj "0.3.2"
  :description "A simple wrapper for properties files with useful hierarchical keys based property-maps"

  :min-lein-version "2.0.0"

  :dependencies [[org.clojure/clojure "1.8.0-RC4"]
                 [midje "1.7.0" :exclusions [org.clojure/clojure]]]

  :profiles {
             :dev {
                   :dependencies [[midje "1.7.0" :exclusions [org.clojure/clojure]]]
                   :plugins [[lein-midje "3.1.3"]]
                   }
             }
  )
