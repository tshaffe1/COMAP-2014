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

; each car is a hash-map of position (as a vector) to another hashmap.
; [x y]: {:inv-speed n,      ; the car will move every nth turn
;         :move-timer n,     ; records n turns since the last move
;         :delay-timer n}    ; unable to move for n turns.
;                            ; this one is checked before :inf-speed

; first, some abstractions that will clarify later logic
(defn wants-to-advance? [car]
 (= 0
  (mod
   (get car :move-timer)
   (get car :inv-speed))))
(defn allowed-to-advance? [car]
 (= 0 (get car :delay-timer)))
(defn go-west [cell]
 (vector
  (dec (first cell))
  (second cell)))
(defn go-northwest [cell]
 (vector
  (dec (first cell))
  (inc (second cell))))
(defn go-north [cell]
 (vector
  (first cell)
  (inc (second cell))))
(defn go-northeast [cell]
 (vector
  (inc (first cell))
  (inc (second cell))))
(defn go-east [cell]
 (vector
  (inc (first cell))
  (second cell)))
; the following functions generate functions that check the availabilities
; of neighboring cells based on board dimensions.
(defn make-west-free [width height]
 (fn [board cell]
  (and
   (> (first cell) 0)
   (not
    (contains?
     board
     (go-west cell))))))
(defn make-northwest-free [width height]
 (fn [board cell]
  (and
   (< (second cell) height)
   (> (first cell) 0)
   (not
    (contains?
     board
     (go-northwest cell))))))
(defn make-north-free [width height]
 (fn [board cell]
  (and
   (< (second cell) height)
   (not
    (contains?
     board
     (go-north cell))))))
(defn make-northeast-free [width height]
 (fn [board cell]
  (and
   (< (second cell) height)
   (< (first cell) (dec width))
   (not
    (contains?
     board
     (go-northeast cell))))))
(defn make-east-free [width height]
 (fn [board cell]
  (and
   (< (first cell) (dec width))
   (not
    (contains?
     board
     (go-east cell))))))
; now the heart of the problem: decide where to move next based on
; local traffic laws, i.e. keep right or whatever
(defn make-whereto-keepright
 [west?
  northwest?
  north?
  northeast?
  east?]
 (fn [board cell]
  (cond
   (northeast? board cell) (go-northeast cell)
   (east? board cell) (go-east cell)
   (north? board cell) (go-north cell)
   (northwest? board cell) (go-northwest cell)
   (west? board cell) (go-west cell)
   :else cell)))
; for the symmetric version, a choice must be made if two equivalent paths
; are available. left and right are randomly flipped on each decision
; to make sure this rule doesn't degenerate to keepright
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
    go-north* go-north
    west*? (if mirror west? east?)
    go-west* (if mirror go-west go-east)
    east*? (if mirror east? west?)
    go-east* (if mirror go-east go-west)
    northwest*? (if mirror northwest? northeast?)
    go-northwest* (if mirror go-northwest go-northeast)
    northeast*? (if mirror northeast? northwest?)
    go-northeast* (if mirror go-northeast go-northwest)]
   (cond
    (north*? board cell) (go-north* cell)
    (northwest*? board cell) (go-northwest* cell)
    (northeast*? board cell) (go-northeast* cell)
    (west*? board cell) (go-west* cell)
    (east*? board cell) (go-east* cell)
    :else cell))))
