(defproject wagtail "0.1.1-SNAPSHOT"
  :description "Simple online classifiers"
  :url "https://github.com/fanannan/wagtail"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/data.csv "0.1.2"]
                 [clatrix "0.3.0"]
                 [org.apache.commons/commons-math3 "3.2"]
                 [incanter "1.5.5"]]
  :main wagtail.core)
