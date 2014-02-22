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
(ns gameboard
 (:load "car"))
(defn logistic [x]
 (dec (/ 2 (inc (java.lang.Math/exp (- x))))))
(defn make-ticker-keepright
 [width height]
 (car/make-ticker
  (car/make-whereto-keepright
   (car/make-west-free width height)
   (car/make-northwest-free width height)
   (car/make-north-free width height)
   (car/make-northeast-free width height)
   (car/make-east-free width height))))
(defn make-ticker-wherever
 [width height]
 (car/make-ticker
  (car/make-whereto-wherever
   (car/make-west-free width height)
   (car/make-northwest-free width height)
   (car/make-north-free width height)
   (car/make-northeast-free width height)
   (car/make-east-free width height))))
(defn make-ticker-halfassed
 ([width height]
  (make-ticker-halfassed 0.5 width height))
 ([ratio width height]
 (car/make-ticker
  (car/make-whereto-halfassed
   ratio
   (car/make-west-free width height)
   (car/make-northwest-free width height)
   (car/make-north-free width height)
   (car/make-northeast-free width height)
   (car/make-east-free width height)))))
(defn safe-assoc [coll pair]
 (if
  (contains? coll (first pair))
  (assoc coll (or (:prev-pos (second pair)) [-1 -1]) (second pair))
  (assoc coll (first pair) (second pair))))
(defn dead-guys [board height]
 (map
  (partial get board)
  (filter
   #(>= (second %) height)
   (keys board))))
(defn live-guys [board height]
 (reduce
  dissoc
  board
  (filter #(>= (second %) height) (keys board))))
(defn pbm
 ([width height board]
  (pbm width height board
   (str "P1" \newline width \space height \newline) 0 0))
 ([width height board s x y]
  (if
   (= x (dec width))
   (if
    (= y (dec height))
    (str s (if (contains? board [x y]) 1 0) \newline)
    (recur
     width height board
     (str s (if (contains? board [x y]) 1 0) \newline) 0 (inc y)))
   (recur
    width height board
    (str s (if (contains? board [x y]) 1 0) \space) (inc x) y))))
(defn rainbow [board cell]
 (if (contains? board cell)
     (str
      (int
       (* 255 (logistic
               (/ (or
                   (get
                    (or
                     (get
                      (or
                       (get board cell) {})
                      :stats) {})
                    :crashes) 0) 0.25)))) \space
      (int
       (* 255 (logistic
               (/ 5
                (or (get (or (get board cell) {}) :speed) 1))))) \space
      (int
       (* 255 (logistic (/ (or
                            (get
                             (or
                              (get
                               (or
                                (get board cell) {})
                               :stats) {})
                             :holdups) 0) 2)))))
     "0 0 0"))
(defn ppm
 ([colorizer width height board]
  (ppm colorizer width height board
   (str "P3" \newline width \space height \newline 255 \newline) 0 0))
 ([colorizer width height board  s x y]
  (if
   (= x (dec width))
   (if
    (= y (dec height))
    (str s (colorizer board [x y]) \newline)
    (recur
     colorizer width height board
     (str s (colorizer board [x y]) \newline) 0 (inc y)))
   (recur
    colorizer width height board
    (str s (colorizer board [x y]) \space \space) (inc x) y))))
(defn make-loop
 [ticker height]
 (fn tick*
  ([board]
   (tick* board nil 0))
  ([board corpses iter]
   (let
    [live (live-guys board height)
     dead (dead-guys board height)]
    (if
     (empty? live)
     (reduce conj corpses dead)
     (recur
      (reduce safe-assoc {}
       (pmap (partial ticker live) live))
      (reduce conj corpses dead)
      (inc iter)))))))
(defn make-history [ticker height]
 (fn tick*
  ([board]
   (tick* board nil))
  ([board past]
   (let
    [live (live-guys board height)]
    (if
     (empty? live)
     (conj past board)
     (recur
      (reduce safe-assoc {}
       (pmap (partial ticker live) live))
      (conj past board)))))))
(defn dump-history
 ([renderer path-format history]
  (println "dumping to disk...")
  (dump-history renderer path-format (reverse history) 0))
 ([renderer path-format history i]
  (if
   (not (empty? history))
   (do
    (spit (format path-format i) (renderer (first history)))
    (recur renderer path-format (rest history) (inc i))))))
