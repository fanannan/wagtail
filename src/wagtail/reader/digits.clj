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

(def train-digits (load-digits "./resources/digits_train.csv"))
(def test-digits (load-digits "./resources/digits_test.csv"))
(def data [train-digits test-digits])

(defn prepare-digits [data]
  (:records data))
