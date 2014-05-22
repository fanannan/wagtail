(ns wagtail.classifier.perceptron
  (:require [clatrix.core :as cl]
            [wagtail.shared :as shared]))


;; margin perceptron

(defn calc-loss [config, {:keys [w, threshold] :as variables}, feature, label]
  (- threshold (shared/margin w feature label)))

(defn perceptron-updater
  [config,
   {:keys [w, learning-rate] :as variables},
   feature, label]
  (into variables
        {:w (cl/+ w (cl/* learning-rate label feature))}))

(def perceptron-config
  {:model-type :classification
   :validator-fn (fn[{:keys [threshold, learning-rate, iterations]}]
                   (and (> 1.0 threshold 0.0)
                        (> 1.0 learning-rate 0.0)
                        (> iterations 0))),
   :initializer-fn shared/weight-initialzer,
   :updater-fn perceptron-updater,
   :loss-fn calc-loss,
   :weight-name :w})
