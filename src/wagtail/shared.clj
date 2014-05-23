(ns wagtail.shared
    (:require [clatrix.core :as cl]
              [wagtail.scale :as scale]))


;; commonly used initialization

(defn weight-initialzer
  [config, variables, num-fields]
  (into variables
        {(:weight-name config) (cl/zeros num-fields)}))

(defn weight-t-initialzer
  [config, variables, num-fields]
  (into (weight-initialzer config variables num-fields)
        {:t 1}))


;; commonly used formulae

(defn estimate
  "Calculate WtXt and returns a scalar value.
   w: weight vector (clatrix matrix)
   feature: example vetcor x (clatrix matrix)"
  ;(estimate (cl/matrix [1 2 3])(cl/matrix [4 5 6]))) → 32.0
  ;(estimate (cl/t (cl/matrix [1 2 3]))(cl/matrix [4 5 6]))) → 32.0
  [w, feature]
  (cl/dot w feature))

(defn margin
  "Calculate margin Yt(WtXt) and returns a scalar value.
   w: weight vector (clatrix matrix)
   feature: example vetcor x (clatrix matrix)
   label: label y (1 or -1)"
  ;(margin (cl/t (cl/matrix [1 2 3]))(cl/matrix [4 5 6]) -1.0)) → -32.0
  [w, feature, label]
  (* label (estimate w feature)))

(defn classify
  "Calculate the classification result and returns 1 or -1"
  ; sign(WtXt) <-> Yt
  [w, feature]
  (let [r (estimate w feature)]
    (cond (> r 0.0) 1
          (< r 0.0) -1
          :else 0)))


;; training

(defn update
  "Execute the update process in a training algorithm.
   Returns a hash map with the updated variables (ex: weight vector)"
  [{:keys [loss-fn, updater-fn, unupdater-fn] :as config}, variables, feature, label]
  (if (nil? loss-fn)
      (updater-fn config variables feature label)
    (let [loss (loss-fn config variables feature label)]
      (if (not (pos? loss))
        (if (nil? unupdater-fn)
          variables
          (unupdater-fn config variables feature label))
        (updater-fn config variables feature label)))))

(defn trainer
  "Execute online learning record by record.
   Returns a hash map with the updated variables (ex: weight vector)
   after processing all the records (features and labels)."
  [config, variables, features, labels]
  (reduce (fn[variables, [feature label]] (update config variables feature label))
          variables
          (zipmap features labels)))

(defn looper
  "Iterate applying training function.
   Returns a hash map with the result variables (ex: weight vector)
   after training 'iterations' times."
  [iterations, func, init]
  (reduce (fn[r _](func r)) init (range iterations)))

(defn train-model
  "Train a model and returns the result variables (ex: weight vector)."
  [{:keys [model-type, validator-fn, initializer-fn] :as config},
   {:keys [iterations] :as variables},
   scaled-features, scaled-labels]
  (assert (validator-fn variables))
  ; 回帰ならバイアス項を追加する
  ;;
  (let [num-features (count (first scaled-features))
        initial-variables (initializer-fn config variables num-features)]
       (looper iterations
               (fn[variables] (trainer config variables scaled-features scaled-labels))
               initial-variables)))

(defn make-learner [config]
  (partial train-model config))


;; execute a model

(defn run-model
  "Run a model and return the predictions.
   Returns a list with vectors of a feature and a prediction."
  [config, {:keys [method] :as scale}, label-stat, variables, scaled-features]
  (let [w ((:weight-name config) variables)
        us (if (nil? scale)
            identity
            (fn[value] (scale/unscale-item value (first label-stat) method)))
        f (case (:model-type config)
            :classification classify
            :regression estimate)]
    (map (fn[scaled-feature] [scaled-feature (us (f w scaled-feature))])
         scaled-features)))
