(defproject org.msync/properties-clj "0.4.0-SNAPSHOT"
  :description "A simple wrapper for properties files with useful hierarchical keys based property-maps"

  :min-lein-version "2.8.1"

  :dependencies [[org.clojure/clojure "1.9.0" :scope "provided"]
                 [org.clojure/data.zip "0.1.2"]]

  :profiles {:dev {:dependencies [[com.rpl/specter "1.0.5" :scope "test" :exclusions [[org.clojure/clojure] [org.clojure/clojurescript]]]
                                  [midje "1.9.0" :exclusions [org.clojure/clojure] :scope "test"]]
                   :plugins      [[lein-midje "3.2.1"]]}
             :1.8 {:dependencies [[org.clojure/clojure "1.8.0"]]}
             :1.7 {:dependencies [[org.clojure/clojure "1.7.0"]]}}

  :deploy-repositories [["releases" :clojars]
                        ["snapshots" :clojars]])
