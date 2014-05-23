(ns wagtail.sample
  (:require [wagtail.shared :as shared]
            [wagtail.performance :as performance]
            [wagtail.classifier.perceptron :as perceptron]
            [wagtail.classifier.linear-regression :as linear-regression]
            [wagtail.classifier.logistic-regression :as logistic-regression]
            [wagtail.classifier.pa :as pa]
            [wagtail.classifier.cw :as cw]
            [wagtail.classifier.scw :as scw]
            [wagtail.classifier.pegasos-svm :as pegasos-svm]
            [wagtail.reader.iris :as iris]
            [wagtail.reader.digits :as digits]
            [wagtail.reader.death-rates :as death-rates]))


;; sample definitions for classifier parameters

; margin perceptron
(def perceptron
  {:model-name "Perceptron"
   :config perceptron/perceptron-config,
   :variables {:threshold 0.2, :learning-rate 0.01, :iterations 100}})

; linear regression
(def linear-reg
  {:model-name "Linear-Regression",
   :config linear-regression/linear-regression-config,
   :variables {:learning-rate 0.1, :iterations 10000}})

; logistic regression
(def logistic-reg
  {:model-name "Logistic-Regression",
   :config logistic-regression/logistic-regression-config,
   :variables {:learning-rate 0.000000005, :iterations 500}})

; passive-agressive
(def pa
  {:model-name "PA"
   :config pa/pa-classifier-config,
   :variables {:iterations 1}})

(def pa1
  {:model-name "PA-I",
   :config pa/pa1-classifier-config,
   :variables {:c 0.8, :iterations 1}})

(def pa2
  {:model-name "PA-II",
   :config pa/pa2-classifier-config,
   :variables {:c 0.8, :iterations 1}})

(def pa-reg
  {:model-name "PA-Regression",
   :config pa/pa-regression-config,
   :variables {:epsiron 0.05, , :iterations 1}})

; confidence weighted
(def cw
  {:model-name "CW",
   :config cw/cw-config,
   :variables {:r 0.2, :iterations 1}})

; soft confidence weighted
(def scw1
  {:model-name "SCW-I",
   :config scw/scw1-config,
   :variables {:c 1.0, :eta 0.9, :iterations 1}})

; pegasos svm
(def pegasos-svm
  {:model-name "Pegasos-I",
   :config pegasos-svm/pegasos-svm-config,
   :variables {:lambda 0.1, :iterations 1}})


;; sample data

; Iris data set (always shuffled)
; http://archive.ics.uci.edu/ml/datasets/Iris
(def iris-data
  {:data-type :iris,
   :records iris/data,
   :train-ratio 0.6
   :target :versicolor})

; MNIST digit data set
; https://github.com/IshitaTakeshi/Hackathon/tree/master/MLAkiba2/Code
(def digits-data
  {:data-type :digits,
   :records digits/data})

; Death rate
; https://orion.math.iastate.edu/burkardt/data/regression/x28.txt
(def death-rate-data
  {:data-type :death-rates,
   :records death-rates/data,
   :train-ratio 0.6})

(def scaled-death-rate-data
  (into death-rate-data
        {:scale {:method :simple :limit 0.98}}))


;; run samples

(defn run-samples []
  (doall
   (do
     #_(for [model [perceptron pa pa1 pa2 cw scw1 pegasos-svm]
           data [iris-data digits-data]]
       (performance/check-performance model data true))
     (for [model [linear-reg];, logistic-reg]; [pa-reg]
           data [death-rate-data, scaled-death-rate-data]]
       (performance/check-performance model data true)))))
