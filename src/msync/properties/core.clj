(ns msync.properties.core
  (:import [java.io FileNotFoundException]))

(defn read-config
  ([file-path]
     (let [file-contents (slurp file-path)
           reader        (java.io.StringBufferInputStream. file-contents)
           props         (doto (java.util.Properties.) (.load reader))]
       (zipmap (map keyword (keys props)) (vals props))))
  ([file-path default-return-map]
     (try (read-config file-path)
          (catch FileNotFoundException fne default-return-map))))

(defn- remove-prefix-from-keyword [kw prefix]
  (let [kw     (name kw)
        prefix (name prefix)]
    (keyword (.substring kw (-> prefix count inc)))))

(defn- key-starts-with? [[k v] prefix]
  (.startsWith (name k) (name prefix)))

(defn- remove-noise-from-entry [[k v] noisy-prefix]
  [(remove-prefix-from-keyword k noisy-prefix) v])

(defn sub-config [config-map prefix]
  (let [prefix (name prefix)
        matching-entries (filter #(key-starts-with? % prefix) config-map)]
    (into {} (map #(remove-noise-from-entry % prefix) matching-entries))))