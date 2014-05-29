(ns wagtail.online.laser
  (:require [clatrix.core :as cl]
            [wagtail.shared :as shared]
            [wagtail.online.arowr :as arowr]
            [wagtail.online.aar :as aar]))


;; LASER
;; based on "A Last-Step Regression Algorithm for Non-Stationary Online Learning" by E. Moroshko and K. Crammer, and "Second-Order Non-Stationary Online Learning for Regression" by N. Vaits, E. Moroshko and K. Crammer.

(defn predict
  [config,
   {:keys [w, sigma, icI] :as variables},
   feature]
  (let [sigma2 (cl/+ sigma icI)]
    (aar/predict config {:w w, :sigma sigma2} feature)))

(defn laser-updater
  [config,
   {:keys [w, sigma, icI] :as variables},
   feature, label]
  (let [sigma2 (cl/+ sigma icI)]
    (into variables
          {:w (arowr/calc-next-w w sigma2 1 feature label)
           :sigma (arowr/calc-next-sigma sigma2 1 1 feature)})))

(defn laser-initialzer
  [config, {:keys [b c] :as variables}, num-fields]
  (into (shared/weight-initialzer config variables num-fields)
        {:sigma (cl/* (/ (- c b) (* b c))(cl/eye num-fields))
         :icI (cl/* (/ 1.0 c) (cl/eye num-fields))}))

(def laser-config
  {:model-type :regression
   :validator-fn (fn [{:keys [b, c, iterations]}]
                   (and (> c b 0.0)(> iterations 0))),
   :initializer-fn laser-initialzer,
   :updater-fn laser-updater,
   :weight-name :w,
   :predict-fn predict})
