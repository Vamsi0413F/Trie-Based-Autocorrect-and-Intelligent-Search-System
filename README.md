# Trie-Based Autocorrect and Intelligent Search System (Java DSA)

A high-performance dictionary search and spelling correction engine implemented in Java SE from first principles.

> **Zero Java Collections Framework Guarantee:**  
> This project contains **no imports or usage** of built-in Java collection classes (`java.util.ArrayList`, `java.util.LinkedList`, `java.util.Queue`, `java.util.HashMap`, `java.util.HashSet`, `java.util.PriorityQueue`, `java.util.Collections`, `java.util.Arrays.sort`) within the custom DSA core.  
> All dynamic arrays (`CustomList`), FIFO queues (`CustomQueue`), bounded min-heaps (`CustomBoundedMinHeap`), multi-way prefix trees (`TrieNode` / `Trie`), and sorting algorithms (`CandidateSorter` MergeSort) are written completely from scratch.

---

## 1. Project Overview

This project was developed as an academic case study for Data Structures and Algorithms (DSA). It demonstrates the advantages and trade-offs of **Prefix Trees (Tries)** compared to **Hash Tables**, **Sorted Arrays (Binary Search)**, and **Linear Search** for exact matching, prefix autocompletion, dynamic insertion/deletion, and Levenshtein edit-distance spelling correction.

### Key Capabilities:
- **$\mathcal{O}(L)$ Exact Word Search:** Instant lookup depending solely on word length $L$, decoupled from corpus size $N$.
- **Prefix Autocomplete with Bounded Min-Heap:** DFS traversal with a custom bounded min-heap maintaining Top-$K$ candidates in $\mathcal{O}(M \log K)$ time and $\mathcal{O}(K)$ auxiliary memory.
- **Level-Order Autocomplete:** BFS traversal using a custom pointer-based queue (`CustomQueue`) for shortest-word exploration.
- **Intelligent Autocorrection:** Wagner-Fischer Levenshtein distance dynamic programming with **Trie branch pruning**, pruning subtrees when $\min(\text{row}) > d_{\max}$.
- **Deterministic Multi-Factor Ranking:** Candidates ranked by Edit Distance (Ascending) $\to$ Corpus Frequency (Descending) $\to$ Lexicographical order (Ascending).
- **Memory-Safe Deletion:** Post-order recursive backtracking that unmarks terminal flags and prunes orphan nodes without corrupting shared prefixes.
- **Empirical 4-Structure Benchmark:** Evaluates Trie, Hash Table (`HashMap`), Sorted Array (`binarySearch`), and Linear Scan (`LinearDictionary`) across $N = 1,000$ to $25,000$ words.

---

## 2. Directory Structure

```
DSA CASE STUDY/
├── bin/                              # Compiled Java bytecode
├── data/
│   ├── dictionary.txt                # Full corpus (25,000 words with frequencies)
│   ├── sample_words.txt              # Curated test corpus (62 words with frequencies)
│   └── words_alpha.txt               # Large English word list (370,105 words, public domain)
├── docs/
│   ├── CASE_STUDY_REPORT.md          # Full 18-section academic case study report (with Criteria A, B, D)
│   ├── PRESENTATION_OUTLINE.md       # Slide presentation guide with 15 Faculty Viva Q&As
│   └── VERIFICATION_REPORT.md        # Audit verification log (101/101 tests, benchmark outputs)
├── src/
│   ├── Main.java                     # Interactive terminal console driver & CLI entry point
│   ├── benchmark/
│   │   └── PerformanceBenchmark.java # 4-structure benchmark harness (Trie, Hash, Sort, Linear)
│   ├── core/
│   │   ├── Candidate.java            # Record holding (word, editDistance, frequency)
│   │   ├── CandidateSorter.java      # Custom stable MergeSort implementation
│   │   ├── LinearDictionary.java     # Unindexed baseline comparison system
│   │   └── SpellChecker.java         # System coordinator, path resolver, and file loader
│   ├── datastructures/
│   │   ├── CustomBoundedMinHeap.java # Custom array-based bounded min-heap for Top-K
│   │   ├── CustomList.java           # Custom resizable dynamic array (2x geometric growth)
│   │   ├── CustomQueue.java          # Custom pointer-based FIFO queue (BFS traversal)
│   │   ├── TrieNode.java             # 26-way prefix tree node
│   │   └── Trie.java                 # Core Trie algorithms (DFS, BFS, Hanov DP, pruning)
│   └── test/
│       └── TestCaseRunner.java       # Comprehensive test harness (13 suites / 101 assertions)
└── README.md
```

### Available Dictionaries & Attribution

