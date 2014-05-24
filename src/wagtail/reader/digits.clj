(ns wagtail.reader.digits
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]))


;; (should be rewritten)

; load MNIST data sets
(defn load-digits [filepath]
  (with-open [in-file (io/reader filepath)]
    (let [contents (csv/read-csv in-file)
          features (mapv #(mapv read-string %) (rest contents))
          labels (mapv #(vector (read-string %)) (first contents))]
      (assert (= (count labels) (count features)))
      [features labels])))

(def data
  (let [[train-features train-labels] (load-digits "./resources/digits_train.csv")
        [test-features test-labels] (load-digits "./resources/digits_test.csv")]
    {:train
     {:original-features train-features,
      :original-labels train-labels},
     :test
     {:original-features test-features,
      :original-labels test-labels}}))

(defn prepare-digits [data]
  (:records data))
