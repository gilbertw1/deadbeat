(ns deadbeat.slack.rtm
  (:require
    [deadbeat.slack.api :as api]
    [manifold.stream :as s]
    [manifold.deferred :as d]
    [cheshire.core :as json]
    [aleph.http :as http]))

(defrecord RtmConnection [ws next-id self team users channels bots groups ims])

(defn- take-event! [ws]
  (json/parse-string @(s/take! ws) true))

(defn- push-event! [ws event]
  (s/put! ws (json/generate-string event)))

(defn- channel-id [{:keys [channels]} chan-name]
  (when-let [channel (some #(if (= (:name %) chan-name) %) channels)]
    (:id channel)))

(defn- connect-rt-websocket [url]
  (let [ws-conn @(http/websocket-client url)
        msg (take-event! ws-conn)]
    (if (= (:type msg) "hello")
      ws-conn
      (throw
        (Exception. (str "Did not receive initial hello message on websocket connect, got: " msg))))))

(defn- create-connection [{:keys [self team users channels bots groups ims url]}]
  (RtmConnection. (connect-rt-websocket url) (atom 1) self team users channels bots groups ims))

(defn- get-and-inc-id [{:keys [next-id]}]
  (let [id @next-id]
    (swap! next-id inc)
    id))

(defn connect [token]
  (let [connect-response (api/request-rtm-start token)]
    (create-connection connect-response)))

(defn send-msg [rtm-conn channel msg]
  (when-let [chan-id (channel-id rtm-conn channel)]
    (push-event! (:ws rtm-conn) {:id (get-and-inc-id rtm-conn)
                                 :type "message"
                                 :channel chan-id
                                 :text msg})))