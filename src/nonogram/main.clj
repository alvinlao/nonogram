(ns nonogram.main
  (:require [nonogram.core :refer [solve]]
            [nonogram.io :refer [read-hints draw]])
  (:gen-class))


(defn -main [& args]
  (if (== (count args) 1)
    (-> (first args) read-hints solve draw)
    (println "Usage: nonogram <filename>")))
