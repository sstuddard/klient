(defproject klient "0.1.0-SNAPSHOT"
  :description "A minimal Keen IO client"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [clj-http "2.0.0"]
                 [cheshire "5.5.0"]]
  :repl-options {:init-ns klient.core})
