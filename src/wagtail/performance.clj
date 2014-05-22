(ns wagtail.performance
  (:require [clatrix.core :as cl]
            [wagtail.shared :as shared]
            [wagtail.reader.data-handler :as data-handler]))


(defn result-checker
  [[feature prediction] label]
  [(= prediction label) prediction label feature])

(defn run
  "Returns the results and the rate of corrects prediction."
  [config, trained-variables, features, labels]
  (let [results (map result-checker
                     (shared/run-model config trained-variables features) labels)
        corrects (count (filter first results))
        performance (float (/ corrects (count results)))]
    [results performance]))

(defn check-performance
  "Train and run a model with the specified model and data"
  [{:keys [model-name, config, variables]},
   {:keys [data-type] :as data}, verbose]
  (let [[[train-features, train-labels]
         [test-features, test-labels]] (data-handler/prepare-data config data)
        learner (shared/make-learner config)
        trained-variables (learner variables train-features train-labels)
        [train-r train-p] (run config trained-variables train-features train-labels)
        [test-r test-p] (run config trained-variables test-features test-labels)]
    (when verbose
      (println "model: " (:model-type config) model-name)
      (println "data:  " data-type)
      (println "train performance: " train-p)
      (println "test performance: " test-p "\n")
      (println "train results:" train-r)
      (println "test results: " test-r)
      (println "weights: " trained-variables)
      )
    [[train-r train-p] [test-r test-p]]))
