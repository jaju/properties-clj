(ns msync.properties.core-test
  (:use midje.sweet
        msync.properties.core))

(fact
  (map (read-config "some-file.txt") (sorted-set :a :b)) => '("A" "B")
  (provided (slurp "some-file.txt") => "a=A\nb=B") :times 1)