| Dictionary File | Word Count | Description & Licensing |
| :--- | :---: | :--- |
| **`data/dictionary.txt`** | **25,000 words** | Default benchmark and production dictionary with word popularity frequencies. |
| **`data/sample_words.txt`** | **62 words** | Curated test corpus with controlled prefixes (`prog`, `care`, `java`) for transparent live demos and unit tests. |
| **`data/words_alpha.txt`** | **370,105 words** | Large alphabetic English word list sourced from [`dwyl/english-words`](https://github.com/dwyl/english-words) (derived from Infochimps simple English word list). Released under **The Unlicense (Public Domain)**. *Note: This dataset contains words without frequency weighting (assigned frequency=1) and should not be confused with the complete Oxford English Dictionary.* |

---

## 3. How to Compile and Run

### Prerequisites
- Java JDK 11 or higher (OpenJDK, Amazon Corretto, Oracle JDK).

### Compilation
From the project root directory (`DSA CASE STUDY/`), compile all source files into the `bin/` directory:

```bash
mkdir -p bin
javac -d bin src/datastructures/*.java src/core/*.java src/benchmark/*.java src/test/*.java src/Main.java
```

### 1. Run Automated Test Suite (13 Suites / 101 Assertions)
```bash
java -cp bin test.TestCaseRunner
```

### 2. Run Empirical Performance Benchmark (4 Data Structures)
```bash
# Standard benchmark on 25,000-word corpus (evaluates N = 1,000, 10,000, and 25,000)
java -cp bin benchmark.PerformanceBenchmark data/dictionary.txt

# Extended large-scale benchmark on 370,105-word corpus (evaluates N = 1k, 10k, 25k, 100k, and 370k)
java -Xmx1024m -cp bin benchmark.PerformanceBenchmark data/words_alpha.txt
```

### 3. Launch Interactive Console Application
```bash
# Default mode (data/dictionary.txt ~ 25,000 words)
java -cp bin Main

# Launch directly with the large 370,105-word dictionary:
java -Xmx1024m -cp bin Main data/words_alpha.txt

# Launch with curated sample dictionary (62 words):
java -cp bin Main data/sample_words.txt
```

---

## 4. Live Demo and Faculty Test Checklist (< 10 Minutes)

Follow this step-by-step checklist during live faculty examination to verify every core algorithm, data structure, edge case, and benchmark claim in under 10 minutes.

### Step 1: Run Full Test Suite (30 seconds)
Execute the automated test suite to prove system correctness:
```bash
java -cp bin test.TestCaseRunner
```
**Expected Output:**
```text
================================================================================
                 COMMENCING AUTOMATED DSA TEST SUITE VERIFICATION               
================================================================================
[Suite 1] CustomList Dynamic Array Verification: PASS (6/6)
[Suite 2] CustomQueue FIFO Verification: PASS (8/8)
[Suite 3] CandidateSorter Custom MergeSort Verification: PASS (4/4)
[Suite 4] Successful and Unsuccessful Exact Searches: PASS (9/9)
[Suite 5] Words with Shared Prefixes: PASS (6/6)
[Suite 6] Prefix Autocomplete: Valid, No-Match, Equal to Word: PASS (8/8)
[Suite 7] Custom Bounded Min-Heap & Top-K Parameter Boundaries: PASS (15/15)
[Suite 8] Autocorrect: Typo Classes & Edit-Distance Boundaries: PASS (10/10)
[Suite 9] Dynamic Insertion, Verification, and Duplicate Handling: PASS (9/9)
[Suite 10] Deletion: Shared Prefixes, Longer Words, & Nonexistent Targets: PASS (11/11)
[Suite 11] Dictionary File Loading, Path Resolution, & Switch Preservation: PASS (6/6)
[Suite 12] Repeated Insert/Delete Stress Cycles (50 Rounds): PASS (4/4)
[Suite 13] Cross-Check Verification: Trie vs. LinearDictionary Baseline: PASS (5/5)
================================================================================
TEST SUITE SUMMARY: 101 / 101 TESTS PASSED (100.0%)
================================================================================
```

---

### Step 2: Launch Console App with Sample Dataset (1 minute)
Launch the application pointing to the curated 62-word test corpus:
```bash
java -cp bin Main data/sample_words.txt
```
**Verification:**
- Active dictionary loaded: 62 words.
- Node count: 184 nodes.

---

### Step 3: Prefix Autocomplete & Bounded Min-Heap Demo (1.5 minutes)
1. Select option `[2] Autocomplete (Prefix Search)`.
2. Enter prefix: `prog`.
3. Enter maximum suggestions: `3`.
**Actual Output:**
```text
Autocomplete suggestions for prefix 'prog' [Time: ~0.5 µs]:
  1. program            [Corpus Frequency: 9500]
  2. programming        [Corpus Frequency: 8200]
  3. programmer         [Corpus Frequency: 6100]
```
**Faculty Viva Point:** The suggestions are extracted via `CustomBoundedMinHeap` in $\mathcal{O}(M \log K)$ without sorting the entire match set. Notice frequency descending order.

---

### Step 4: Intelligent Autocorrect & Branch Pruning Demo (2 minutes)
1. Select option `[3] Autocorrect (Fuzzy Search)`.
2. Enter misspelled word: `programing` (single deletion typo).
3. Maximum edit distance: `1`.
**Actual Output:**
```text
Autocorrect suggestions for 'programing' (max edit dist = 1) [Time: ~2.1 µs]:
  1. programming        (Edit Dist: 1, Frequency: 8200)
```
4. Repeat option `[3]` with a substitution typo: `progran` at distance `1`.
**Actual Output:**
```text
Autocorrect suggestions for 'progran' (max edit dist = 1):
  1. program            (Edit Dist: 1, Frequency: 9500)
```
**Faculty Viva Point:** Explain Steve Hanov's algorithm. When evaluating `prog...`, branches like `b...`, `c...`, `z...` are pruned at depth 1 because their minimum Levenshtein row value exceeds $d_{\max}=1$.

---

### Step 5: Memory-Safe Deletion with Shared Prefix (2 minutes)
1. Select option `[1] Exact Word Search` $\to$ Enter `cat` $\to$ `[✓] Exact match FOUND`.
2. Select option `[1] Exact Word Search` $\to$ Enter `caterpillar` $\to$ `[✓] Exact match FOUND`.
3. Select option `[5] Delete Word` $\to$ Enter `cat`.
**Actual Output:**
```text
[✓] Word 'cat' successfully deleted from dictionary.
```
4. Verify `cat` is gone: Option `[1]` $\to$ `cat` $\to$ `[✗] Word 'cat' NOT FOUND`.
5. Verify `caterpillar` remains intact: Option `[1]` $\to$ `caterpillar` $\to$ `[✓] Exact match FOUND`.
**Faculty Viva Point:** Recursive post-order backtracking unmarks `isEndOfWord` at `'t'`. Because `'t'` has a child `'e'` leading to `"caterpillar"`, pruning halts immediately, preserving the longer word!

---

### Step 6: Input Validation & Error Handling (1 minute)
1. Option `[1]` $\to$ Enter `java 123` (invalid non-letter input).
**Actual Output:**
```text
[!] Invalid input: Word must contain only alphabetical characters (a-z, A-Z).
```
2. Option `[7] Switch Dictionary` $\to$ Enter `data/non_existent.txt`.
**Actual Output:**
```text
[!] Error switching dictionary: Dictionary file not found: data/non_existent.txt
[!] Preserving active dictionary (Word count: 61, Nodes: 184).
```

---

### Step 7: Run 4-Structure Performance Benchmark (2 minutes)
Exit console (`[0] Exit`) and run the full 25,000-word benchmark:
```bash
java -cp bin benchmark.PerformanceBenchmark data/dictionary.txt
```
**Faculty Viva Point:** Review the comparative table showing where each structure excels:
- **Exact Search:** Hash Table is fastest ($0.010\,\mu s$), Trie is close ($0.033\,\mu s$), Linear scan lags ($4.652\,\mu s$).
- **Prefix Autocomplete:** Trie ($0.507\,\mu s$) and Sorted Array ($0.235\,\mu s$) outperform Hash Table ($130.325\,\mu s$) by over $250\times$.
- **Dynamic Insertion/Deletion:** Trie avoids the $\mathcal{O}(N)$ element shifting penalty of Sorted Array ($0.277\,\mu s$ vs $1.190\,\mu s$).
- **Fuzzy Autocorrect:** Trie achieves a **$1,345\times$ speedup** over Linear Scan and Hash Table by pruning 99.4% of the node space ($S=134$ nodes explored vs $N=25,000$ words).

---

## 5. Measured Empirical Benchmark Summaries

The benchmarks below reflect real measurements on a 64-bit JVM (OpenJDK Corretto 11.0.31 LTS on macOS arm64 across 5 repetitions, reporting Median):

### Corpus 1: Standard Dictionary ($N = 25,000$ Words, Frequencies Included)

| Operation | Trie (Custom) | Hash Table (`HashMap`) | Sorted Array (`binarySearch`) | Linear Scan (`LinearDictionary`) | Trie vs. Baseline Speedup / Analysis |
| :--- | :---: | :---: | :---: | :---: | :--- |
| **Exact Search (Hit)** | $0.033\,\mu s$ | **$0.010\,\mu s$** | $0.042\,\mu s$ | $4.652\,\mu s$ | **141.0× vs Linear** *(Hash table wins on isolated hit)* |
| **Exact Search (Miss)** | $0.038\,\mu s$ | **$0.004\,\mu s$** | $0.034\,\mu s$ | $38.778\,\mu s$ | **1,020.5× vs Linear** *(Early exit on missing prefix)* |
| **Prefix Auto (`'pro'`)** | $0.507\,\mu s$ | $130.325\,\mu s$ | **$0.235\,\mu s$** | $53.097\,\mu s$ | **257.1× vs Hash Table** *(Hash table must scan all $N$)* |
| **Prefix Auto (`'algo'`)** | **$1.530\,\mu s$** | $142.673\,\mu s$ | $1.551\,\mu s$ | $61.786\,\mu s$ | **93.2× vs Hash Table** |
| **Insert + Delete (Shift Cost)** | $0.277\,\mu s$ | **$0.028\,\mu s$** | $1.190\,\mu s$ ($\mathcal{O}(N)$ shift) | $5.652\,\mu s$ | **4.3× vs Sorted Array** *(Array incurs shift penalty)* |
| **Fuzzy Autocorrect ($d=1$)** | **$2.100\,\mu s$** | $3,171.192\,\mu s$ | $2,891.771\,\mu s$ | $2,825.750\,\mu s$ | **1,345.6× vs Linear / 1,510.1× vs Hash Table** |
| **Nodes / Words Explored ($S$)** | **134 nodes** | 25,000 words | 25,000 words | 25,000 words | **186.6× search space reduction** |
| **Memory Footprint (Est.)** | 17.23 MB | 1.34 MB | **0.29 MB** | 0.29 MB | Space-Time Trade-off ($224$ B/node) |

### Corpus 2: Large English Vocabulary ($N = 370,105$ Words, `words_alpha.txt`)

| Operation | Trie (Custom) | Hash Table (`HashMap`) | Sorted Array (`binarySearch`) | Linear Scan (`LinearDictionary`) | Trie vs. Baseline Speedup / Analysis |
| :--- | :---: | :---: | :---: | :---: | :--- |
| **Exact Search (Hit)** | $0.028\,\mu s$ | **$0.004\,\mu s$** | $0.043\,\mu s$ | $286.897\,\mu s$ | **10,246× vs Linear** *(Trie latency remains flat $\mathcal{O}(L)$)* |
| **Exact Search (Miss)** | $0.033\,\mu s$ | **$0.001\,\mu s$** | $0.033\,\mu s$ | $599.445\,\mu s$ | **18,165× vs Linear** *(Instant miss detection at root/first level)* |
| **Prefix Auto (`'algo'`)** | **$1.979\,\mu s$** | $4,682.466\,\mu s$ | **$1.635\,\mu s$** | $629.616\,\mu s$ | **2,366× vs Hash Table** *(Hash table must scan all 370k entries)* |
| **Insert + Delete (Shift Cost)** | $0.177\,\mu s$ | **$0.013\,\mu s$** | **$92.753\,\mu s$** ($\mathcal{O}(N)$ shift) | $287.897\,\mu s$ | **524.0× vs Sorted Array** *(Array shifts hundreds of thousands of slots)* |
| **Fuzzy Autocorrect ($d=1$)** | **$112.111\,\mu s$** | $78,252.208\,\mu s$ (78.2 ms) | $40,161.125\,\mu s$ (40.2 ms) | $39,716.125\,\mu s$ (39.7 ms) | **354.3× vs Linear / 698.0× vs Hash Table** |
| **Nodes / Words Explored ($S$)** | **2,138 nodes** | 370,105 words | 370,105 words | 370,105 words | **173.1× search space reduction** |
| **Memory Footprint (Est.)** | 219.56 MB | 19.77 MB | **4.24 MB** | 4.24 MB | 1,027,810 Trie nodes (~622 bytes/word) |

---

## 6. Documentation Index

- **Academic Case Study Report:** [docs/CASE_STUDY_REPORT.md](docs/CASE_STUDY_REPORT.md) — Comprehensive 18-section report featuring data structure trade-off justifications, 10 implementation design choices, and formal recurrence proofs.
- **Presentation Defense Outline & Viva Guide:** [docs/PRESENTATION_OUTLINE.md](docs/PRESENTATION_OUTLINE.md) — Slide walkthrough and 15 faculty examination questions with model answers.
- **Verification & Audit Report:** [docs/VERIFICATION_REPORT.md](docs/VERIFICATION_REPORT.md) — Empirical execution logs, test outputs, and compliance records.


