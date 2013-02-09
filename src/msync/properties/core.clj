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


(defn- fold-props
  [props keyfn]
  (reduce (fn [res k] (assoc-in res (keyfn k) (get props k)))
          {} (keys props)))


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
