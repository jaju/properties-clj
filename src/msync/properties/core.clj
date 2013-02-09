(ns msync.properties.core
  (:import java.util.Properties
           java.io.FileNotFoundException)
  (:require [clojure.string :refer [split join]]
            [clojure.java.io :refer [reader]]))

;;; Util
(defn- key->path
  "Convert a possibly dotted key to a seq of keywords."
  [k]
  (map keyword (split k #"\.")))


(defn- load-props
  "Given a path to a properties file, load it into a Java Properties object."
  [file-path]
  (let [props (-> file-path reader)]
    (doto (Properties.)
      (.load props))))


(defn- fold-props
  [props keyfn]
  (reduce (fn [res k] (assoc-in res (keyfn k) (get props k)))
          {} (keys props)))


(defn- dump-config
  "Dump a config map to a sequence of key-value pairs collapsing
   all nested keys if any."
  [props]
  (reduce-kv (fn [r k v] (if (map? v)
                          (let [k1 (name k)]
                            (concat (map (fn [[k2 v2]] [(str k1 "." (name k2)) v2])
                                         (write-config* [] v))
                                    r))
                          (conj r [(name k) v])))
             []
             props))


;;; API
(defn read-config
  "Read a Java properties file into a map."
  ([file-path & {:keys [default nest-keys?] :or {nest-keys? true}}]
     (try
       (let [keyfn (or (and nest-keys? key->path) vector)
             props (load-props file-path)]
         (fold-props props keyfn))
       (catch FileNotFoundException e
         (or default (throw e))))))


(defn write-config
  "Dump a (nested) property map to Java properties format."
  [props]
  (->> (dump-config props)
       (sort-by first)
       (map (fn [[k v]] (str k " = " v)))
       (join "\n")))
