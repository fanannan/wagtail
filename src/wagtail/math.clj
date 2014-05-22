(ns wagtail.math
  (:import [org.apache.commons.math3.special Erf]))

(defn probit
  "inverse function of cumulative normal distribution function"
  [p] ; Φの逆関数
  (* (Math/sqrt 2.0) (Erf/erfInv (- (* p 2.0) 1.0))))

;(defn Φ [x]
;  ; cdf-normal: μ=0, σ2=1 のガウス分布の累積分布関数
;  (* 0.5 (+ 1.0 (Erf/erf (/ x (Math/sqrt 2.0))))))

(defn sigmoid
  [z]
  (/ 1.0 (+ 1.0 (Math/exp (- z)))))

(defn stdev
  [xs]
  (let [n (count xs)
	      mean (/ (reduce + xs) n)
	      intermediate (map #(Math/pow (- %1 mean) 2) xs)]
    (Math/sqrt (/ (reduce + intermediate) n))))
