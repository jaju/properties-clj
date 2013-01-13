(ns msync.properties.core)

(defn read-config [file-path]
  (let
      [
       file-contents (slurp file-path)
       reader (java.io.StringBufferInputStream. file-contents)
       props (doto (java.util.Properties.) (.load reader))
       ]
    (zipmap (map keyword (keys props)) (vals props))))
