(ns msync.properties.common
  (:require [clojure.string :refer [split]]))

(defn key->path
  "Convert a possibly dotted key to a seq of keywords."
  [k]
  (map keyword (split k #"\.")))
