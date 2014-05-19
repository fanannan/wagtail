(ns wagtail.performance
  (:require [clatrix.core :as cl]
            [wagtail.shared :as shared]
            [wagtail.reader.data-handler :as data-handler]))


(defn result-checker
  [[feature prediction] label]
  [(= prediction label) prediction label feature])

(defn run
  "Returns the results and the rate of corrects prediction."
  [features, labels, trained-variables, model-type]
  (let [results (map result-checker
                     (shared/run-model model-type trained-variables features) labels)
        corrects (count (filter first results))
        performance (float (/ corrects (count results)))]
    [results performance]))

(defn check-performance
  "Train and run a model with the specified model and data"
  [{:keys [model-type, learner, options]},
   {:keys [data-type] :as data}, verbose]
  (let [[[train-features, train-labels]
         [test-features, test-labels]] (data-handler/prepare-data data)
        trained-variables (learner options train-features train-labels)
        [train-r train-p] (run train-features train-labels trained-variables model-type)
        [test-r test-p] (run test-features test-labels trained-variables model-type)]
    (when verbose
      (println "model: " model-type)
      (println "data:  " data-type)
      (println "train performance: " train-p)
      (println "test performance: " test-p "\n"))
    [[train-r train-p] [test-r test-p]]))
