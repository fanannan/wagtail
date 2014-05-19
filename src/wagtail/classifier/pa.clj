(ns wagtail.classifier.pa
  (:require [clatrix.core :as cl]
            [wagtail.shared :as shared]))


;; passive agressive

(defn calc-loss [config, {:keys [w] :as variables}, feature, label]
  (max 0.0 (- 1.0 (shared/margin w feature label))))

(defn updater
  [tau-fn,
   {:keys [loss-fn] :as config},
   {:keys [w c] :as variables},
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

(defn pa-initialzer
  [config, variables, num-fields]
  (into variables
        {:w (cl/zeros num-fields)}))

(def pa-config
  {:validator-fn (fn[{:keys [iterations]}] (> iterations 0)),
   :initializer-fn pa-initialzer,
   :updater-fn pa-updater,
   :loss-fn calc-loss})

(def pa1-config
  (into pa-config
        {:validator-fn (fn[{:keys [iterations c]}]
                         (and (> c 0.0)(> iterations 0))),
         :updater-fn pa1-updater}))

(def pa2-config
  (assoc pa1-config :updater-fn pa2-updater))
