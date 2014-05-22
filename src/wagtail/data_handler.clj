(ns wagtail.data-handler
  (:require [clatrix.core :as cl]
            [wagtail.scale :as scale]
            [wagtail.reader.iris :as iris]
            [wagtail.reader.digits :as digits]
            [wagtail.reader.death-rates :as death-rates]))


;; data handling functions (should be rewritten)
; [train test]
; [[train-features train-labels stat] [test-features test-labels stat] ]


(defn add-bias* [features]
  (map #(cl/vstack (cl/matrix [1.0]) %) features))

(defn add-bias [[features labels & [stats]]]
  [(add-bias* features) labels stats])

(defn apply-scale*
  [scale [feature-stats label-stat] [features labels]]
  [(scale/scale (map cl/as-vec features) feature-stats (:method scale))
   (map first (scale/scale (map vector labels) (vector label-stat) (:method scale)))])

(defn apply-stat
  [[features labels] limit]
  [(scale/stat (map cl/as-vec features) limit)
   (first (scale/stat (map vector labels) limit))])

(defn apply-scale
  "Scale data sets.
   Returns the scaled train data, the scaled test data and the stats for scaling:
   [[train-features train-labels] [test-features test-labels] [feature-stats label-stat]]"
  [scale [train test]]
  (if (nil? scale)
    [train test nil]
    (let [stats (apply-stat train (:limit scale))]
      [(apply-scale* scale stats train)
       (apply-scale* scale stats test)
       stats])))

(defn prepare-data*
  "Returns [[train-features train-labels] [test-features test-labels]]
   train-features and test-features: a vector of feature cl/matrix vrcyprs
   train-labels and test-labels: a vector of number (label value)"
  [{:keys [data-type] :as data}]
  (case data-type
        :iris (iris/prepare-iris data)
        :digits (digits/prepare-digits data)
        :death-rates (death-rates/prepare-death-rates data)))

(defn prepare-data
  [{:keys [bias] :as config}, {:keys [scale] :as data}]
  (let [[train test stats] (apply-scale scale (prepare-data* data))]
    (if (true? bias)
      [(add-bias train) (add-bias test) stats]
      [train test stats])))
