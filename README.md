# Trie-Based Autocorrect and Intelligent Search System (Java DSA)

A high-performance dictionary search and spelling correction engine implemented in Java SE from first principles.

> **Zero Java Collections Framework Guarantee:**  
> This project contains **no imports or usage** of built-in Java collection classes (`java.util.ArrayList`, `java.util.LinkedList`, `java.util.Queue`, `java.util.HashMap`, `java.util.HashSet`, `java.util.PriorityQueue`, `java.util.Collections`, `java.util.Arrays.sort`).  
> All dynamic arrays (`CustomList`), FIFO queues (`CustomQueue`), multi-way prefix trees (`TrieNode` / `Trie`), and sorting algorithms (`CandidateSorter` MergeSort) are written from scratch.

---

## 1. Project Overview

This project was developed as an academic case study for Data Structures and Algorithms (DSA). It demonstrates the advantages of **Prefix Trees (Tries)** over linear search for exact matching, prefix autocompletion, and Levenshtein edit-distance spelling correction.

### Key Capabilities:
- **$\mathcal{O}(L)$ Exact Word Search:** Instant lookup depending only on word length $L$, decoupled from corpus size $N$.
- **Prefix Autocomplete:** DFS traversal over prefix subtrees to collect matching words.
- **Intelligent Autocorrection:** Wagner-Fischer Levenshtein distance dynamic programming with **Trie branch pruning**.
- **Multi-Factor Ranking:** Candidates ranked by Edit Distance (Ascending) $\to$ Corpus Frequency (Descending) $\to$ Lexicographical order.
- **Memory-Safe Deletion:** Post-order recursive backtracking that unmarks terminal flags and prunes orphan nodes without corrupting shared prefixes.
- **Empirical Benchmarking:** Compares Trie performance against a linear search baseline across $N = 1,000$ to $25,000$ words.

---

## 2. Directory Structure

```
DSA CASE STUDY/
├── bin/                          # Compiled Java bytecode
├── data/
│   ├── dictionary.txt            # Full corpus (25,000 words with frequencies)
│   └── sample_words.txt          # Curated test corpus (62 words with frequencies)
├── docs/
│   ├── CASE_STUDY_REPORT.md      # Full 18-section academic case study report
│   └── PRESENTATION_OUTLINE.md   # Slide-by-slide defense guide with viva Q&A
├── src/
│   ├── Main.java                 # Interactive terminal console driver
│   ├── benchmark/
│   │   └── PerformanceBenchmark.java # Empirical benchmark harness (Trie vs Linear)
│   ├── core/
│   │   ├── Candidate.java        # Record holding (word, editDistance, frequency)
│   │   ├── CandidateSorter.java  # Custom MergeSort algorithm
│   │   ├── LinearDictionary.java # Baseline comparison system
│   │   └── SpellChecker.java     # System coordinator and file loader
│   ├── datastructures/
│   │   ├── CustomList.java       # Custom resizable dynamic array
│   │   ├── CustomQueue.java      # Custom pointer-based FIFO queue
│   │   ├── TrieNode.java         # 26-way prefix tree node
│   │   └── Trie.java             # Core Trie algorithms
│   └── test/
│       └── TestCaseRunner.java   # 10 automated test suites (53 assertions)
└── README.md
```

---

## 3. How to Compile and Run

### Prerequisites
- Java JDK 11 or higher (OpenJDK, Corretto, Oracle JDK).

### Compilation
From the project root directory, compile all source files into the `bin/` directory:

```bash
mkdir -p bin
javac -d bin src/datastructures/*.java src/core/*.java src/benchmark/*.java src/test/*.java src/Main.java
```

### 1. Run Interactive Console Application
```bash
java -cp bin Main
```

### 2. Run Automated Test Suite (10 Suites / 53 Assertions)
```bash
java -cp bin test.TestCaseRunner
```

### 3. Run Empirical Performance Benchmark
```bash
java -cp bin benchmark.PerformanceBenchmark data/dictionary.txt
```

---

## 4. Sample Console Interactions

### Autocomplete:
```text
Enter your choice [0-9]: 2
Enter prefix: prog
Maximum suggestions to display [default 5]: 5

Autocomplete suggestions for prefix 'prog' [Time: 499.583 µs]:
  1. program            [Corpus Frequency: 9500]
  2. programming        [Corpus Frequency: 8200]
  3. programmer         [Corpus Frequency: 6100]
  4. progress           [Corpus Frequency: 5400]
  5. progressive        [Corpus Frequency: 3100]
```

### Intelligent Autocorrect:
```text
Enter your choice [0-9]: 3
Enter misspelled word: programing
Maximum edit distance allowed [1 or 2, default 2]: 1

Autocorrect suggestions for 'programing' (max edit dist = 1) [Time: 108.708 µs]:
  1. programming        (Edit Dist: 1, Frequency: 8200)
```

### Exact Search:
```text
Enter your choice [0-9]: 1
Enter word to search: java
[✓] Exact match FOUND: 'java' (Corpus Frequency: 9900) [Time: 11.791 µs]
```

### Absent Word with Suggestions:
```text
Enter your choice [0-9]: 1
Enter word to search: xyzabc
[✗] Word 'xyzabc' NOT FOUND in dictionary. [Time: 1.041 µs]
    Checking for spelling corrections...
    No close suggestions found within edit distance 2.
```

---

## 5. Measured Benchmark Summary ($N = 25,000$ Words)

| Operation | Trie Latency | Linear Search Latency | Speedup Factor |
| :--- | :---: | :---: | :---: |
| **Exact Search (Hit)** | **0.025 $\mu s$** | 4.995 $\mu s$ | **197.8×** |
| **Exact Search (Miss)** | **0.033 $\mu s$** | 41.550 $\mu s$ | **1,247.8×** |
| **Prefix Autocomplete** | **1.526 $\mu s$** | 60.875 $\mu s$ | **39.9×** |
| **Fuzzy Autocorrect ($d \le 1$)** | **4.580 $\mu s$** | 2,711.347 $\mu s$ | **592.0×** |

For the complete technical report, proofs, recurrence relations, and diagrams, see [docs/CASE_STUDY_REPORT.md](docs/CASE_STUDY_REPORT.md).  
For slide presentation notes and defense viva questions, see [docs/PRESENTATION_OUTLINE.md](docs/PRESENTATION_OUTLINE.md).

