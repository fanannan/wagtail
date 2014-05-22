(ns wagtail.scale
  (:require [wagtail.math :as wmath]))
(binding [*warn-on-reflection* false]
  (use '[incanter.stats :only [quantile] :as stats]))

(defn stat
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

(defn field-stat
 "統計情報の作成
  records: データレコードの束
  field-keys: 統計情報作成対象項目のセット
  limit: パーセンタイル値の適用水準
  number-of-records: 対象レコード数(nilなら全部)"
  [records, limit, number-of-records]
  {:pre [(every? map? records)(< 0.0 limit 1.0)]}
  (mapv (fn[xs] (stat xs limit))
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
 "データ項目のアンスケーリングを行う
  value: 対象データ項目の値
  stat: 統計情報
  method-key: スケーリング方法"
  [value, statm, method-key]
  {:pre [(number? value)(map? statm)(keyword? method-key)]}
  (let [iv (imp value)
        {:keys [max, min, upper-bound, lower-bound]} statm]
    (case method-key
      :raw             value
      :simple          (+ (* iv (- max min)) min)
      :bounded         (+ (* iv (- upper-bound lower-bound)) lower-bound)
      :zero-bounded    (if (pos? value)
                              (* value upper-bound)
                              (* value lower-bound)))))

(defn- scale-item
 "データ項目のスケーリングを行う
  field-key: 対象データ項目を示すキーワード
  value: 対象データ項目の値
  stat: 統計情報
  method-key: スケーリング方法"
  [value, statm, method-key]
  {:pre [(number? value)(map? statm)(keyword? method-key)]}
  (let [{:keys [max, min, upper-bound, lower-bound]} statm]
    (if (= upper-bound lower-bound)
      Double/NaN
      (case method-key
        :raw             value
        :simple          (mp (/ (- value min)(- max min)))
        :bounded         (mp (/ (- value lower-bound)(- upper-bound lower-bound)))
        :zero-bounded    (if (pos? value)
                             (/ value upper-bound)
                             (/ value lower-bound))))))

(defn- scale-items
 "データ項目のスケーリングを行う
  record: 対象データレコード
  stat: 統計情報
  method-key: スケーリング方法"
  [record, stat, method-key]
  {:pre [(vector? record)(every? map? stat)(keyword? method-key)]}
  (mapv (fn[v m] (scale-item v m method-key))
        record
        stat))

(defn scale
 "データ全体のスケーリングを行う
  records: 対象データレコード全体
  stat: 統計情報
  method-key: スケーリング方法"
  [records, stat, method-key]
  {:pre [(coll? records)(every? map? stat)(keyword? method-key)]}
  (mapv #(scale-fields % stat method-key) records))


(def x (stat (range 500) 0.9))
(unscale-item 30 x :raw)
(unscale-item (scale-item 30 x :raw) x :raw)
(unscale-item (scale-item 30 x :simple) x :simple)
(unscale-item (scale-item 30 x :bounded) x :bounded)
(unscale-item (scale-item 30 x :zero-bounded) x :zero-bounded)
