(ns weblog-riemann.core
  (:require [clj-kafka.core :refer :all]
            [clj-kafka.consumer.zk :refer :all]
            [cheshire.core :as json]
            [clojure.string :as string]
            [riemann.client :refer (tcp-client send-event)]
            [clojure.set :refer (rename-keys)])
  (:import [java.lang String]))

(def config {"zookeeper.connect" (System/getenv "ZOOKEEPER")
             "group.id" "weblog-riemann"
             "auto.offset.reset" "largest"
             "auto.commit.enable" "false"})

(defn- parse
  [m]
  (try
    (-> m
        .message
        String.
        (json/decode (fn [k] (keyword (string/replace k #"_" "-"))))
        (update :content #(json/decode % (fn [k] (keyword (string/replace k #"_" "-"))))))
    (catch Exception e nil)))

(defn- transform
  [m]
  (let [m (-> m
              (merge (:content m))
              (dissoc :content))]
    (-> m
       (rename-keys {:hostname :host})
       (assoc :service "nginx requests")
       (assoc :time (-> m :msec read-string int))
       (update :body-bytes-sent (comp int read-string))
       (update :request-time (comp int (partial * 1000) read-string))
       (dissoc :msec :timestamp))))


(def process (comp (map parse)
                   (remove nil?)
                   (map transform)))

(defn- send
  [r m]
  ;(clojure.pprint/pprint m)
  (send-event r m))

(defn -main [& args]
  (with-resource [kafka (consumer config)]
    shutdown
    (let [riemann (tcp-client {:host (or (System/getenv "RIEMANN") "127.0.0.1")})
          stream (create-message-stream kafka "syslogger")]
      (run! (partial send riemann) (eduction process stream)))))
