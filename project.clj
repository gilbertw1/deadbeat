(defproject deadbeat "0.1.0-SNAPSHOT"
  :description "A clojure library that interacts with the slack real time messaging api "
  :url "http://bryangilbert.com"
  :main deadbeat.core
  :license {:name "MIT"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [aleph "0.4.0-beta3"]
                 [clj-http "1.1.0"]
                 [cheshire "5.4.0"]])