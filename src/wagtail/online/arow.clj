(ns wagtail.online.arow
  (:require [clatrix.core :as cl]
            [wagtail.shared :as shared]
            [wagtail.math :as wmath]
            [wagtail.online.cw :as cw]))


;; AROW
;; based on "Adaptive Regularization of Weight Vectors" by K. Crammer, Alex Kulesza and Mark Dredze.

(defn calc-loss [config, {:keys [mu, sigma] :as variables}, feature, label]
  (- 1.0 (* (shared/margin mu feature label) label)))

(defn arow-updater
  [config,
   {:keys [mu, sigma, r] :as variables},
   feature, label]
  (let [beta (cw/calc-beta sigma r feature)
        alpha (cw/calc-alpha mu sigma beta feature label)]
    (into variables
          {:mu (cw/calc-next-mu mu sigma alpha feature label)
           :sigma (cw/calc-next-sigma mu sigma beta feature label)})))

(def arow-config
  {:model-type :classification
   :validator-fn (fn [{:keys [r, iterations]}]
                   (and (> r 0.0)(> iterations 0))),
   :initializer-fn cw/cw-initialzer,
   :updater-fn arow-updater,
   :loss-fn calc-loss,
   :weight-name :mu})
