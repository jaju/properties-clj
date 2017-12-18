(ns msync.properties.core
  (:import java.util.Properties
           [java.io StringBufferInputStream FileNotFoundException])
  (:require [msync.properties.common :as common]
            [clojure.string :refer [split join trim]]
            [clojure.java.io :refer [reader]]
            [clojure.edn :as edn]
            [clojure.walk :refer [postwalk]]))

;;; Util

(defn- load-props
  "Given a path to a properties file, load it into a Java Properties object."
  [readable]
  (let [props (reader readable)]
    (doto (Properties.)
      (.load props))))


(defn- fold-props
  [props keyfn]
  (reduce (fn [res k] (assoc-in res (keyfn k) (-> props (get k) trim)))
          {} (keys props)))


(defn- dump-properties
  "Dump a map to a sequence of key-value pairs collapsing to dot-separated form
   all nested keys if any."
  [props]
  (reduce-kv (fn [r k v] (if (map? v)
                           (let [k1 (name k)]
                             (concat (map (fn [[k2 v2]] [(str k1 "." (name k2)) v2])
                                          (dump-properties v))
                                     r))
                           (conj r [(name k) v])))
             []
             props))


(defn- getenv []
  (System/getenv))

(defn- ^:testable split-at-first [str re]
  (let [parts (split str re)]
    (if (< (count parts) 3)
      parts
      (list (first parts) (clojure.string/join (str re) (rest parts))))))

(defn- get-default [str]
  (let [splits (split-at-first str #":")]
    (second splits)))

(defn- ^:testable rewrite-placeholder-string [s smap]
  (clojure.string/replace s
                          #"\$\{(.*?)\}"
                          (fn [[k v]]
                              (let [default (get-default v)]
                                   (get smap v (or default "NONE"))))))

(defn- ^:testable rewrite-from-env [in-map smap]
  (postwalk
    (fn [v]
      (if (string? v)
        (rewrite-placeholder-string v smap)
        v))
    in-map))

;;; API
(defn read-properties
  "Read a Java properties file into a map. Replaces ${ENVVAR} placeholders from the environment."
  ([readable & {:keys [default nest-keys?] :or {nest-keys? true}}]
   (try
     (let [readable (if (string? readable)
                      (rewrite-placeholder-string readable (getenv))
                      readable)
           keyfn (or (and nest-keys? common/key->path) (comp vector keyword))
           props (load-props readable)]
       (rewrite-from-env (fold-props props keyfn) (getenv)))
     (catch FileNotFoundException e
       (or default (throw e))))))


(defn- ^:testable read-properties-str
  "Read a Java properties string into a map."
  [prop-str & {:keys [default nest-keys?] :or {nest-keys? true}}]
  (read-properties (StringBufferInputStream. prop-str) :default default :nest-keys? nest-keys?))


(defn- ^:testable write-properties
  "Dump a (nested) property map to Java properties format."
  [props]
  (->> (dump-properties props)
       (sort-by first)
       (map (fn [[k v]] (str k " = " v)))
       (join "\n")))

(defn- ^:testable file-exists? [file-path]
  (.exists (clojure.java.io/file file-path)))

(defn read-edn
  "Reads an EDN file into a map. Duh! Oh yes - replaces $ENVVAR placeholders from the environment."
  ([readable & {:keys [default]}]
   (if (file-exists? readable)
     (-> readable
         slurp
         edn/read-string
         (rewrite-from-env (getenv)))
     (if default
       default
       (throw (FileNotFoundException. (str "No such file: " readable)))))))
