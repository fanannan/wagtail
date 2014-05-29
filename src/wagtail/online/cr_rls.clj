(ns wagtail.online.cr-rls
  (:require [clatrix.core :as cl]
            [wagtail.shared :as shared]
            [wagtail.online.cw :as cw]
            [wagtail.online.arowr :as arowr]))


;; CR-RLS
;; based on "Second-Order Non-Stationary Online Learning for Regression" by N. Vaits, E. Moroshko and K. Crammer.

(defn cr-rls-updater
  [config,
   {:keys [w, sigma, r, t, T, I] :as variables},
   feature, label]
  (into variables
        {:w (arowr/calc-next-w w sigma r feature label)
         :sigma (if (zero? (mod t T))
                  I
                  (arowr/calc-next-sigma sigma r 1 feature))
         :t (inc t)}))

(defn cr-rls-initialzer
  [config, variables, num-fields]
  (into (cw/cw-initialzer config variables num-fields)
        {:I (cl/eye num-fields)
         :t 1}))

(def cr-rls-config
  {:model-type :regression
   :validator-fn (fn [{:keys [r, T, iterations]}]
                   (and (> 1.0 r 0.0)(integer? T)(pos? T)(> iterations 0))),
   :initializer-fn cr-rls-initialzer,
   :updater-fn cr-rls-updater,
   :weight-name :w})
