(ns wagtail.core
  (:require [wagtail.sample :as sample]))

(defn -main []
  (sample/run-samples))
