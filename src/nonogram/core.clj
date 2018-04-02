(ns nonogram.core)


(defn transpose
  "Transpose a sequence of sequences."
  [rows]
  (apply map vector rows))

(defn sum
  "Add all numbers in a sequence."
  [a-seq]
  (reduce + a-seq))

(defn empty-board [x-size y-size]
  "Create a x-size by y-size empty board.
  Nil is unknown. 0 is blank. 1 is filled."
  (repeat y-size (repeat x-size nil)))

(defn get-hint
  "Create a nonogram hint for a row with no unknowns (no nils)."
  [row]
  (remove zero? (map sum (partition-by identity row))))

(defn valid-row? [hint row]
  "Returns true if the row satisfies the hint."
  (and (= row (remove nil? row))
       (= hint (get-hint row))))

(defn valid-solution? [hints board]
  "Returns true if all rows and columns satisfy their hints."
  (and (every? true? (map valid-row? (second hints) board))
       (every? true? (map valid-row? (first hints) (transpose board)))))

(defn min-size
  "Calculate the minimum amount of space
  a row needs to satisfy the hint."
  [hint]
  (let [num-ones (sum hint)
        num-zeros (max 0 (dec (count hint)))]
    (+ num-ones num-zeros)))

(defn lazy-pad
  "Create a lazy sequence where the initial sequence has an
  infinite number of pad-values appended to the end of it."
  [pad-value a-seq]
  (concat a-seq (repeat pad-value)))

(defn row-permutations [hint size]
  "Generate all row permutations for the given hint and size.
  Not all results are valid for the hint.
  Not all results are have the right size (need to be right padded with zeros)."
  (if (empty? hint)
    '(())
    (let [max-size (- size (min-size (rest hint)))
          max-zeros (- max-size (first hint))]
      (for [head (take (inc max-zeros) (iterate #(conj % 0) (repeat (first hint) 1)))
            tail (row-permutations (rest hint) (- size (count head)))]
        (concat head tail)))))

(defn candidate-rows [hint size]
  "Generate all valid candidates for the given hint."
  (->>
    (row-permutations hint size)
    (map #(take size (lazy-pad 0 %)))
    (filter #(valid-row? hint %))))

(defn child-row?
  "Returns true if row has the same values as existing.
  Nils in the existing row are treated as wildcards."
  [existing row]
  (let [same? (fn [[a b]] (if (nil? a) true (= a b)))]
    (every? same? (map vector existing row))))

(defn common-row
  "Create a row where each cell is either a value or nil (unknown).
  A cell has a value if all rows have the same value at that position."
  [rows]
  (let [common-element (fn [row] (if (apply = row) (first row) nil))]
    (map common-element (transpose rows))))

(def candidate-rows-memo (memoize candidate-rows))
(defn solve-rows
  "For each row find the common row of all its valid candidates."
  [hints board]
  (let [size (count (first board))]
    (for [[hint row] (map vector hints board)]
      (->>
        (candidate-rows-memo hint size)
        (filter #(child-row? row %))
        (common-row)))))

(defn solve [hints]
  "Solve a nonogram that has a unique solution.
  Hints is a list of column hints followed by row hints.

  Example: hints = [[[2] [] []] [[1] [1]]]
  +-----------+
  |   |   |   |  1
  ----+---+----
  |   |   |   |  1
  +-----------+
    2   0   0

  Solution: [[1 0 0] [1 0 0]]
  +-----------+
  | 1 | 0 | 0 |  1
  ----+---+----
  | 1 | 0 | 0 |  1
  +-----------+
    2   0   0
  "
  (let [x-size (count (first hints))
        y-size (count (second hints))]
    (loop [board (empty-board x-size y-size)]
      (if (valid-solution? hints board)
        board
        (let [next-board (->>
                           board
                           (solve-rows (second hints))
                           transpose
                           (solve-rows (first hints))
                           transpose)]
          (if (= board next-board)
            board  ; No progress, unsolvable with this strategy
            (recur next-board)))))))
