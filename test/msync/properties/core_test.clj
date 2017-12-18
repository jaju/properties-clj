(ns msync.properties.core-test
  (:require [midje.sweet :refer :all]
            [midje.util :refer [expose-testables]]
            [msync.properties.core :refer :all])
  (:import [java.io FileNotFoundException StringBufferInputStream]))

(expose-testables msync.properties.core)

(fact "Read a properties file and convert into a map with keys converted to clojure-keywords."
  (let [input-str "a.A=Y\nb=Z"]
      (map (read-properties-str input-str) (sorted-set :a :b)) => '({:A "Y"} "Z")))


(fact "Single map-value from multiple properties, given the same prefix, with the prefix as the key in the top-level."
  (let [input-str "some-prefix.a = A\nsome-prefix.b = B"]
      (-> input-str read-properties-str :some-prefix) => {:a "A" :b "B"}))

(fact "Some more extensive checks for sub-maps."
      (let [input-str "prefix1.prefix2.a = ABC
                       prefix1.prefix2.b = XYZ
                       prefix2.prefix1.Z = FOO"]
        (-> input-str read-properties-str :prefix1 :prefix2) => {:a "ABC" :b "XYZ"}
        (-> input-str read-properties-str :prefix2 :prefix1) => {:Z "FOO"}
        (-> input-str (read-properties-str :nest-keys? false)) => {:prefix1.prefix2.a "ABC" :prefix1.prefix2.b "XYZ" :prefix2.prefix1.Z "FOO"}))

(fact "When a file is not found, and a default return is supplied, return it.
      Otherwise, bubble up the FileNotFoundException."
      (let [default-map {:a :A :b "B"}]
        (read-properties "non-existent-file.txt" :default default-map) => default-map
        (read-properties "non-existent-file.txt") => (throws FileNotFoundException)
        (provided
          (#'msync.properties.core/load-props "non-existent-file.txt") =throws=> (FileNotFoundException.))))


(fact "When a file is not found, and a default return is supplied, return it.
      Otherwise, throw a FileNotFoundException. For the EDN file."
      (let [default-map {:a :A :b "B"}]
        (read-edn "non-existent-file.txt" :default default-map) => default-map
        (read-edn "non-existent-file.txt") => (throws FileNotFoundException)
        (provided
          (#'msync.properties.core/file-exists? "non-existent-file.txt") => false)))


(fact "Trims spaces around the values in properties - effectively ignoring them"
      (let [input-str "a = A \n b = B"]
        (read-properties-str input-str) => {:a "A" :b "B"}))

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
        (read-properties-str input-str) => (just res)))

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
        (read-properties-str input-str) => (just res)
        (read-properties-str (write-properties (read-properties-str input-str))) => (just res)))

(fact "Returns a sorted properties-format string from a map"
  (write-properties {:a "A" :b {:c "d"} :x {:y {:z "FOO"}}}) => "a = A\nb.c = d\nx.y.z = FOO")

(def env-for-test {"HOME" "/home/jaju"
                   "APPDIR" "/opt/appdir"
                   "SECRETFILE" "secrets.key"
                   "USER" "some_random_user"})

(fact "Reads properties from a supplied file on disk."
  (read-properties "test/resources/test.properties") =>
  {
   :name "configuration-clj"
   :version "0.2.4"
   :db {
        :name "some_random_user-whacko-db"
        :host "example.org"
        :port "4567"}
   :ring {
          :handler {
                    :ns "some-ring-handler-ns"
                    :protocol "binary"}}}
  (provided (#'msync.properties.core/getenv) => env-for-test))

(fact "Reads EDN from a supplied file on disk."
  (read-edn "test/resources/test-properties.edn") =>
  {
   :name "configuration-clj"
   :version "0.2.4"
   :db {
        :name "whacko-db"
        :host "example.org"
        :port 4567}

   :ring {
          :handler {
                    :ns "some-ring-handler-ns"
                    :protocol "binary"}}})



(fact "Replaces ${KEY} place-holders in strings from values in the supplied map"
  (let [env env-for-test]
    (rewrite-placeholder-string "${HOME}/abc" env) => "/home/jaju/abc"
    (rewrite-placeholder-string "${APPDIR}/config/${SECRETFILE}" env) => "/opt/appdir/config/secrets.key"))

(fact "Replaces $KEY place-holders in string values of the input map from values in supplied map."
  (let [env env-for-test
        config-map {
                    :file "${HOME}/configuration-clj"
                    :db {:data-dir "${APPDIR}/whacko-db"}
                    :password "${HOME}/config/${SECRETFILE}"
                    :this-needs-a-default "${BOMB:not-found}"
                    :another-default "${HOME}/config/${APPPATH:foo/bar/baz}"}]
    (rewrite-from-env config-map env) => {
                                          :file "/home/jaju/configuration-clj"
                                          :db {:data-dir "/opt/appdir/whacko-db"}
                                          :password "/home/jaju/config/secrets.key"
                                          :this-needs-a-default "not-found"
                                          :another-default "/home/jaju/config/foo/bar/baz"}))
