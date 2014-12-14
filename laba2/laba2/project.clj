(defproject laba2 "0.0.1-SNAPSHOT"
  :description "FIXME: write description"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [http-kit "2.1.16"]
                 [enlive "1.1.1"]
                 [http.async.client "0.5.2"]
                 [net.cgrand/moustache "1.1.0"]]
  :javac-options ["-target" "1.6" "-source" "1.6" "-Xlint:-options"]
  :aot [laba2.core]
  :main laba2.core)
