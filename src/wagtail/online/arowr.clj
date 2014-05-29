(ns wagtail.online.arowr
  (:require [clatrix.core :as cl]
            [wagtail.shared :as shared]
            [wagtail.online.cw :as cw]))


;; AROWR
;; based on "Second-Order Non-Stationary Online Learning for Regression" by N. Vaits, E. Moroshko and K. Crammer.

(defn calc-next-w
  [w, sigma, r, feature, label]
  (cl/+ w
        (cl/* (/ 1.0 (+ r (get (cl/* (cl/t feature) sigma feature) 0 0)))
              (cl/* (- label (shared/estimate w feature)) sigma feature))))

(defn calc-next-sigma
  [sigma, r1, r2, feature]
  (cl/i (cl/+ (cl/* r1 (cl/i sigma)) (cl/* r2 feature (cl/t feature)))))

(defn arowr-updater
  [config,
   {:keys [w, sigma, r] :as variables},
   feature, label]
  (into variables
        {:w (calc-next-w w sigma r feature label)
         :sigma (calc-next-sigma sigma 1 (/ 1.0 r) feature)}))

(def arowr-config
  {:model-type :regression
   :validator-fn (fn [{:keys [r, iterations]}]
                   (and (> r 0.0)(> iterations 0))),
   :initializer-fn cw/cw-initialzer,
   :updater-fn arowr-updater,
   :weight-name :w})
