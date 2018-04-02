(ns nonogram.io
  (:require [clojure.java.io :refer [reader]]))


(defn read-hints [filename]
  (with-open [input (java.io.PushbackReader. (reader filename))]
    (read input)))

(defn format-cell [cell]
  (if (zero? cell)
    " "
    "█"))

(defn format-row [row]
  (->>
    row
    (map format-cell)
    (reduce str)))

(defn draw [board]
  (doall
    (->>
      board
      (map format-row)
      (map println))))
