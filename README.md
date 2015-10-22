# klient - A simple Keen IO Clojure client

This is a simple experimental client for publishing events to Keen IO. 

## Usage

There are two kinds of clients, and both accept either a single event or a list of events. A client is tied to an individual event collection. You may create multiple clients for different event collections.

### Single Request Client

This client will make a single HTTP call per publish call. This should be ok for relatively low volume events, or if your application is buffering events itself. You can create the client like so:

```clojure
;; create a basic client, errors are dropped
(create-client "my-project-id" "event-coll1" "<api-key>")

;; You can also specify a function to handle errors
(create-client "my-project-id"
               "event-coll1"
               "<api-key>"
               :error-fn (fn [events exception] (println events exception)))
```

### Buffered Client

This client will buffer events based on a time period and/or a max number of events. The client is created like so:

```clojure
;; create a basic buffered client, errors are dropped
;; default time window is 1 second
;; default max events is 1000
(create-buffered-client "my-project-id" "event-coll1" "<api-key>")

;; You can also specify a function to handle errors, time window of 100ms,
;; and 200 event threshold
(create-buffered-client "my-project-id"
                        "event-coll1"
                        "<api-key>"
                        :error-fn (fn [events exception] (println events exception))
                        :max-ms 100
                        :max-size 200)
```

### Publishing events

The result of client creation is a single arity function regardless of the type of client you create.

```clojure
(def client (create-client "my-project-id" "event-coll1" "<api-key>"))

;; publish a single event
(client {:id 1 :name "jeff" :action "click"})

;; publich a list of events
(client [{:id 1 :name "jeff" :action "click"}
         {:id 2 :name "alan" :action "back"}])

## License

Copyright © 2015 Shayne Studdard

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
