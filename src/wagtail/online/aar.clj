(ns wagtail.online.aar
  (:require [clatrix.core :as cl]
            [wagtail.shared :as shared]
            [wagtail.online.arowr :as arowr]))


;; AAR
;; based on "Second-Order Non-Stationary Online Learning for Regression" by N. Vaits, E. Moroshko and K. Crammer.

(defn divider [sigma feature]
  (+ 1.0 (get (cl/* (cl/t feature) sigma feature) 0 0)))

(defn predict
  [config,
   {:keys [w, sigma] :as variables},
   feature]
   (/ (shared/estimate w feature) (divider sigma feature)))

(defn aar-updater
  [config,
   {:keys [w, sigma] :as variables},
   feature, label]
  (into variables
        {:w (arowr/calc-next-w w sigma 1 feature label)
         :sigma (arowr/calc-next-sigma sigma 1 1 feature)}))

(defn aar-initialzer
  [config, {:keys [b] :as variables}, num-fields]
  (into (shared/weight-initialzer config variables num-fields)
        {:sigma (cl/* (/ 1.0 b) (cl/eye num-fields))}))

(def aar-config
  {:model-type :regression
   :validator-fn (fn [{:keys [b, iterations]}]
                   (and (> b 0.0)(> iterations 0))),
   :initializer-fn aar-initialzer,
   :updater-fn aar-updater,
   :weight-name :w,
   :predict-fn predict})
