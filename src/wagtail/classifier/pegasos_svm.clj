(ns wagtail.classifier.pegasos-svm
  (:require [clatrix.core :as cl]
            [wagtail.shared :as shared]))


;; pegasos svm
; based on the code by Shai Shalev-Shwartz presebted at http://bickson.blogspot.jp/2012/04/more-on-large-scale-svm.html
; for detail, "Pegasos: Primal Estimated sub GrAdient SOlver for SVM", Shai Shalev-Shwartz, Y Singer N Srebro 2007.

(defn calc-loss [config, {:keys [w] :as variables}, feature, label]
  (- 1.0 (shared/margin w feature label)))

(defn pegasos-svm-updater
  [config,
   {:keys [w, lambda, t] :as variables},
   feature, label]
  (into variables
        {:w (cl/+ (cl/* (- 1.0 (/ 1.0 t)) w) (cl/* (/ 1.0 (* lambda t)) label feature)),
         :t (inc t)}))

(defn pegasos-svm-unupdater
  [config,
   {:keys [w, t] :as variables},
   feature, label]
  (into variables
        {:w (cl/* (- 1.0 (/ 1.0 t)) w),
         :t (inc t)}))

(def pegasos-svm-config
  {:model-type :classification,
   :validator-fn (fn[{:keys [lambda, iterations]}]
                   (and (> 1.0 lambda 0.0)
                        (> iterations 0))),
   :initializer-fn shared/weight-t-initialzer,
   :updater-fn pegasos-svm-updater,
   :unupdater-fn pegasos-svm-unupdater,
   :loss-fn calc-loss,
   :weight-name :w})
