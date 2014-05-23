(ns wagtail.performance
  (:require [clatrix.core :as cl]
            [wagtail.shared :as shared]
            [wagtail.data-handler :as data-handler]
            [wagtail.math :as wmath]))


(defn result-checker
  [[feature prediction] label]
  [(= prediction label) prediction label feature])

(defn run
  "Returns the results and the rate of corrects prediction."
  [config, scale, label-stat, trained-variables, features, labels]
  (let [results (map result-checker
                     (shared/run-model config scale label-stat trained-variables features) labels)
        predictions (map second results)]
    [results
     {:performance (float (/ (count (filter first results)) (count results)))
      :correlation (wmath/correl predictions labels)
      :mse (wmath/mse predictions labels)}]))

(defn check-performance
  "Train and run a model with the specified model and data"
  [{:keys [model-name, config, variables]},
   {:keys [data-type, scale] :as data}, verbose]
  (let [[[train-features, train-labels]
         [test-features, test-labels]
         stat] (data-handler/prepare-data config data)
        [feature-stat label-stat] stat
        learner (shared/make-learner config)
        trained-variables (learner variables train-features train-labels)
        [train-r train-p] (run config scale label-stat trained-variables train-features train-labels)
        [test-r test-p] (run config scale label-stat trained-variables test-features test-labels)]
    (when verbose
      (println "model: " (:model-type config) model-name)
      (println "data:  " data-type)
      (when-not (nil? scale) (println "scale: " scale))
      (case (:model-type config)
        :classification
        (do
          (println "train performance: " (:performance train-p))
          (println "test  performance: " (:performance test-p) "\n"))
        :regression
        (do
          (println "train correlation: " (:correlation train-p))
          (println "test  correlation: " (:corelation test-p) "\n")
          (println "train mse:         " (:mse train-p))
          (println "test  mse:         " (:mse test-p) "\n")))
      #_(println "train results:" train-r)
      (println "test results: " test-r)
      #_(println "weights: " trained-variables)
      )
    [[train-r train-p] [test-r test-p]]))
