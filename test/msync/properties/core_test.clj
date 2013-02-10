(ns msync.properties.core-test
  (:use midje.sweet
        msync.properties.core)
  (:import [java.io FileNotFoundException StringBufferInputStream]))


(defn- mock-load-props
  "Load a property string into a Java Properties object."
  [prop-str]
  (doto (java.util.Properties.)
    (.load (StringBufferInputStream. prop-str))))

(fact "Read a properties file and convert into a map with keys converted to clojure-keywords."
  (map (read-config "some-file.txt") (sorted-set :a :b)) => '({:A "Y"} "Z")
  (provided (#'msync.properties.core/load-props "some-file.txt") => (mock-load-props "a.A=Y\nb=Z")) :times 1)

(fact "Sub-configs from a config-map given a prefix. Note that these configs are just associative maps."
 (-> "some-properties.txt" read-config :some-prefix) => {:a "A" :b "B"}
  (provided (#'msync.properties.core/load-props "some-properties.txt") => (mock-load-props "some-prefix.a = A\nsome-prefix.b = B")))

(fact "Some more extensive checks for sub-configs."
  (let [file-contents "prefix1.prefix2.a = ABC
                       prefix1.prefix2.b = XYZ
                       prefix2.prefix1.Z = FOO"]
    (-> "some-properties.txt" read-config :prefix1 :prefix2) => {:a "ABC" :b "XYZ"}
    (provided (#'msync.properties.core/load-props "some-properties.txt") => (mock-load-props file-contents))
    (-> "some-properties.txt" read-config :prefix2 :prefix1) => {:Z "FOO"}
    (provided (#'msync.properties.core/load-props "some-properties.txt") => (mock-load-props file-contents))))

(fact "When a file is not found, and a default return is supplied, return it.
      Otherwise, bubble up the exception."
   (let [default-map {:a :A :b "B"}]
     (against-background
       (#'msync.properties.core/load-props "non-existent-file.txt") =throws=> (FileNotFoundException.))
     (read-config "non-existent-file.txt" :default default-map) => default-map
     (read-config "non-existent-file.txt") => (throws FileNotFoundException)))

(fact "Trims spaces around the values - effectively ignoring them"
  (let [file-contents "a = A \n b = B"]
    (read-config "foo.txt") => {:a "A" :b "B"}
    (provided (#'msync.properties.core/load-props "foo.txt") => (mock-load-props file-contents))))

 (fact "Creates nested maps-of-maps for hierarchical configuration data"
   (let [file-contents "a = A
                      b = B
                      c.a = CA
                      c.b = CB
                      d.c.a = DCA
                      d.c.b = DCB
                      d.c.e.f = DCEF"]
     (read-config "some-properties.txt") => {:a "A"
                            :b "B"
                            :c {
                                :a "CA"
                                :b "CB"
                                }
                            :d {
                                :c {
                                    :a "DCA"
                                    :b "DCB"
                                    :e {:f "DCEF"}}}}
     (provided (#'msync.properties.core/load-props "some-properties.txt") => (mock-load-props file-contents))))
