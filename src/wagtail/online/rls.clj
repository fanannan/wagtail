(ns wagtail.online.rls
  (:require [clatrix.core :as cl]
            [wagtail.shared :as shared]
            [wagtail.online.cw :as cw]
            [wagtail.online.arowr :as arowr]))


;; RLS
;; based on "Second-Order Non-Stationary Online Learning for Regression" by N. Vaits, E. Moroshko and K. Crammer.

(defn rls-updater
  [config,
   {:keys [w, sigma, r] :as variables},
   feature, label]
  (into variables
        {:w (arowr/calc-next-w w sigma r feature label)
         :sigma (arowr/calc-next-sigma sigma r 1 feature)}))

(def rls-config
  {:model-type :regression
   :validator-fn (fn [{:keys [r, iterations]}]
                   (and (> 1.0 r 0.0)(> iterations 0))),
   :initializer-fn cw/cw-initialzer,
   :updater-fn rls-updater,
   :weight-name :w})
