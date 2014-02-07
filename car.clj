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
; [x y]: {:speed n1,               ; the car will move every nth turn
;         :move-timer n2,          ; records n turns since the last move
;         :delay-timer n3,         ; unable to move for n turns.
;         :stats {:ticks n4,       ; the stats are pretty much self-
;                 :crashes n5,     ; explanatory.
;                 :holdups n6}}    ;

; first, some abstractions that will clarify later logic
(defn next-move-timer [car]
 (mod (inc (:move-timer car)) (:speed car)))
(defn calc-delay-timer [car crash]
 (if (zero? crash)
  (max 0 (dec (:delay-timer car)))
  (+ crash (:delay-timer car))))
(defn calc-crash-count [car crash]
 (+ (:crashes (:stats car)) (if (zero? crash) 0 1)))
(defn next-tick [car]
 (inc (:ticks (:stats car))))
(defn held-up? [before after]
 (= before after))
(defn wants-to-advance? [car]
 (= 0
  (mod
   (:move-timer car)
   (:speed car))))
(defn allowed-to-advance? [car]
 (= 0 (:delay-timer car)))
(defn calc-holdup [car before after]
 (if
  (and
   (wants-to-advance? car)
   (held-up? before after))
  (inc (:holdups (:stats car)))
  (:holdups (:stats car))))
; these aren't strictly necessary, but abstracting them helps readability
(defn west-of [cell]
 (vector
  (dec (first cell))
  (second cell)))
(defn northwest-of  [cell]
 (vector
  (dec (first cell))
  (inc (second cell))))
(defn north-of [cell]
 (vector
  (first cell)
  (inc (second cell))))
(defn northeast-of  [cell]
 (vector
  (inc (first cell))
  (inc (second cell))))
(defn east-of  [cell]
 (vector
  (inc (first cell))
  (second cell)))
; NOTE: these aren't used anywhere yet and if they don't find use, CULL!
(defn cars-vertical [board cell]
 (filter #(not (nil? %)) (get board (north-of cell))))
(defn cars-diagonal [board cell]
 (filter (partial contains? #{(northeast-of cell)
                              (northwest-of cell)}) (keys board)))
(defn cars-horizontal [board cell]
 (filter (partial contains? #{(east-of cell)
                              (west-of cell)}) (keys board)))
(defn cars-adjacent [board cell]
 (concat
  (cars-vertical board cell)
  (cars-diagonal board cell)
  (cars-horizontal board cell)))
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
; local traffic laws, i.e. keep right or whatever
(defn make-whereto-keep-right
 [west?
  northwest?
  north?
  northeast?
  east?]
 (fn [board cell]
  (cond
   (northeast? board cell) (northeast-of cell)
   (east? board cell) (east-of  cell)
   (north? board cell) (north-of cell)
   (northwest? board cell) (northwest-of cell)
   (west? board cell) (west-of cell)
   :else cell)))
; for the symmetric version, a choice must be made if two equivalent paths
; are available. left and right are randomly flipped on each decision
; to make sure this rule doesn't degenerate to keep-right
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
    west*? (if mirror west? east?)
    west-of* (if mirror west-of east-of )
    east*? (if mirror east? west?)
    east-of* (if mirror east-of  west-of)
    northwest*? (if mirror northwest? northeast?)
    northwest-of* (if mirror northwest-of  northeast-of)
    northeast*? (if mirror northeast? northwest?)
    northeast-of* (if mirror northeast-of  northwest-of)]
   (cond
    (north*? board cell) (north-of* cell)
    (northwest*? board cell) (northwest-of* cell)
    (northeast*? board cell) (northeast-of* cell)
    (west*? board cell) (west-of* cell)
    (east*? board cell) (east-of* cell)
    :else cell))))
; now bring everything together.
(defn make-tick [whereto]
 (fn [board x]
  (let
   [cell (first x)
    car (second x)
    go-time? (and (wants-to-advance? car)
                 (allowed-to-advance? car))
    cell* (if go-time? (whereto board cell) cell)
    crash 0]
   (vector
    cell*
    (assoc car
     :move-timer (next-move-timer car)
     :delay-timer (calc-delay-timer car crash)
     :stats (assoc (:stats car)
             :ticks (next-tick car)
             :holdups (calc-holdup car cell cell*)
             :crashes (calc-crash-count car crash)))))))
