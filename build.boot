(set-env!
 :source-paths #{"src"}
 :resource-paths #{"resources"}
 :dependencies '[[org.clojure/clojure "1.8.0"]
                 [mount "0.1.10"]
                 [cheshire "5.5.0"]
                 [clj-time "0.11.0"]
                 [clj-kafka "0.3.4"]
                 [riemann-clojure-client "0.4.2"]])

(task-options!
 pom {:project 'weblog-riemann
      :version "0.1.0"}
 jar {:main 'weblog-riemann.core
      :file "weblog-riemann.jar"})

(require '[weblog-riemann.core :refer (-main)])

(deftask run []
  (with-pre-wrap fileset
    (-main)
    fileset))
