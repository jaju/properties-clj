(ns msync.properties.core-test
  (:use midje.sweet
        msync.properties.core))

(fact
  (map (read-config "some-file.txt") (sorted-set :a.A :b)) => '("Y" "Z")
  (provided (slurp "some-file.txt") => "a.A=Y\nb=Z") :times 1)

(fact
  (let [config-map {:some-prefix.a :A :some-prefix.b :B}]
    (sub-config config-map :some-prefix) => {:a :A :b :B}))

(fact
  (let [config-map {:prefix1.prefix2.a :ABC :prefix1.prefix2.b :XYZ :prefix2.prefix1.Z "FOO"}]
    (-> config-map (sub-config :prefix1) (sub-config :prefix2)) => {:a :ABC :b :XYZ}
    (-> config-map (sub-config :prefix2.prefix1)) => {:Z "FOO"})
  )