(ns wagtail.data-handler
  (:require [clatrix.core :as cl]
            [wagtail.scale :as scale]
            [wagtail.reader.iris :as iris]
            [wagtail.reader.digits :as digits]
            [wagtail.reader.death-rates :as death-rates]))


;; data handling functions (should be rewritten)

(defn matrixize* [{:keys [features labels] :as xys}]
  {:features (map #(cl/matrix %) features)
   :labels (map first labels)}) ;takes only one label from a label vector

(defn matrixize [{:keys [train test stats] :as data-set}]
  (into data-set
        {:train (into train (matrixize* train))
         :test (into test (matrixize* test))}))

(defn add-bias* [{:keys [features] :as xys}]
  (into xys
        {:features (mapv #(cl/matrix (conj % 1.0)) features)}))

(defn add-bias [bias {:keys [train test stats] :as data-set}]
  (if (nil? bias)
    data-set
    (into data-set
          {:train (into train (add-bias* train))
           :test (into test (add-bias* test))})))

(defn apply-scale*
  [scale,
   {:keys [feature-stats, label-stats] :as stats},
   {:keys [original-features, original-labels]}]
  {:features (scale/scale original-features feature-stats (:method scale))
   :labels (scale/scale original-labels label-stats (:method scale))})

(defn apply-stat
  [{:keys [original-features, original-labels]}, limit]
  {:feature-stats (scale/stat original-features limit),
   :label-stats (scale/stat original-labels limit)})

(defn apply-scale
  "Scale data sets.
   Returns the scaled train data, the scaled test data and the stats for scaling"
  [scale data-map]
  (if (nil? scale)
    {:train (into (:train data-map)
                  {:features (:original-features (:train data-map))
                   :labels (:original-labels (:train data-map))})
     :test  (into (:test data-map)
                  {:features (:original-features (:test data-map))
                   :labels (:original-labels (:test data-map))})
     :stats nil}
    (let [stats (apply-stat (:train data-map) (:limit scale))]
       {:train (into (:train data-map)
                     (apply-scale* scale stats (:train data-map)))
        :test  (into (:test data-map)
                     (apply-scale* scale stats (:test data-map)))
        :stats stats})))

(defn read-data
  "Returns a map {:train {:original-features xs, :original-labels ys} :test ..}
   train-features and test-features: a vector of feature vectors
   train-labels and test-labels: a vector of label vectors"
  [{:keys [data-type] :as data}]
  (case data-type
        :iris (iris/prepare-iris data)
        :digits (digits/prepare-digits data)
        :death-rates (death-rates/prepare-death-rates data)))

(defn prepare-data
  [{:keys [bias] :as config}, {:keys [scale] :as data}]
  (->> (read-data data)
       (apply-scale scale)
       (add-bias bias)
       (matrixize)))
