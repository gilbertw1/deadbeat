(ns deadbeat.core
  (:require
    [clj-http.client :as client]
    [deadbeat.slack.api :as api]
    [deadbeat.slack.rtm :as rtm]
    [clojure.core.async :as a :refer [go chan <! >!]]))

(def hardcode-token "...")

(defn print-message [{:keys [user text]}]
  (println (:name user) ": " text))

(defn -main [& args]
  (let [rtm-connection (rtm/connect hardcode-token)]
    (println "Connected to RTM!")
    (rtm/on-message rtm-connection "general" print-message)
    (println (read-line))))