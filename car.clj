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

; seeing car used in this way will annoy the hell out of any old-school
; lisp programmers.

; each car is a hashmap of properties.
; a board is a hashmap of cars keyed by position vectors.
; [x y]: {:speed n,                   ; the car will move every nth turn
;         :move-timer n,              ; records n turns since the last move
;         :delay-timer n,             ; unable to move for n turns.
;         :mood n,                    ; poor outlook gets you in crashes
;         :min-mood,
;         :prev-pos [x y],
;         :initial-x,
;         :initial-y,
;         :stats {:ticks n,           ; the stats are pretty much self-
;                 :crashes n,         ; explanatory.
;                 :worst-crash n,
;                 :holdups n}}
(ns car)
(defmacro dbg[x] `(let [x# ~x] (println "dbg:" '~x "=" x#) x#))
; first, some abstractions that will clarify later logic
(defn next-move-timer [car]
 (mod (inc (or (:move-timer car) 0))
      (or (:speed car) 0)))
(defn calc-delay-timer [car crash]
 (if (zero? crash)
  (max 0 (dec (or (:delay-timer car) 0)))
  (+ crash (or (:delay-timer car) 0))))
(defn calc-crash-count [car crash]
 (+ (or (:crashes (or (:stats car) {})) 0)
    (if (zero? crash) 0 1)))
(defn calc-worst-crash [car crash]
 (max crash (or (:worst-crash (or (:stats car) {})) 0)))
(defn next-tick [car]
 (inc (or (:ticks (or (:stats car) {})) 0)))
(defn held-up? [before after]
 (= before after))
(defn wants-to-advance? [car]
 (= 0
  (mod
   (or (:move-timer car) 0)
   (or (:speed car) 1))))
(defn allowed-to-advance? [car]
 (= 0 (or (:delay-timer car) 0)))
(defn calc-holdup [car before after]
 (if
  (and
   (wants-to-advance? car)
   (held-up? before after))
  (inc (or (:holdups (or (:stats car) {})) 0))
  (or (:holdups (or (:stats car) {})) 0)))
(defn calc-mood [car before after]
 (if
  (and
   (wants-to-advance? car)
   (held-up? before after))
  (min
   17
   (+ 2 (or (:mood car) 1)))
  (max
   1
   (-
    (or (:mood car) 1)
    (if (= 0 (rand-int 9)) 1 0)))))
(defn west-of [cell]
 (vector
  (dec (first cell))
  (second cell)))
(defn northwest-of [cell]
 (vector
  (dec (first cell))
  (inc (second cell))))
(defn north-of [cell]
 (vector
  (first cell)
  (inc (second cell))))
(defn northeast-of [cell]
 (vector
  (inc (first cell))
  (inc (second cell))))
(defn east-of [cell]
 (vector
  (inc (first cell))
  (second cell)))
(defn south-of [cell]
 (vector
  (first cell)
  (dec (second cell))))
(defn southeast-of [cell]
 (vector
  (inc (first cell))
  (dec (second cell))))
(defn southwest-of [cell]
 (vector
  (dec (first cell))
  (dec (second cell))))
(defn cars-adjacent [board cell]
 (filter (partial contains? #{(east-of cell)
                              (west-of cell)
                              (north-of cell)
                              (south-of cell)
                              (northeast-of cell)
                              (northwest-of cell)
                              (southeast-of cell)
                              (southwest-of cell)}) (keys board)))
(defn speed-difference [board cell]
 (let
  [speeds
   (cons
    (or (:speed (get board cell)) 1)
    (map #(or (get (get board %) :speed) 1) (cars-adjacent board cell)))]
  (- (reduce max speeds) (reduce min speeds))))
(defn calc-crash [board cell]
 (let
  [speed-difference** (speed-difference board cell)]
  (if
   (and
    (allowed-to-advance? (get board cell))
    (< ((fn [x] (* x x)) (rand))
       (* 1e-5 speed-difference**
          (+
          (or (:mood (or (get board cell) {})) 1)
          (or (:min-mood (or (get board cell) {})) 0)))))
   (* speed-difference** (or (:mood (or (get board cell) {})) 1))
   0)))
; the following functions generate functions that check if neighboring
; cells are free based on board dimensions.
(defn make-west-free [width height]
 (fn [board cell]
  (and
   (> (first cell) 0)
   (not
    (contains?
     board
     (west-of cell))))))
(defn make-northwest-free [width height]
 (fn [board cell]
  (and
   (< (second cell) height)
   (> (first cell) 0)
   (not
    (contains?
     board
     (northwest-of cell))))))
(defn make-north-free [width height]
 (fn [board cell]
  (and
   (< (second cell) height)
   (not
    (contains?
     board
     (north-of cell))))))
(defn make-northeast-free [width height]
 (fn [board cell]
  (and
   (< (second cell) height)
   (< (first cell) (dec width))
   (not
    (contains?
     board
     (northeast-of cell))))))
(defn make-east-free [width height]
 (fn [board cell]
  (and
   (< (first cell) (dec width))
   (not
    (contains?
     board
     (east-of cell))))))
; now the heart of the problem: decide where to move next based on
; local traffic laws, i.e. keep right or whatever.
; for the symmetric version, a choice must be made if two equivalent paths
; are available. left and right are randomly flipped on each decision
; to make sure this rule doesn't degenerate to keepright.
(defn make-whereto-wherever
 [west?
  northwest?
  north?
  northeast?
  east?]
 (fn [board cell]
  (let
   [mirror (= 0 (rand-int 2))
    north*? north?
    north-of* north-of
    west*? (if mirror east? west?)
    west-of* (if mirror east-of west-of)
    east*? (if mirror west? east?)
    east-of* (if mirror west-of east-of)
    northwest*? (if mirror northeast? northwest?)
    northwest-of* (if mirror northeast-of northwest-of)
    northeast*? (if mirror northwest? northeast?)
    northeast-of* (if mirror northwest-of northeast-of)]
   (cond
    (north*? board cell) (north-of* cell)
    (northwest*? board cell) (northwest-of* cell)
    (northeast*? board cell) (northeast-of* cell)
    (west*? board cell) (west-of* cell)
    (east*? board cell) (east-of* cell)
    :else cell))))
(defn make-whereto-keepright
 [west?
  northwest?
  north?
  northeast?
  east?]
 (fn [board cell]
  (let
   [mirror (= 0 (rand-int 2))
    northeast*? northeast?
    northeast-of* northeast-of
    northwest*? northwest?
    northwest-of* northwest-of
    west*? west?
    west-of* west-of
    east*? (if mirror north? east?)
    east-of* (if mirror north-of east-of)
    north*? (if mirror east? north?)
    north-of* (if mirror east-of north-of)]
   (cond
    (northeast*? board cell) (northeast-of* cell)
    (north*? board cell) (north-of* cell)
    (east*? board cell) (east-of* cell)
    (northwest*? board cell) (northwest-of* cell)
    (west*? board cell) (west-of* cell)
    :else cell))))
; this one follows keepright ratio of the time else falls back to wherever
(defn make-whereto-halfassed
 [ratio
  west?
  northwest?
  north?
  northeast?
  east?]
 (let
  [keepright (make-whereto-keepright
              west? northwest? north? northeast? east?)
   wherever (make-whereto-wherever
             west? northwest? north? northeast? east?)]
  (fn [board cell]
   (if
    (> (rand) ratio)
    (keepright board cell)
    (wherever board cell)))))
; now bring everything together.
(defn make-ticker [whereto]
 (fn [board x]
  (let
   [cell (first x)
    car (second x)
    go-time? (and (wants-to-advance? car)
                 (allowed-to-advance? car))
    cell* (if go-time? (whereto board cell) cell)
    crash (calc-crash board cell)]
   (vector
    cell*
    (assoc car
     :move-timer (next-move-timer car)
     :delay-timer (calc-delay-timer car crash)
     :mood (calc-mood car cell cell*)
     :prev-pos cell
     :stats (assoc (or (:stats car) {})
             :ticks (next-tick car)
             :holdups (calc-holdup car cell cell*)
             :worst-crash (calc-worst-crash car crash)
             :crashes (calc-crash-count car crash)))))))
