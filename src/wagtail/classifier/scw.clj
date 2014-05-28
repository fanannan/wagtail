(ns wagtail.classifier.scw
  (:require [clatrix.core :as cl]
            [wagtail.shared :as shared]
            [wagtail.math :as wmath]
            [wagtail.classifier.cw :as cw]))


;; soft confidence weighted learning
;; based on "Exact Soft Confidence-Weighted Learning" by J Wang, P Zhao and S C H Hoi, 2012.

(defn calc-phi [eta]
  (wmath/probit eta))

(defn calc-psi [eta]
  (let [p (calc-phi eta)]
    (+ 1.0 (* p p 0.5))))

(defn calc-zeta [eta]
  (let [p (calc-phi eta)]
    (+ 1.0 (* p p))))

(defn calc-alpha-I [mu, sigma, phi, psi, zeta, c, feature, label]
  (let [v (cw/confidence sigma feature)
        m (shared/margin mu feature label)
        j (* m m phi phi phi phi 0.25)
        k (* v phi phi zeta)
        t (/ (- (Math/sqrt (+ j k))(* m psi))
             (* v zeta))]
    (min c (max 0.0 t))))

(defn calc-beta [mu, sigma, alpha, phi, psi, zeta, feature, label]
  (let [v (cw/confidence sigma feature)
        m (shared/margin mu feature label)
        j (* -1 alpha v phi)
        k (Math/sqrt (+ (* (* alpha v phi)(* alpha v phi)) (* 4 v)))
        u (* (+ j k)(+ j k) 0.25)]
    (/ (* alpha phi)
       (+ (Math/sqrt u)(* v alpha phi)))))

(defn calc-loss [config, {:keys [mu, sigma, phi] :as variables}, feature, label]
  ; max(0, φ√(X"ΣX)-YtWtXt)
  (max 0.0
       (- (* phi (Math/sqrt (cw/confidence sigma feature)))
          (shared/margin mu feature label))))

(defn scw-updater
  [{:keys [alpha-fn] :as config},
   {:keys [mu, sigma, phi, psi, zeta, c] :as variables},
   feature, label]
  (let [alpha (alpha-fn mu sigma phi psi zeta c feature label)
        beta (calc-beta mu sigma alpha phi psi zeta feature label)]
    (into variables
          {:mu (cw/calc-next-mu mu sigma alpha feature label)
           :sigma (cw/calc-next-sigma mu sigma beta feature label)})))

(defn scw-initialzer
  [config, {:keys [c, eta] :as variables}, num-fields]
  (into variables
        {:mu (cl/zeros num-fields),
         :sigma (cl/eye num-fields),
         :phi (calc-phi eta),
         :psi (calc-psi eta),
         :zeta (calc-zeta eta)}))

(def scw1-config
  {:model-type :classification
   :validator-fn (fn [{:keys [c, eta, iterations]}]
                   (and (> c 0.0)(> 1.0 eta 0.5)(> iterations 0))),
   :initializer-fn scw-initialzer,
   :updater-fn scw-updater,
   :alpha-fn calc-alpha-I,
   :loss-fn calc-loss,
   :weight-name :mu})
