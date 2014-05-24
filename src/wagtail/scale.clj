(ns wagtail.scale
  (:require [wagtail.math :as wmath]))
(binding [*warn-on-reflection* false]
  (use '[incanter.stats :only [quantile] :as stats]))


(defn stat-item
 "Statistics info of the given number sequence.
  xs: number sequence
  limit: percentile applied to the number sequence"
  [xs, limit]
  {:pre [(every? number? xs)(<= 0.0 limit 1.0)]}
  {:max (apply max xs),
   :upper-bound (stats/quantile xs :probs (- 1.0 limit)),
   :average (/ (apply + xs)(count xs)),
   :median (stats/quantile xs :probs 0.5),
   :lower-bound (stats/quantile xs :probs limit),
   :min (apply min xs),
   :stdev (wmath/stdev xs),
   :limit limit})

(defn transpose
  [xys]
  (apply mapv vector xys))

(defn stat
 "Make statistic info of each featuer item of the given records
  records: a collection of data records (a vector of vectors)
  limit: percentile position used for :bound and :zero-bound methods"
  [records, limit]
  {:pre [(every? vector? records)(< 0.0 limit 1.0)]}
  (mapv (fn[xs] (stat-item xs limit))
        (transpose records)))

(defn mp
 "0..1 -> -1..+1"
  [x]
  (let [v (- (* x 2.0) 1.0)]
    (if (> v 1.0)
      1.0
      (if (> -1.0 v)
          -1.0
          v))))

(defn imp
 "-1..+1 -> 0..1"
  [x]
  (/ (+ x 1.0) 2.0))

(defn unscale-item
 "Unscale a value from -1..+1 range
  value: value of a scaled data item
  statm: stat info given by function 'stat'
  method-key: scaling method"
  [value, statm, method-key]
  {:pre [(number? value)(map? statm)(keyword? method-key)]}
  (let [{:keys [max, min, upper-bound, lower-bound]} statm]
    (case method-key
      :raw             value
      :simple          (+ (* (imp value) (- max min)) min)
      :bounded         (+ (* (imp value) (- upper-bound lower-bound)) lower-bound)
      :zero-bounded    (if (pos? value)
                              (* value upper-bound)
                              (* value lower-bound)))))

(defn- scale-item
 "Scale an data item within the range of -1..+1
  value: value of a data item
  statm: stat info given by function 'stat'
  method-key: scaling method"
  [value, statm, method-key]
  {:pre [(number? value)(map? statm)(keyword? method-key)]}
  (let [{:keys [max, min, upper-bound, lower-bound]} statm]
    (if (and (= upper-bound lower-bound)
             (or (= method-key :bounded)(= method-key :zero-bounded)))
      Double/NaN
      (case method-key
        :raw             value
        :simple          (mp (/ (- value min)(- max min)))
        :bounded         (mp (/ (- value lower-bound)(- upper-bound lower-bound)))
        :zero-bounded    (if (pos? value)
                             (/ value upper-bound)
                             (/ value lower-bound))))))

(defn- scale-record
 "Scale a record of numerical values
  record: a vector of numerical values
  stat: a vector of stat info given by function 'stat'
  method-key: scaling method"
  [record, stat, method-key]
  (mapv (fn[v m] (scale-item v m method-key))
        record
        stat))

(defn scale
 "Scale a collection of records.
  records: a vector of vectors.
  stat: a vector of stat info given by function 'stat'
  method-key: scaling method"
  [records, stat, method-key]
  {:pre [(every? vector? records)(every? map? stat)(keyword? method-key)]}
  (mapv #(scale-record % stat method-key) records))
