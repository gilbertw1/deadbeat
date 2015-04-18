(ns deadbeat.core
  (:require
    [clj-http.client :as client]
    [deadbeat.slack.api :as api]
    [deadbeat.slack.rtm :as rtm]))

(def hardcode-token "...")

(defn -main [& args]
  (let [rtm-connection (rtm/connect hardcode-token)]
    (rtm/send-msg rtm-connection "general" "shut up sweet booby")))