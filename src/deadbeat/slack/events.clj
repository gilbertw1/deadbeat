(ns deadbeat.slack.events
  (:require
    [deadbeat.slack.state :as st]))

(defrecord HelloEvent [type])
(defrecord MessageEvent [type channel user text ts])
(defrecord PresenceEvent [type user presence])
(defrecord UserTypingEvent [type channel user])
(defrecord UserChangeEvent [type user])

(defn- convert-hello-event [state e]
  (HelloEvent. "hello"))

(defn- convert-message-event [state {:keys [channel user text ts]}]
  (MessageEvent. "message" (st/channel-by-id state channel) (st/user-by-id state user) text ts))

(defn- convert-presence-change-event [state {:keys [user presence]}]
  (PresenceEvent. "presence_change" (st/user-by-id state user) presence))

(defn- convert-user-typing-event [state {:keys [channel user]}]
  (UserTypingEvent. "user_typing" (st/channel-by-id state channel) (st/user-by-id state user)))

(defn- convert-user-change-event [state {:keys [user]}]
  (UserChangeEvent. "user_change" user))

(defn convert-event [state e]
  (condp = (:type e)
    "hello" (convert-hello-event state e)
    "message" (convert-message-event state e)
    "presence_change" (convert-presence-change-event state e)
    "user_typing" (convert-user-typing-event state e)
    "user_change" (convert-user-change-event state e)
    nil))