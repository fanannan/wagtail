(ns wagtail.reader.data-handler
  (:require [clatrix.core :as cl]
            [wagtail.reader.iris :as iris]
            [wagtail.reader.digits :as digits]
            [wagtail.reader.death-rates :as death-rates]))


;; data handling functions (should be rewritten)

(defn add-bias* [features]
  (map #(cl/vstack (cl/matrix [1.0]) %) features))

(defn add-bias [[features labels]]
  [(add-bias* features) labels])

(defn prepare-data*
  [{:keys [data-type] :as data}]
  (case data-type
        :iris (iris/prepare-iris data)
        :digits (digits/prepare-digits data)
        :death-rates (death-rates/prepare-death-rates data)))

(defn prepare-data
  [{:keys [bias] :as config}, {:keys [data-type] :as data}]
  (let [[train test] (prepare-data* data)]
    (if (true? bias)
      [(add-bias train) (add-bias test)]
      [train test])))
