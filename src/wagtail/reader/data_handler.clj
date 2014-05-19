(ns wagtail.reader.data-handler
  (:require [wagtail.reader.iris :as iris]
            [wagtail.reader.digits :as digits]))


;; data handling functions (should be rewritten)

(defn prepare-data [{:keys [data-type] :as data}]
  (case data-type
    :iris (iris/prepare-iris data)
    :digits (digits/prepare-digits data)))
