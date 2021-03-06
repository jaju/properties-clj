# properties-clj [![Build Status](https://secure.travis-ci.org/jaju/properties-clj.png)](http://travis-ci.org/jaju/properties-clj)
`properties-clj` is a simple `java.util.Properties` based library for reading `*.properties` files. Essentially, files with the following format:

    property1 = value1
    property2 = value2
    context1.property1 = context1-value1
    context1.property2 = context1-value2
    one-more-property = some-string-${ENV_VARIABLE}-and-it-ends
    ...

While it returns a `map` with the keywords turned into symbols from any properties file, it can take any map with "contextual" keys and a prefix, and return a "simplified" sub-map with new keys, without the redundant prefixes (which correspond to the sub-context that the configuration applies to.)

# Dependency
    [org.msync/properties-clj "0.4.0-SNAPSHOT"]
(Available via [clojars](https://clojars.org/search?q=properties-clj))

# Example
For parsing the above sample, in a file named `application.properties` in $HOME (i.e., the user's home directory)

```clj
(require '[msync.properties.core :as properties-reader])
```
...
```clj
(read-properties "${HOME}/application.properties")
;;; => {:ring {:handler {:protocol "binary", :ns "some-ring-handler-ns"}}, :name "configuration-clj",
;;;     :db {:host "example.org", :name "whacko-db", :port "4567"}, :version "0.2.4"}

;;; Accessing `sub-maps`
(get-in (read-properties "test/resources/test.properties") [:ring :handler :protocol])
;;; => "binary"
```

```properties
name = configuration-clj
version = 0.2.4
db.name = ${USER}-whacko-db
db.host = ${NON_EXISTENT_HOSTNAME_ENV:example.org}
db.port = 4567
ring.handler.ns = some-ring-handler-ns
ring.handler.protocol = binary
```

```clj
(read-properties "test/resources/test.properties" :nest-keys? false)
;;; {"ring.handler.protocol" "binary", "ring.handler.ns" "some-ring-handler-ns", "db.host" "example.org",
;;;   "name" "configuration-clj", "db.name" "jaju-whacko-db", "db.port" "4567", "version" "0.2.4"}

(read-properties "/some/file/that/does/not/exist") ;; => Raises FileNotFoundException

(read-properties "/some/file/that/does/not/exist" :default {}) ;; => {}

;;; Converting a nested Clojure map to Java properties format
(write-properties {:foo "bar" :baz {:quux 42}})
;;; => "baz.quux = 42\nfoo = bar"

;;; Also works with EDN
(read-edn "path-to-edn-file")
;;; => A map with values from the EDN file appropriately substituted for env-vars
```

## Contributors
* [Ravindra R. Jaju](https://msync.org/). - [jaju](https://github.com/jaju)
* [Baishampayan Ghose](http://freegeek.in). - [ghoseb](https://github.com/ghoseb)

## License
Distributed under the [Eclipse Public License](http://www.eclipse.org/legal/epl-v10.html), the same as Clojure.
