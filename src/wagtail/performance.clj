(ns wagtail.performance
  (:require [clatrix.core :as cl]
            [wagtail.shared :as shared]
            [wagtail.data-handler :as data-handler]
            [wagtail.math :as wmath]))


(defn result-checker
  [[feature prediction] scaled-label original-label]
  [(= prediction original-label) prediction scaled-label original-label feature])

(defn run
  "Returns the results and the rate of corrects prediction."
  [config, scale, label-stats, trained-variables, features, scaled-labels, original-labels]
  (let [results (map result-checker
                     (shared/run-model config scale label-stats trained-variables features)
                     scaled-labels
                     original-labels)
        predictions (map second results)]
    [results
     {:performance (float (/ (count (filter first results)) (count results)))
      :correlation (wmath/correl predictions scaled-labels)
      :mse (wmath/mse predictions scaled-labels)}]))

(defn check-performance
  "Train and run a model with the specified model and data"
  [{:keys [model-name, config, variables]},
   {:keys [data-type, scale] :as data}, verbose]
  (let [{:keys [train test stats]} (data-handler/prepare-data config data)
        train-features (:features train)
        test-features (:features test)
        train-labels (:labels train)
        test-labels (:labels test)
        {:keys [feature-stats label-stats]} stats
        learner (shared/make-learner config)
        trained-variables (learner variables train-features train-labels)
        [train-r train-p] (run config scale label-stats trained-variables train-features train-labels (map first (:original-labels train)))
        [test-r test-p] (run config scale label-stats trained-variables test-features test-labels (map first (:original-labels test)))]
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
          (println "test  correlation: " (:corelation test-p))
          (println "train mse:         " (:mse train-p))
          (println "test  mse:         " (:mse test-p) "\n")))
      #_(println "train results:" train-r)
      #_(println "test results: " test-r)
      #_(println "weights: " trained-variables)
      )
    [[train-r train-p] [test-r test-p]]))
