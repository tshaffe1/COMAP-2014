; copyright 2014 Tim Shaffer

;:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::;
; This program is free software: you can redistribute it and/or modify  ;
; it under the terms of the GNU General Public License as published by  ;
; the Free Software Foundation, either version 3 of the License, or     ;
; (at your option) any later version.                                   ;
;                                                                       ;
; This program is distributed in the hope that it will be useful,       ;
; but WITHOUT ANY WARRANTY; without even the implied warranty of        ;
; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         ;
; GNU General Public License for more details.                          ;
;                                                                       ;
; You should have received a copy of the GNU General Public License     ;
; along with this program.  If not, see <http://www.gnu.org/licenses/>. ;
;:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::;
(ns monte-carlo)
(defmacro dbg[x] `(let [x# ~x] (println "dbg:" '~x "=" x#) x#))
(def ziggurat
 {[1.000000 0.963929] 0.086916,
  [0.963929 0.936858] 0.115810,
  [0.936858 0.913827] 0.136125,
  [0.913827 0.893250] 0.152359,
  [0.893250 0.874380] 0.166144,
  [0.874380 0.856795] 0.178274,
  [0.856795 0.840224] 0.189202,
  [0.840224 0.824487] 0.199212,
  [0.824487 0.809450] 0.208495,
  [0.809450 0.795015] 0.217188,
  [0.795015 0.781105] 0.225389,
  [0.781105 0.767660] 0.233175,
  [0.767660 0.754630] 0.240605,
  [0.754630 0.741974] 0.247727,
  [0.741974 0.729659] 0.254578,
  [0.729659 0.717656] 0.261191,
  [0.717656 0.705940] 0.267592,
  [0.705940 0.694490] 0.273803,
  [0.694490 0.683287] 0.279844,
  [0.683287 0.672315] 0.285729,
  [0.672315 0.661559] 0.291475,
  [0.661559 0.651006] 0.297093,
  [0.651006 0.640645] 0.302594,
  [0.640645 0.630466] 0.307988,
  [0.630466 0.620459] 0.313284,
  [0.620459 0.610615] 0.318489,
  [0.610615 0.600927] 0.323611,
  [0.600927 0.591388] 0.328655,
  [0.591388 0.581991] 0.333628,
  [0.581991 0.572730] 0.338536,
  [0.572730 0.563600] 0.343381,
  [0.563600 0.554595] 0.348170,
  [0.554595 0.545712] 0.352907,
  [0.545712 0.536944] 0.357594,
  [0.536944 0.528290] 0.362236,
  [0.528290 0.519743] 0.366836,
  [0.519743 0.511302] 0.371397,
  [0.511302 0.502962] 0.375922,
  [0.502962 0.494721] 0.380414,
  [0.494721 0.486575] 0.384875,
  [0.486575 0.478522] 0.389308,
  [0.478522 0.470559] 0.393715,
  [0.470559 0.462684] 0.398098,
  [0.462684 0.454894] 0.402459,
  [0.454894 0.447187] 0.406801,
  [0.447187 0.439562] 0.411125,
  [0.439562 0.432015] 0.415433,
  [0.432015 0.424545] 0.419727,
  [0.424545 0.417152] 0.424009,
  [0.417152 0.409831] 0.428281,
  [0.409831 0.402583] 0.432543,
  [0.402583 0.395406] 0.436798,
  [0.395406 0.388297] 0.441048,
  [0.388297 0.381257] 0.445293,
  [0.381257 0.374283] 0.449535,
  [0.374283 0.367374] 0.453777,
  [0.367374 0.360529] 0.458018,
  [0.360529 0.353747] 0.462262,
  [0.353747 0.347026] 0.466508,
  [0.347026 0.340367] 0.470760,
  [0.340367 0.333767] 0.475017,
  [0.333767 0.327225] 0.479282,
  [0.327225 0.320742] 0.483556,
  [0.320742 0.314315] 0.487840,
  [0.314315 0.307945] 0.492137,
  [0.307945 0.301630] 0.496447,
  [0.301630 0.295369] 0.500772,
  [0.295369 0.289163] 0.505113,
  [0.289163 0.283009] 0.509473,
  [0.283009 0.276908] 0.513852,
  [0.276908 0.270858] 0.518253,
  [0.270858 0.264860] 0.522677,
  [0.264860 0.258913] 0.527125,
  [0.258913 0.253015] 0.531601,
  [0.253015 0.247167] 0.536104,
  [0.247167 0.241368] 0.540638,
  [0.241368 0.235618] 0.545205,
  [0.235618 0.229916] 0.549805,
  [0.229916 0.224261] 0.554442,
  [0.224261 0.218654] 0.559118,
  [0.218654 0.213094] 0.563835,
  [0.213094 0.207580] 0.568595,
  [0.207580 0.202112] 0.573402,
  [0.202112 0.196690] 0.578257,
  [0.196690 0.191314] 0.583163,
  [0.191314 0.185984] 0.588124,
  [0.185984 0.180698] 0.593143,
  [0.180698 0.175457] 0.598223,
  [0.175457 0.170261] 0.603368,
  [0.170261 0.165110] 0.608580,
  [0.165110 0.160003] 0.613866,
  [0.160003 0.154940] 0.619228,
  [0.154940 0.149921] 0.624671,
  [0.149921 0.144946] 0.630201,
  [0.144946 0.140015] 0.635822,
  [0.140015 0.135129] 0.641541,
  [0.135129 0.130286] 0.647364,
  [0.130286 0.125487] 0.653297,
  [0.125487 0.120732] 0.659349,
  [0.120732 0.116021] 0.665526,
  [0.116021 0.111355] 0.671838,
  [0.111355 0.106733] 0.678295,
  [0.106733 0.102155] 0.684907,
  [0.102155 0.097623] 0.691686,
  [0.097623 0.093135] 0.698646,
  [0.093135 0.088693] 0.705801,
  [0.088693 0.084297] 0.713167,
  [0.084297 0.079948] 0.720765,
  [0.079948 0.075645] 0.728614,
  [0.075645 0.071389] 0.736738,
  [0.071389 0.067182] 0.745167,
  [0.067182 0.063024] 0.753931,
  [0.063024 0.058915] 0.763069,
  [0.058915 0.054858] 0.772624,
  [0.054858 0.050852] 0.782650,
  [0.050852 0.046899] 0.793208,
  [0.046899 0.043002] 0.804375,
  [0.043002 0.039161] 0.816247,
  [0.039161 0.035379] 0.828941,
  [0.035379 0.031658] 0.842611,
  [0.031658 0.028002] 0.857455,
  [0.028002 0.024414] 0.873742,
  [0.024414 0.020898] 0.891849,
  [0.020898 0.017462] 0.912324,
  [0.017462 0.014113] 0.936016,
  [0.014113 0.010862] 0.964348,
  [0.010862 0] 0.999999})
(defn normal-rand
 ([]
  (normal-rand 1))
 ([n]
  (let
   [r (rand)
    s (rand)]
   (if
    (> r (get
          ziggurat
          (some
           #(if
             (and
              (< s (first %))
              (>= s (second %)))
             %)
           (keys ziggurat))))
    (* n (+ 0.5 (* 0.5 (- 1 r) (if (= 0 (rand-int 2)) 1 -1))))
    (recur n)))))
(defn normal-rand-int [n] (int (normal-rand n)))
(defn gen-param [spec]
 (if
  (instance? Number spec)
  spec
  (let
   [r (+ (* (normal-rand)
            (or (:scale spec) 1))
         (or (:offset spec) 0))]
   (if
    (:float spec)
    r
    (int r)))))
(defn gen-car [spec]
 (reduce
  merge
  (map
   #(hash-map % (gen-param (get spec %)))
   (keys spec))))
(defn gen-board
 ([board-spec car-specs]
  (gen-board car-specs {} (gen-param board-spec) 0))
 ([car-specs board n i]
  (if
   (> i n)
   board
   (recur
    car-specs
    (let
     [c (gen-car (rand-nth car-specs))]
     (assoc
      board
      (vector
       (:initial-x c)
       (:initial-y c))
      c))
    n
    (inc i)))))
(defn car-ticks [car]
 (or (:ticks (or (:stats car) {})) 0))
(defn car-crash-count [car]
 (or (:crashes (or (:stats car) {})) 0))
(defn car-worst-crash [car]
 (or (:worst-crash (or (:stats car) {})) 0))
(defn car-holdups [car]
 (or (:holdups (or (:stats car) {})) 0))
(defn car-avg-speed [road-length car]
 (/
  (- road-length (or (:initial-y car) 0))
  (float (max 1 (car-ticks car)))))
(defn run-summary
 ([road-length run-data]
  (run-summary
   road-length
   run-data
   (count run-data)
   {}))
 ([road-length run-data car-count summary]
  (if
   (empty? run-data)
   summary
   (recur
    road-length
    (rest run-data)
    car-count
    (assoc
     summary
     :ticks (+
             (or (:ticks summary) 0)
             (/ (car-ticks (first run-data)) (float car-count)))
     :crash-count (+
                   (or (:crash-count summary) 0)
                   (/ (car-crash-count (first run-data))
                      (float car-count)))
     :worst-crash (+
                   (or (:worst-crash summary) 0)
                   (/ (car-worst-crash (first run-data))
                      (float car-count)))
     :holdups (+
               (or (:holdups summary) 0)
               (/ (car-holdups (first run-data))
                  (float car-count)))
     :avg-speed (+
                 (or (:avg-speed summary) 0)
                 (/ (car-avg-speed road-length (first run-data))
                    (float car-count))))))))
(defn trial
 ([iter road-length ticker board-spec car-specs]
  (trial iter road-length ticker board-spec car-specs 0 (list)))
 ([iter road-length ticker board-spec car-specs i trial-data]
  (if
   (>= i iter)
   trial-data
   (recur
    iter
    road-length
    ticker
    board-spec
    car-specs
    (inc i)
    (conj
     trial-data
     (run-summary
      road-length
      (ticker
       (gen-board
        board-spec
        car-specs))))))))
(defn trial-summary
 ([trial-data]
  (trial-summary trial-data (count trial-data) {}))
 ([trial-data trial-count summary]
  (if
   (empty? trial-data)
   summary
   (recur
    (rest trial-data)
    trial-count
    (assoc summary
     :avg-speed (+
                 (or (:avg-speed summary) 0)
                 (/ (or (:avg-speed (first trial-data)) 0)
                    (float trial-count)))
     :holdups (+
               (or (:holdups summary) 0)
               (/ (or (:holdups (first trial-data)) 0)
                  (float trial-count)))
     :worst-crash (+
                   (or (:worst-crash summary) 0)
                   (/ (or (:worst-crash (first trial-data)) 0)
                    (float trial-count)))
     :crash-count (+
                 (or (:crash-count summary) 0)
                 (/ (or (:crash-count (first trial-data)) 0)
                    (float trial-count)))
     :ticks (+
                 (or (:ticks summary) 0)
                 (/ (or (:ticks (first trial-data)) 0)
                    (float trial-count))))))))
