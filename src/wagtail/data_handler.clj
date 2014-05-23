(ns wagtail.data-handler
  (:require [clatrix.core :as cl]
            [wagtail.scale :as scale]
            [wagtail.reader.iris :as iris]
            [wagtail.reader.digits :as digits]
            [wagtail.reader.death-rates :as death-rates]))


;; data handling functions (should be rewritten)
; [train test]
; [[train-features train-labels stat] [test-features test-labels stat] ]

(defn matrixize* [[features labels]]
  [(map cl/matrix features)
   (map first labels)]) ;takes only one label from a label vector

(defn matrixize [[train test stats]]
  [(matrixize* train) (matrixize* test) stats])

(defn add-bias* [[features labels]]
  [(mapv #(cl/matrix (conj % 1.0)) features) labels])

(defn add-bias [bias [train test stats]]
  (if (nil? bias)
    [train test stats]
    [(add-bias* train) (add-bias* test) stats]))

(defn apply-scale*
  [scale [feature-stats label-stat] [features labels]]
  [(scale/scale features feature-stats (:method scale))
   (scale/scale labels label-stat (:method scale))])

(defn apply-stat
  [[features labels] limit]
  [(scale/stat features limit)
   (scale/stat labels limit)])

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

(defn read-data
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
  (->> (read-data data)
       (apply-scale scale)
       (add-bias bias)
       (matrixize)))
