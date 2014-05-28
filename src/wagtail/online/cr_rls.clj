(ns wagtail.online.cr-rls
  (:require [clatrix.core :as cl]
            [wagtail.shared :as shared]))


;; CR-RLS
;; based on "Second-Order Non-Stationary Online Learning for Regression" by N. Vaits, E. Moroshko and K. Crammer.

(defn calc-loss [config, {:keys [mu, sigma] :as variables}, feature, label]
  (- 1.0 (* (shared/margin mu feature label) label)))

(defn cr-rls-updater
  [config,
   {:keys [mu, sigma, r] :as variables},
   feature, label]
  (let [beta (cw/calc-beta sigma r feature)
        alpha (cw/calc-alpha mu sigma beta feature label)]
    (into variables
          {:mu (cw/calc-next-mu mu sigma alpha feature label)
           :sigma (cw/calc-next-sigma mu sigma beta feature label)})))

(def cr-rls-config
  {:model-type :classification
   :validator-fn (fn [{:keys [r, T, iterations]}]
                   (and (> 1.0 r 0.0)(integer? T)(> iterations 0))),
   :initializer-fn cr-rls-initialzer,
   :updater-fn cr-rls-updater,
   :loss-fn calc-loss,
   :weight-name :mu})
