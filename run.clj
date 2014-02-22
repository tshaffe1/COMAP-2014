; copyright 2014 Tim Shaffer

;;:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
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

(ns run
 (:load "monte-carlo"
        "gameboard"))
(def monochrome gameboard/pbm)
(def colored (partial gameboard/ppm gameboard/rainbow))
(def trial-count 64)
(def road-length 256)
(def short-road-length 128)
(def tiny-road-length 32)
(defn many-cars [lanes] (/ (* road-length lanes) 4))
(defn some-cars [lanes] (/ (* road-length lanes) 8))
(defn few-cars [lanes] (/ (* road-length lanes) 16))
(defn fixed-cars [lanes] 128)
(def normal-speed {:scale 4 :offset 3})
(def overposted-speed {:scale 6 :offset 1})
(def underposted-speed {:scale 6 :offset 3})
(def fixed-speed 4)
(def bad-roads 2)
(def good-roads 0)
(def wherever gameboard/make-ticker-wherever)
(def keepright gameboard/make-ticker-keepright)
(def halfassed gameboard/make-ticker-halfassed)
(defn go
 [filename lanes rule car-count base-mood speed-limit]
 (println filename)
 (spit filename
  (monte-carlo/trial-summary
   (monte-carlo/trial
    trial-count
    road-length
    (gameboard/make-loop
     (rule
      lanes
      road-length)
     road-length)
    (car-count lanes)
    [{:speed speed-limit
      :min-mood base-mood
      :mood {:scale 8}
      :initial-x {:scale lanes}
      :initial-y {:scale road-length}}]))))
(defn pix
 [path-format picture-type road-length lanes rule car-count
  base-mood speed-limit]
 (gameboard/dump-history
  (partial picture-type lanes road-length)
  path-format
  ((gameboard/make-history
    (rule
     lanes
     road-length)
    road-length)
   (monte-carlo/gen-board
    (car-count lanes)
    [{:speed speed-limit
      :min-mood base-mood
      :mood {:scale 8}
      :initial-x {:scale lanes}
      :initial-y {:scale road-length}}]))))

;(go "data/1.txt" 2 keepright many-cars good-roads normal-speed)
;(go "data/2.txt" 2 halfassed many-cars good-roads normal-speed)
;(go "data/3.txt" 2 wherever many-cars good-roads normal-speed)
;(go "data/4.txt" 2 keepright few-cars good-roads normal-speed)
;(go "data/5.txt" 2 halfassed few-cars good-roads normal-speed)
;(go "data/6.txt" 2 wherever few-cars good-roads normal-speed)
;(go "data/7.txt" 3 keepright many-cars good-roads normal-speed)
;(go "data/8.txt" 3 halfassed many-cars good-roads normal-speed)
;(go "data/9.txt" 3 wherever many-cars good-roads normal-speed)
;(go "data/10.txt" 3 keepright few-cars good-roads normal-speed)
;(go "data/11.txt" 3 halfassed few-cars good-roads normal-speed)
;(go "data/12.txt" 3 wherever few-cars good-roads normal-speed)
;(go "data/13.txt" 4 keepright many-cars good-roads normal-speed)
;(go "data/14.txt" 4 halfassed many-cars good-roads normal-speed)
;(go "data/15.txt" 4 wherever many-cars good-roads normal-speed)
;(go "data/16.txt" 4 keepright few-cars good-roads normal-speed)
;(go "data/17.txt" 4 halfassed few-cars good-roads normal-speed)
;(go "data/18.txt" 4 wherever few-cars good-roads normal-speed)
;(go "data/19.txt" 7 keepright many-cars good-roads normal-speed)
;(go "data/20.txt" 7 halfassed many-cars good-roads normal-speed)
;(go "data/21.txt" 7 wherever many-cars good-roads normal-speed)
;(go "data/22.txt" 7 keepright few-cars good-roads normal-speed)
;(go "data/23.txt" 7 halfassed few-cars good-roads normal-speed)
;(go "data/24.txt" 7 wherever few-cars good-roads normal-speed)
;(go "data/25.txt" 4 keepright some-cars good-roads normal-speed)
;(go "data/26.txt" 4 halfassed some-cars good-roads normal-speed)
;(go "data/27.txt" 4 wherever some-cars good-roads normal-speed)
;(go "data/28.txt" 4 keepright some-cars good-roads underposted-speed)
;(go "data/29.txt" 4 halfassed some-cars good-roads underposted-speed)
;(go "data/30.txt" 4 wherever some-cars good-roads underposted-speed)
;(go "data/31.txt" 4 keepright some-cars good-roads overposted-speed)
;(go "data/32.txt" 4 halfassed some-cars good-roads overposted-speed)
;(go "data/33.txt" 4 wherever some-cars good-roads overposted-speed)
;(go "data/34.txt" 2 keepright fixed-cars good-roads normal-speed)
;(go "data/35.txt" 2 halfassed fixed-cars good-roads normal-speed)
;(go "data/36.txt" 2 wherever fixed-cars good-roads normal-speed)
;(go "data/37.txt" 3 keepright fixed-cars good-roads normal-speed)
;(go "data/38.txt" 3 halfassed fixed-cars good-roads normal-speed)
;(go "data/39.txt" 3 wherever fixed-cars good-roads normal-speed)
;(go "data/40.txt" 4 keepright fixed-cars good-roads normal-speed)
;(go "data/41.txt" 4 halfassed fixed-cars good-roads normal-speed)
;(go "data/42.txt" 4 wherever fixed-cars good-roads normal-speed)
;(go "data/43.txt" 4 keepright some-cars bad-roads normal-speed)
;(go "data/44.txt" 4 halfassed some-cars bad-roads normal-speed)
;(go "data/45.txt" 4 wherever some-cars bad-roads normal-speed)

;(go "data/46.txt" 4 keepright many-cars good-roads fixed-speed)
;(go "data/47.txt" 4 halfassed many-cars good-roads fixed-speed)
;(go "data/48.txt" 4 wherever many-cars good-roads fixed-speed)
;(go "data/49.txt" 7 keepright many-cars good-roads fixed-speed)
;(go "data/50.txt" 7 halfassed many-cars good-roads fixed-speed)
;(go "data/51.txt" 7 wherever many-cars good-roads fixed-speed)
;(go "data/52.txt" 4 keepright many-cars bad-roads fixed-speed)
;(go "data/53.txt" 4 halfassed many-cars bad-roads fixed-speed)
;(go "data/54.txt" 4 wherever many-cars bad-roads fixed-speed)
;(go "data/55.txt" 7 keepright many-cars bad-roads fixed-speed)
;(go "data/56.txt" 7 halfassed many-cars bad-roads fixed-speed)
;(go "data/57.txt" 7 wherever many-cars bad-roads fixed-speed)
;(go "data/58.txt" 4 wherever
;    (fn [lanes] (/ (* road-length lanes) 2)) good-roads normal-speed)
;(go "data/59.txt" 4 wherever
;    (fn [lanes] (/ (* road-length lanes) 32)) good-roads normal-speed)
;(go "data/60.txt" 11 wherever some-cars good-roads normal-speed)
;(go "data/61.txt" 4 wherever some-cars 16 normal-speed)
;(go "data/62.txt" 4 wherever some-cars good-roads {:scale 2 :offset 1})
;(go "data/63.txt" 4 wherever some-cars good-roads {:scale 2 :offset 8})
;(go "data/64.txt" 25 wherever
;    (fn [lanes] (/ (* road-length lanes) 2)) good-roads normal-speed)

(pix "/tmp/comap/frame%05d.ppm" colored 25 4 wherever some-cars good-roads normal-speed)
