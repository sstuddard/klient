(ns klient.core
  (:require [clj-http.client :as http]
            [cheshire.core :as json]))

(defn- seqify
  [x]
  (if (sequential? x)
    x
    (list x)))

(defn- post-events
  [url event-collection error-fn events]
  (try
    (let [data (->> events 
                    seqify 
                    (hash-map event-collection)
                    json/generate-string)]
      (http/post url 
                 {:content-type :json 
                  :accept :json
                  :body data}))
    (catch Exception e (when error-fn (error-fn events e)))))

(defn- build-url
  [project-id api-key]
  (str "https://api.keen.io/3.0/projects/" project-id "/events?api_key=" api-key))

;; Buffered client support
(defn- start-scheduler
  [frequency scheduled-fn]
  (letfn [(worker [] 
            (while true
              (do
                (Thread/sleep frequency) 
                (try (scheduled-fn) (catch Exception e)))))]
    (doto (Thread. #(worker))
      (.setDaemon true)
      .start)))

(defn- build-buffered-client
  "Builds a buffered client, enclosing all relevant data and 
   helper functions in a closure so they are available."
  [url event-collection error-fn max-ms max-size]
  (letfn [(enqueue [queue events]
            (let [new-queue (->> events 
                                 seqify 
                                 (reduce conj queue))]
              (if (> (count new-queue) max-size)
                (flush-queue new-queue)
                new-queue)))
          (flush-queue [queue]
            (let [all-events (into [] queue)]
              (when-not (empty? all-events) 
                (post-events url event-collection error-fn all-events))
              (clojure.lang.PersistentQueue/EMPTY)))]
    (let [client-agent (agent (clojure.lang.PersistentQueue/EMPTY))]
      (start-scheduler max-ms #(send client-agent flush-queue))
      (fn [events]
        (send client-agent enqueue events)))))
  
;; Public API
(defn create-client
  "Returns a function that can be used to submit events (single or collection),
   optionally taking an error function to be called."
  [project-id event-collection api-key & {:keys [error-fn]
                                          :or {error-fn nil}}]
  (let [url (build-url project-id api-key)]
    (fn [events]
      (future (post-events url event-collection error-fn events)))))

(defn create-buffered-client
  "Returns a function that can be used to submit events (single or collection),
   optionally taking an error function to be called. Internally the events are batched
   per period and/or by volume."
  [project-id event-collection api-key & {:keys [max-ms max-size error-fn]
                                          :or {max-ms   1000
                                               max-size 1000
                                               error-fn nil}}]
  (let [url (build-url project-id api-key)]
    (build-buffered-client url event-collection error-fn max-ms max-size)))
 
