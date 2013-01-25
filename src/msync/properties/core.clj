(ns msync.properties.core
  (:import [java.io FileNotFoundException]))

(defn- remove-prefix-from-keyword [kw prefix]
  (let [kw     (name kw)
        prefix (name prefix)]
    (keyword (.substring kw (-> prefix count inc)))))

(defn- key-starts-with? [[k v] prefix]
  (let [k (name k) prefix (str (name prefix) ".")]
    (.startsWith k prefix)))

(defn- chop-prefix-from-entry [[k v] prefix]
  [(remove-prefix-from-keyword k prefix) v])

(defn- group-by-first-prefix [coll]
  (group-by #(first (clojure.string/split (name (first %)) #"\.")) coll))

(defn- peek-and-count-values [some-pairs]
  (reduce #(+ % (count %2)) 0 some-pairs))

(defn- remove-prefix-from-keys
  [coll prefix]
  (into {} (map #(chop-prefix-from-entry % prefix) coll)))

(defn sub-config
  "Given a map and a prefix, return another map with only those entries where the key in the original map has the same prefix - additionally with the prefix removed from all keys in the resultant map."
  [coll prefix]
  (let [prefix (name prefix)
        matching-coll (filter #(key-starts-with? % prefix) coll)]
    (remove-prefix-from-keys matching-coll prefix)))

(defn- denoise-entry
  "Intended for a map-entry where the 'second' is another map. Take a pair of key (as prefix) and a map (as value), and return a transformed pair of the same key and, and the value transformed such that all keys in it have the prefix stripped. Doesn't handle well those cases where the value-map contains mixed kinds of keys - i.e., keys both with and without the prefix."
  [[k coll]]
  (let [sc (sub-config coll k)]
    (if (= 0 (count sc))
      [(-> coll first first) (-> coll first second)]
      [k sc])))

(defn java-properties-from-string
  [content-str]
  (doto (java.util.Properties.)
    (.load
     (java.io.StringBufferInputStream. content-str))))

(defn parse-config-from-string
  "Given a string representing the contents of a properties file, parse to create a map."
  [s]
  (let [props (java-properties-from-string s)]
    (zipmap (map keyword (keys props)) (vals props))))

(defn read-config
  "Given a path to a properties file, load into a map. If a default is supplied, that is returned in case of any FileNotFoundException error occuring."
  ([file-path]
     (parse-config-from-string (slurp file-path)))
  ([file-path default-return-map]
     (try (read-config file-path)
          (catch FileNotFoundException fne default-return-map))))

(defn denoise [coll]
  (map denoise-entry (group-by-first-prefix coll)))