(ns msync.properties.core
  (:import java.util.Properties
           java.io.FileNotFoundException)
  (:require [clojure.string :refer [split]]
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


;;; API
(defn read-config
  "Read a Java properties file into a map."
  ([file-path]
     (let [props (load-props file-path)]
       (reduce (fn [res k] (assoc-in res (key->path k) (get props k)))
               {} (keys props))))
  ([file-path default-map]
     (try (read-config file-path)
          (catch FileNotFoundException _ default-map))))
