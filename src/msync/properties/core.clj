(ns msync.properties.core
  (:import java.util.Properties
           [java.io StringBufferInputStream FileNotFoundException])
  (:require [clojure.string :refer [split join trim]]
            [clojure.java.io :refer [reader]]
            [clojure.edn :as edn]
            [clojure.walk :refer [postwalk]]))

;;; Util
(defn- key->path
  "Convert a possibly dotted key to a seq of keywords."
  [k]
  (map keyword (split k #"\.")))


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

(defn- ^:testable rewrite-placeholder-string [s smap]
  (clojure.string/replace s #"\$(\w+)" (fn [[k v]] (get smap v "NONE"))))

(defn- ^:testable rewrite-from-env [in-map smap]
  (postwalk (fn [v] (if (string? v) (rewrite-placeholder-string v smap) v)) in-map))

;;; API
(defn read-properties
  "Read a Java properties file into a map. Replaces $ENVVAR placeholders from the environment."
  ([readable & {:keys [default nest-keys?] :or {nest-keys? true}}]
     (try
       (let [keyfn (or (and nest-keys? key->path) (comp vector keyword))
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
       (rewrite-from-env (edn/read-string (slurp readable)) (getenv))
       (if default
         default
         (throw (FileNotFoundException. (str "No such file: " readable)))))))
