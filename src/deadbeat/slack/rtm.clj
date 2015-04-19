(ns deadbeat.slack.rtm
  (:require
    [deadbeat.slack.api :as api]
    [deadbeat.slack.events :as events]
    [deadbeat.slack.state :as st]
    [manifold.stream :as s]
    [clojure.core.async :as a :refer [go chan <! >!]]
    [manifold.deferred :as d]
    [cheshire.core :as json]
    [aleph.http :as http]
    [overtone.at-at :as at])
  (:import [deadbeat.slack.state RtmState]))

(defrecord RtmConnection [ws pool event-chan state])

(defn- take-event! [ws]
  (json/parse-string @(s/take! ws) true))

(defn- push-event! [ws event]
  (s/put! ws (json/generate-string event)))

(defn- connect-rt-websocket [url]
  (let [ws @(http/websocket-client url)
        msg (take-event! ws)]
    (if (= (:type msg) "hello")
      ws
      (throw
        (Exception. (str "Did not receive initial hello message on websocket connect, got: " msg))))))

(defn- event-chan [ws state-atom]
  (let [c (chan)]
    (s/connect ws c)
    (a/map< #(events/convert-event state-atom (json/parse-string % true)) c)))

(defn- create-connection [{:keys [self team users channels bots groups ims url]}]
  (let [ws (connect-rt-websocket url)
        state-atom (atom (RtmState. 1 (System/currentTimeMillis) self team users channels bots groups ims))
        event-chan (event-chan ws state-atom)]
    (RtmConnection. ws (at/mk-pool) event-chan state-atom)))

(defn- send-ping [rtmc]
  (push-event! rtmc {:id (st/next-id-and-inc (:state rtmc)) :type "ping"}))

(defn- schedule-pings [rtmc]
  (at/every 1000 #(send-ping rtmc) (:pool rtmc)))

(defn connect [token]
  (let [connect-response (api/request-rtm-start token)
        connection (create-connection connect-response)]
    ;(schedule-pings connection)
    connection))

(defn send-message [rtmc channel msg]
  (when-let [chan-id (:id (st/channel-by-name (:state rtmc) channel))]
    (push-event! (:ws rtmc) {:id (st/next-id-and-inc (:state rtmc))
                                 :type "message"
                                 :channel chan-id
                                 :text msg})))

(defn message-chan [{:keys [event-chan state]} channel-name]
  (->> event-chan
       (a/filter< (fn [{:keys [type channel]}]
                    (and
                      (= type "message")
                      (= (:name channel) channel-name))))))

(defn on-message [rtmc channel-name fun]
  (let [chan (message-chan rtmc channel-name)]
    (go
      (while true
        (fun (<! chan))))))