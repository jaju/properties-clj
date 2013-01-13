(ns msync.properties.core-test
  (:use midje.sweet
        msync.properties.core)
  (:import [java.io FileNotFoundException]))

(fact "Read a properties file and convert into a map with keys converted to clojure-keywords."
  (map (read-config "some-file.txt") (sorted-set :a.A :b)) => '("Y" "Z")
  (provided (slurp "some-file.txt") => "a.A=Y\nb=Z") :times 1)

(fact "Sub-configs from a config-map given a prefix. Note that these configs are just associative maps."
  (let [config-map {:some-prefix.a :A :some-prefix.b :B}]
    (sub-config config-map :some-prefix) => {:a :A :b :B}))

(fact "Some more extensive checks for sub-configs."
  (let [config-map {:prefix1.prefix2.a :ABC :prefix1.prefix2.b :XYZ :prefix2.prefix1.Z "FOO"}]
    (-> config-map (sub-config :prefix1) (sub-config :prefix2)) => {:a :ABC :b :XYZ}
    (-> config-map (sub-config :prefix2.prefix1)) => {:Z "FOO"})
  )

(fact "When a file is not found, and a default return is supplied, return it. Otherwise, bubble up the exception."
  (let [default-map {:a :A :b "B"}]
    (against-background (slurp "non-existent-file.txt") =throws=> (FileNotFoundException.))
    (read-config "non-existent-file.txt" default-map) => default-map
    #_(read-config "non-existent-file.txt") =throws=> (FileNotFoundException.)))
