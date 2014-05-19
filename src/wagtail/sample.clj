(ns wagtail.sample
  (:require [wagtail.shared :as shared]
            [wagtail.performance :as performance]
            [wagtail.classifier.perceptron :as perceptron]
            [wagtail.classifier.pa :as pa]
            [wagtail.classifier.cw :as cw]
            [wagtail.classifier.scw :as scw]
            [wagtail.reader.iris :as iris]
            [wagtail.reader.digits :as digits]))


;; sample definitions for classifier parameters
; :model-type  model identifier,
; :learner     model learner, generated with a coniguration specification
; :options     model parameters

; margin perceptron
(def perceptron {:model-type :Perceptron,
                 :learner (shared/make-learner perceptron/perceptron-config),
                 :options {:threshold 0.2, :learning-rate 0.01, :iterations 100}})

; passive agressive
(def pa {:model-type :PA,
         :learner (shared/make-learner pa/pa-config),
         :options {:iterations 1}})

(def pa1 {:model-type :PA-I,
         :learner (shared/make-learner pa/pa1-config),
         :options {:c 0.8, :iterations 1}})

(def pa2 {:model-type :PA-II,
         :learner (shared/make-learner pa/pa2-config),
         :options {:c 0.8, :iterations 1}})

; confidence weighted
(def cw {:model-type :CW,
         :learner (shared/make-learner cw/cw-config),
         :options {:r 0.2, :iterations 1}})

; soft confidence weighted
(def scw {:model-type :SCW-I,
          :learner (shared/make-learner scw/scw-config),
          :options {:c 1.0, :eta 0.9, :iterations 1}})


;; sample data

; Iris data set (always shuffled)
; http://archive.ics.uci.edu/ml/datasets/Iris
(def iris-data {:data-type :iris, :records iris/iris, :train-ratio 0.75 :target :versicolor})

; MNIST digit data set
; https://github.com/IshitaTakeshi/Hackathon/tree/master/MLAkiba2/Code
(def digits-data  {:data-type :digits, :records digits/digits})


;; run samples

(defn run-samples []
  (doall
   (for [model [perceptron pa pa1 pa2 cw scw]
         data [iris-data digits-data]]
     (performance/check-performance model data true))))
