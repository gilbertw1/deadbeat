(ns deadbeat.slack.state)

(defrecord RtmState [next-id last-pong self team users channels bots groups ims])

(defn user-by-id [state id]
  (let [{:keys [users]} @state]
    (some #(if (= (:id %) id) %) users)))

(defn channel-by-id [state id]
  (let [{:keys [channels]} @state]
    (some #(if (= (:id %) id) %) channels)))

(defn channel-by-name [state name]
  (let [{:keys [channels]} @state]
    (some #(if (= (:name %) name) %) channels)))

(defn inc-next-id [state]
  (let [new-id (inc (:next-id state))]
    (conj state [:next-id new-id])))

(defn next-id-and-inc [state]
  (:next-id (swap! state inc-next-id)))