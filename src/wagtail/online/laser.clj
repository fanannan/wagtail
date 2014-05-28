(ns wagtail.online.laser
  (:require [clatrix.core :as cl]
            [wagtail.shared :as shared]))


;; LASER
;; based on "A Last-Step Regression Algorithm for Non-Stationary Online Learning" by E. Moroshko and K. Crammer, and "Second-Order Non-Stationary Online Learning for Regression" by N. Vaits, E. Moroshko and K. Crammer.

(defn divider [sigma2 feature]
  (+ 1.0 (get (cl/* (cl/t feature) sigma2 feature) 0 0)))

(defn predict
  [config,
   {:keys [w, sigma, icI] :as variables},
   feature]
  (let [sigma2 (cl/+ sigma icI)]
    (/ (shared/estimate w feature) (divider sigma2 feature))))

(defn calc-next-w
  [w, sigma2, feature, label]
  (cl/+ w
        (cl/* (/ 1.0 (divider sigma2 feature))
              (cl/* (- label (shared/estimate w feature)) sigma2 feature))))

(defn calc-next-sigma
  [sigma2, feature]
  (cl/i (cl/+ (cl/i sigma2) (cl/* feature (cl/t feature)))))

(defn laser-updater
  [config,
   {:keys [w, sigma, icI] :as variables},
   feature, label]
  (let [sigma2 (cl/+ sigma icI)]
    (into variables
          {:w (calc-next-w w sigma2 feature label)
           :sigma (calc-next-sigma sigma2 feature)})))

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
