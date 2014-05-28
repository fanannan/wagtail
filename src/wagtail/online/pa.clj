(ns wagtail.online.pa
  (:require [clatrix.core :as cl]
            [wagtail.shared :as shared]))


;; passive agressive

(defn calssifier-loss [config, {:keys [w] :as variables}, feature, label]
  (max 0.0 (- 1.0 (shared/margin w feature label))))

(defn regression-loss [config, {:keys [w, epsiron] :as variables}, feature, label]
  (max 0.0 (- (Math/abs (- (shared/estimate w feature) label)) epsiron)))

(defn updater
  [tau-fn,
   {:keys [loss-fn] :as config},
   {:keys [w, c] :as variables},
   feature, label]
  (let [f (cl/norm feature)
        tau (tau-fn (loss-fn config variables feature label) f c)]
    (into variables
          {:w (cl/+ w (cl/* tau label feature))})))

(def pa-updater
  (partial updater (fn[loss, f, _] (/ loss (* f f)))))

(def pa1-updater
  (partial updater (fn[loss, f, c] (min c (/ loss (* f f))))))

(def pa2-updater
  (partial updater (fn[loss, f, c] (/ loss (+ (* f f) (/ 1.0 (* 2 c)))))))


(def pa-classifier-config
  {:model-type :classification
   :validator-fn (fn[{:keys [iterations]}] (> iterations 0)),
   :initializer-fn shared/weight-initialzer,
   :updater-fn pa-updater,
   :loss-fn calssifier-loss,
   :weight-name :w})

(def pa1-classifier-config
  (into pa-classifier-config
        {:validator-fn (fn[{:keys [c, iterations c]}]
                         (and (> c 0.0)(> iterations 0))),
         :updater-fn pa1-updater}))

(def pa2-classifier-config
  (assoc pa1-classifier-config :updater-fn pa2-updater))

(def pa-regression-config
  (into pa-classifier-config
        {:model-type :regression
         :validator-fn (fn[{:keys [epsiron, iterations]}]
                         (and (> epsiron 0.0)(> iterations 0))),
         :loss-fn regression-loss}))
