(ns wagtail.classifier.cw
  (:require [clatrix.core :as cl]
            [wagtail.shared :as shared]))


;; confidence weighted learning
;; base on "Confidence-Weighted Linear Classification" by M Dredze, Koby Crammmer and F Pereira, 2008

(defn confidence [sigma, feature] ;?_?
  ; |WtXt|
  (cl/get (cl/* (cl/t feature) sigma feature) 0 0))

(defn calc-alpha [mu, sigma, beta, feature, label]
  (let [t (- 1.0 (cl/get (cl/* label (cl/t feature) mu) 0 0))]
    (* (max 0.0 t) beta)))

(defn calc-beta [sigma, r, feature]
  (/ 1.0 (+ (confidence sigma feature) r)))

(defn calc-next-mu [mu, sigma, alpha, feature, label]
  ; μ＝μt+αtYtΣtXt
  (cl/+ mu (cl/* alpha label sigma feature)))

(defn calc-next-sigma [mu, sigma, beta, feature, label]
  ; Σ＝Σt-βtΣtXtXt"Σt
  (cl/- sigma (cl/* beta sigma feature (cl/t feature) sigma)))

(defn calc-loss [config, {:keys [mu] :as variables}, feature, label]
  (if (pos? (shared/margin mu feature label)) 0 1))

(defn cw-updater
  [config,
   {:keys [mu, sigma, r] :as variables},
   feature, label]
  (let [beta (calc-beta sigma r feature)
        alpha (calc-alpha mu sigma beta feature label)]
    (into variables
          {:mu (calc-next-mu mu sigma alpha feature label)
           :sigma (calc-next-sigma mu sigma beta feature label)})))

(defn cw-initialzer
  [config, variables, num-fields]
  (into variables
        {:mu (cl/zeros num-fields),
         :sigma (cl/eye num-fields)}))

(def cw-config
  {:model-type :classification,
   :validator-fn (fn[{:keys [r]}] (> r 0.0)),
   :initializer-fn cw-initialzer,
   :updater-fn cw-updater,
   :loss-fn calc-loss,
   :weight-name :mu})
