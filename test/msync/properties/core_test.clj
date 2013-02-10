(ns msync.properties.core-test
  (:use midje.sweet
        msync.properties.core)
  (:import [java.io FileNotFoundException StringBufferInputStream]))


(fact "Read a properties file and convert into a map with keys converted to clojure-keywords."
  (let [input-str "a.A=Y\nb=Z"]
      (map (read-config-str input-str) (sorted-set :a :b)) => '({:A "Y"} "Z")
      ))

(fact "Sub-configs from a config-map given a prefix. Note that these configs are just associative maps."
  (let [input-str "some-prefix.a = A\nsome-prefix.b = B"]
      (-> input-str read-config-str :some-prefix) => {:a "A" :b "B"}))

(fact "Some more extensive checks for sub-configs."
      (let [input-str "prefix1.prefix2.a = ABC
                       prefix1.prefix2.b = XYZ
                       prefix2.prefix1.Z = FOO"]
        (-> input-str read-config-str :prefix1 :prefix2) => {:a "ABC" :b "XYZ"}
        (-> input-str read-config-str :prefix2 :prefix1) => {:Z "FOO"}
        (-> input-str (read-config-str :nest-keys? false)) => {:prefix1.prefix2.a "ABC" :prefix1.prefix2.b "XYZ" :prefix2.prefix1.Z "FOO"}))

(fact "When a file is not found, and a default return is supplied, return it.
      Otherwise, bubble up the exception."
      (let [default-map {:a :A :b "B"}]
        (against-background
         (#'msync.properties.core/load-props "non-existent-file.txt") =throws=> (FileNotFoundException.))
        (read-config "non-existent-file.txt" :default default-map) => default-map
        (read-config "non-existent-file.txt") => (throws FileNotFoundException)))

(fact "Trims spaces around the values - effectively ignoring them"
      (let [input-str "a = A \n b = B"]
        (read-config-str input-str) => {:a "A" :b "B"}))

(fact "Creates nested maps-of-maps for hierarchical configuration data"
      (let [input-str "a = A
                      b = B
                      c.a = CA
                      c.b = CB
                      d.c.a = DCA
                      d.c.b = DCB
                      d.c.e.f = DCEF"
            res {:a "A"
                 :b "B"
                 :c {:a "CA"
                     :b "CB"}
                 :d {:c {:a "DCA"
                         :b "DCB"
                         :e {:f "DCEF"}}}}]
        (read-config-str input-str) => (just res)))

(fact "Creates a properties map from string, and checks for correctness after round-trip."
      (let [input-str "a = A
                      b = B
                      c.a = CA
                      c.b = CB
                      d.c.a = DCA
                      d.c.b = DCB
                      d.c.e.f = DCEF    "
            res {:a "A"
                 :b "B"
                 :c {:a "CA"
                     :b "CB"}
                 :d {:c {:a "DCA"
                         :b "DCB"
                         :e {:f "DCEF"}}}}]
        (read-config-str input-str) => (just res)
        (read-config-str (write-config (read-config-str input-str))) => (just res)))

(fact "Returns a sorted properties-format string from a map"
  (write-config {:a "A" :b {:c "d"} :x {:y {:z "FOO"}}}) => "a = A\nb.c = d\nx.y.z = FOO")

(fact "Reads properties from a supplied file on disk."
  (read-config "test/resources/test.properties") =>
  {
   :name "configuration-clj"
   :version "0.2.4"
   :db {
        :name "whacko-db"
        :host "example.org"
        :port "4567"}
  :ring {
         :handler {
                   :ns "some-ring-handler-ns"
                   :protocol "binary"}}})