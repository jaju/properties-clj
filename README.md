# properties-clj [![Build Status](https://secure.travis-ci.org/jaju/properties-clj.png)](http://travis-ci.org/jaju/properties-clj)
==========
`properties-clj` is a simple `java.util.Properties` based library for reading `*.properties` files. Essentially, files with the following format:

    property1 = value1
    property2 = value2
    context1.property1 = context1-value1
    context1.property2 = context1-value2
    ...

While it returns a `map` with the keywords turned into symbols from any properties file, it can take any map with "contextual" keys and a prefix, and return a "simplified" sub-map with new keys, without the redundant prefixes (which correspond to the sub-context that the configuration applies to.)

# Dependency
    [org.msync/properties-clj "0.1.0"]
(Available via [clojars](https://clojars.org/search?q=properties-clj))

# Example
==========
For parsing the above sample, in a file named `application.properties`

```clj
(require '[msync.properties.core :as properties-reader])
```
...
```clj
(def props (properties-reader/read-config "path/to/application.properties"))
;;; => {:property1 "value1" :property2 "value2" :context1.property1
;;; "context1-value1" :context1.property2 "context1-value2"}

(properties-reader/sub-config props :context1)
;;; => {:property1 "context1-value1" :property2 "context1-value2"}

(properties-reader/read-config "some-non-existent-file")
;;; Throws a FileNotFoundException

(properties-reader/read-config "some-non-existent-file" {:default-key "DefaultValue"})
;;; => {:default-key "DefaultValue"} - No exception thrown as long it
;;; is a FileNotFoundException one.
```

## License
Copyright &copy; 2013 Ravindra R. Jaju. Distributed under the [Eclipse Public License](http://www.eclipse.org/legal/epl-v10.html), the same as Clojure.
