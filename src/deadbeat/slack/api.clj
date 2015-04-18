(ns deadbeat.slack.api
  (:require
    [clj-http.client :as client]
    [cheshire.core :as json]))

(def rt-start-endpoint "https://slack.com/api/rtm.start")

(defn request-rtm-start [token]
  (let [res (client/get
              rt-start-endpoint
              {:query-params {:token token}})]
    (if (= (:status res) 200)
      (json/parse-string (:body res) true)
      (throw
        (Exception. (str "Got non 200 status code in rtm start response: " res))))))