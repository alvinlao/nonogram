# Nonogram Solver
My Clojure nonogram solver.

# Strategy
Going row by row, then column by column, set cells that must have a value (all valid possibilities have that value at that cell). Repeat until solved.

# Usage
`lein run resources/1.txt`
