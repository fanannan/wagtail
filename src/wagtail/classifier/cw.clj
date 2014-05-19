(ns wagtail.classifier.cw
  (:require [clatrix.core :as cl]
            [wagtail.shared :as shared]
            [wagtail.math :as wm]
            [wagtail.classifier.scw :as scw]))


;; confidence weighted learning
;; base on "Confidence-Weighted Linear Classification" by M Dredze, Koby Crammmer and F Pereira, 2008

(defn calc-alpha [mu, sigma, beta, feature, label]
  (let [t (- 1.0 (cl/get (cl/* label (cl/t feature) mu) 0 0))]
    (* (max 0.0 t) beta)))

(defn calc-beta [sigma, r, feature]
  (/ 1.0 (+ (scw/confidence sigma feature) r)))

(defn calc-loss [config, {:keys [mu] :as variables}, feature, label]
  (if (pos? (shared/margin mu feature label)) 0 1))

(defn cw-updater
  [{:keys [loss-fn] :as config},
   {:keys [mu, sigma, r] :as variables},
   feature, label]
  (let [beta (calc-beta sigma r feature)
        alpha (calc-alpha mu sigma beta feature label)]
    (into variables
          {:mu (scw/calc-next-mu mu sigma alpha feature label)
           :sigma (scw/calc-next-sigma mu sigma beta feature label)})))

(defn cw-initialzer
  [config, variables, num-fields]
  (into variables
        {:mu (cl/zeros num-fields),
         :sigma (cl/eye num-fields)}))

(def cw-config
  {:validator-fn (fn[{:keys [r]}] (> r 0.0))
   :initializer-fn cw-initialzer
   :updater-fn cw-updater
   :loss-fn calc-loss})
