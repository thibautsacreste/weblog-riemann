(ns weblog-riemann.core
  (:require [clj-kafka.core :refer :all]
            [clj-kafka.consumer.zk :refer :all])
  (:import [java.lang String]))

(def config {"zookeeper.connect" (System/getenv "ZOOKEEPER")
             "group.id" "weblog-riemann"
             "auto.offset.reset" "smallest"
             "auto.commit.enable" "false"})

(defn- parse
  [m]
  (-> m .message String.))

(def xform (map parse))

(defn -main [& args]
  (with-resource [c (consumer config)]
    shutdown
    (let [stream (create-message-stream c "syslogger" (default-decoder) (default-decoder))]
      (run! println (eduction xform stream)))))
