(ns wagtail.classifier.logistic-regression
  (:require [clatrix.core :as cl]
            [wagtail.shared :as shared]
            [wagtail.math :as wmath]))


;; logistic regression

(defn sigmoid-error [config, {:keys [w] :as variables}, feature, label]
  (- label (wmath/sigmoid (shared/estimate w feature))))

(defn logistic-regression-updater
  [{:keys [error-fn] :as config},
   {:keys [w, learning-rate] :as variables},
   feature, label]
  (let [error (error-fn config variables feature label)]
    (into variables
          {:w (cl/+ w (cl/* learning-rate error feature))})))

(def logistic-regression-config
  {:model-type :regression
   :validator-fn (fn[{:keys [learning-rate, iterations]}]
                   (and (> 1.0 learning-rate 0.0)
                        (> iterations 0))),
   :initializer-fn shared/weight-initialzer,
   :error-fn sigmoid-error,
   :updater-fn logistic-regression-updater,
   :weight-name :w})
