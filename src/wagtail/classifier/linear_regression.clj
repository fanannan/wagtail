(ns wagtail.classifier.linear-regression
  (:require [clatrix.core :as cl]
            [wagtail.shared :as shared]
            [wagtail.math :as wmath]))


;; linear regression

(defn error [config, {:keys [w] :as variables}, feature, label]
  (- label (shared/estimate w feature)))

(defn linear-regression-updater
  [{:keys [error-fn] :as config},
   {:keys [learning-rate, w, t] :as variables},
   feature, label]
  (let [error (error-fn config variables feature label)]
    (into variables
          {:w (cl/+ w (cl/* learning-rate (/ 1.0 t) error feature))
           :t (inc t)
           ;:learning-rate (* learning-rate 0.999999)
           })))

(def linear-regression-config
  {:model-type :regression
   :validator-fn (fn[{:keys [learning-rate, iterations]}]
                   (and (> 1.0 learning-rate 0.0)
                        (> iterations 0))),
   :initializer-fn shared/weight-t-initialzer,
   :error-fn error,
   :updater-fn linear-regression-updater,
   :weight-name :w
   :bias true})
