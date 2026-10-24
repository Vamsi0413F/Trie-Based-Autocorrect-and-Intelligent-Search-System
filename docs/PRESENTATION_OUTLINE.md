# PRESENTATION OUTLINE & DEFENSE GUIDE
## Trie-Based Autocorrect and Intelligent Search System (Java DSA)

---

### Slide 1: Title & Introduction
- **Project Title:** Trie-Based Autocorrect and Intelligent Search System
- **Focus Area:** Data Structures and Algorithms (DSA) Case Study (8-Mark Academic Evaluation)
- **Tech Stack:** Pure Java SE 11+ (Engineered from first principles — zero `java.util.*` collections in core DSA)
- **Presenter / Team:** Academic DSA Case Study Group
- **Key Hook:** How modern search engines achieve sub-microsecond autocomplete and fault-tolerant spell checking across tens of thousands of words without full dictionary scanning.

---

### Slide 2: Problem Statement & Motivation
- **The Keystroke Latency Constraint:**
  - Modern interactive systems (Google search, IDE code completion, smartphone keyboards) process keystrokes in real time.
  - A user types a prefix $P$ $\to$ the engine must return ranked suggestions before the next keystroke (< 100 ms perceptual latency threshold).
  - A user types a misspelled query $Q$ $\to$ the engine must identify and rank plausible corrections without stalling the UI thread.
- **Why Naive Search Fails:**
  - Linear dictionary scanning requires $\mathcal{O}(N \cdot L)$ time per exact search and $\mathcal{O}(N \cdot L \cdot L_{\text{avg}})$ for Levenshtein distance calculations.
  - As dictionary size $N$ scales to $25,000+$ words, brute-force scanning requires seconds of compute per keystroke, causing noticeable UI freezes.

---

### Slide 3: System Architecture & First-Principles Philosophy
- **Strict Implementation Constraint:** Zero reliance on Java collections framework (`ArrayList`, `LinkedList`, `Queue`, `HashMap`, `TreeMap`, `PriorityQueue`, `Collections.sort`).
- **Core Custom Data Structures Engineered from Scratch:**
  1. `CustomList<T>`: Generic resizable dynamic array with $2\times$ geometric expansion and amortized $\mathcal{O}(1)$ insertion.
  2. `CustomQueue<T>`: Pointer-based First-In-First-Out (FIFO) linked queue for BFS level-order traversal.
  3. `CustomBoundedMinHeap`: Array-based binary min-heap for Top-$K$ candidate selection in $\mathcal{O}(M \log K)$ time and $\mathcal{O}(K)$ auxiliary space.
  4. `TrieNode`: 26-way multi-way child array with $\mathcal{O}(1)$ child tracking and memory optimization.
  5. `Trie`: Core prefix tree supporting insertion, exact search, recursive deletion, bounded DFS autocomplete, BFS autocomplete, and Steve Hanov branch-pruned DP.
  6. `CandidateSorter`: In-place stable MergeSort prioritizing Distance (ASC) $\to$ Frequency (DESC) $\to$ Lexicographical (ASC).
  7. `LinearDictionary`: Unindexed baseline to empirically measure real speedup.

---

### Slide 4: The Trie Data Structure & Node Representation
- **What is a Trie?**
  - An ordered tree data structure where keys are strings formed by traversing path edges.
  - Unlike binary search trees where nodes store whole keys, a Trie node's position in the hierarchy defines the key prefix.
  - All descendants of a given node share a common prefix.
- **Node Anatomy (`TrieNode`):**
  - `TrieNode[26] children`: Direct indexed lookup `index = c - 'a'` ($\mathcal{O}(1)$ transition).
  - `boolean isEndOfWord`: Distinguishes prefixes from complete words.
  - `int frequency`: Corpus popularity weighting for suggestion ranking.
  - `int childCount`: Tracks active outgoing edges for $\mathcal{O}(1)$ leaf identification during deletion.

---

### Slide 5: Core Trie Operations & Complexities
- **1. Word Insertion $\mathcal{O}(L)$:**
  - Walk down character by character. Create child nodes on demand.
  - Mark terminal flag `isEndOfWord = true` and record frequency at depth $L$.
- **2. Exact Word Search $\mathcal{O}(L)$:**
  - Traverse characters. If any child pointer is null, return `false` immediately.
  - Return `isEndOfWord` at terminal node. Execution time depends *only* on query length $L$, completely decoupled from corpus size $N$.
- **3. Recursive Word Deletion $\mathcal{O}(L)$:**
  - Post-order recursive backtracking.
  - Unmark terminal flag.
  - Prune orphan nodes bottom-up without damaging shared prefixes (e.g., deleting `"cat"` preserves `"caterpillar"`).

---

### Slide 6: Prefix Autocomplete: DFS vs. BFS vs. Bounded Min-Heap
- **Two Autocomplete Traversal Strategies:**
  1. **Depth-First Search (DFS):** Traverses subtrees deeply, finding all completions.
  2. **Breadth-First Search (BFS):** Traverses level-by-level using `CustomQueue`, ensuring suggestions are discovered in order of increasing word length.
- **Top-$K$ Selection Optimization:**
  - **Naive Approach:** Collect all $M$ matching words in subtree, sort via MergeSort in $\mathcal{O}(M \log M)$ time, retain top $K$.
  - **Bounded Min-Heap Approach (`CustomBoundedMinHeap`):** Maintain a min-heap of capacity $K$. When heap reaches size $K$, compare candidate against root (the current $K$-th best). If superior, replace root and sift down.
  - **Complexity:** Reduces time to $\mathcal{O}(M \log K)$ and auxiliary retained memory to $\mathcal{O}(K)$.

---

### Slide 7: Autocorrection via Levenshtein DP & Branch Pruning
- **Wagner-Fischer Recurrence Relation:**
  $$D[i, j] = \min \begin{cases}
  D[i-1, j] + 1 & \text{(Deletion)} \\
  D[i, j-1] + 1 & \text{(Insertion)} \\
  D[i-1, j-1] + [S[i-1] \ne W[j-1]] & \text{(Substitution)}
  \end{cases}$$
- **The Steve Hanov Trie Advantage:**
  - Standard dynamic programming computes an $(|Q|+1) \times (|W|+1)$ matrix independently for every word $W \in \text{Corpus}$ ($\mathcal{O}(N \cdot L \cdot L_{\text{avg}})$ total work).
  - On a Trie, common prefixes share identical DP prefix rows. The row is computed *once* per prefix node.
  - **Branch Pruning Condition:**
    $$\text{If } \min_{0 \le i \le |Q|}(D[i]) > d_{\max} \implies \text{PRUNE SUBTREE}$$
  - Discards entire subtrees containing thousands of words in a single conditional check!

---

### Slide 8: Complexity Analysis: Trie vs. Four Baselines
Highlight the theoretical contrast across all four data structures:

| Metric | Trie Engine | Hash Table (`HashMap`) | Sorted Array (`binarySearch`) | Linear Scan (`LinearDictionary`) |
| :--- | :---: | :---: | :---: | :---: |
| **Exact Search (Hit)** | $\mathcal{O}(L)$ | $\mathcal{O}(L)$ avg / $\mathcal{O}(N \cdot L)$ worst | $\mathcal{O}(L \log N)$ | $\mathcal{O}(N \cdot L)$ |
| **Exact Search (Miss)** | $\mathcal{O}(1) \text{ to } \mathcal{O}(L)$ | $\mathcal{O}(L)$ avg | $\mathcal{O}(L \log N)$ | $\mathcal{O}(N \cdot L)$ |
| **Prefix Autocomplete** | $\mathcal{O}(P + V)$ | $\mathcal{O}(N \cdot P)$ (Full Scan) | $\mathcal{O}(L \log N + M)$ | $\mathcal{O}(N \cdot P)$ (Full Scan) |
| **Top-$K$ Selection** | $\mathcal{O}(M \log K)$ (Heap) | $\mathcal{O}(M \log M)$ (Sort) | $\mathcal{O}(M)$ (Direct Slice) | $\mathcal{O}(M \log M)$ (Sort) |
| **Insert / Delete** | $\mathcal{O}(L)$ | $\mathcal{O}(L)$ avg | $\mathcal{O}(N)$ (Shift Penalty) | $\mathcal{O}(1)$ / $\mathcal{O}(N)$ |
| **Fuzzy Autocorrect** | $\mathcal{O}(S \cdot Q)$ ($S \ll N$) | $\mathcal{O}(N \cdot Q \cdot L_{\text{avg}})$ | $\mathcal{O}(N \cdot Q \cdot L_{\text{avg}})$ | $\mathcal{O}(N \cdot Q \cdot L_{\text{avg}})$ |
| **Memory Footprint** | $\mathcal{O}(V \cdot |\Sigma|)$ | $\mathcal{O}(N \cdot L_{\text{avg}})$ | $\mathcal{O}(N)$ (Contiguous Array) | $\mathcal{O}(N)$ (Contiguous Array) |

---

### Slide 9: Measured Empirical Benchmark: Scaling from 25k to 370k Words
Actual measurements collected on macOS arm64 OpenJDK 11 across 5 repetitions:

#### Scale 1: Standard Dictionary ($N = 25,000$ Words)
| Operation | Trie | Hash Table | Sorted Array | Linear Scan | Trie Speedup |
| :--- | :---: | :---: | :---: | :---: | :---: |
| **Exact Search (Hit)** | $0.033\,\mu s$ | **$0.010\,\mu s$** | $0.042\,\mu s$ | $4.652\,\mu s$ | **141.0× vs Linear** |
| **Exact Search (Miss)** | $0.038\,\mu s$ | **$0.004\,\mu s$** | $0.034\,\mu s$ | $38.778\,\mu s$ | **1,020.5× vs Linear** |
| **Prefix Auto (`'algo'`)** | **$1.530\,\mu s$** | $142.673\,\mu s$ | $1.551\,\mu s$ | $61.786\,\mu s$ | **93.2× vs Hash Table** |
| **Insert + Delete (Shift)**| $0.277\,\mu s$ | **$0.028\,\mu s$** | $1.190\,\mu s$ ($\mathcal{O}(N)$) | $5.652\,\mu s$ | **4.3× vs Sorted Array** |
| **Autocorrect ($d=1$)** | **$2.100\,\mu s$** | $3,171.192\,\mu s$ | $2,891.771\,\mu s$ | $2,825.750\,\mu s$ | **1,345.6× vs Linear** |
| **Nodes Explored ($S$)** | **134 nodes** | 25,000 words | 25,000 words | 25,000 words | **186.6× reduction** |

#### Scale 2: Large English Vocabulary ($N = 370,105$ Words, `words_alpha.txt`)
| Operation | Trie | Hash Table | Sorted Array | Linear Scan | Trie Speedup / Key Insight |
| :--- | :---: | :---: | :---: | :---: | :--- |
| **Exact Search (Hit)** | $0.028\,\mu s$ | **$0.004\,\mu s$** | $0.043\,\mu s$ | $286.897\,\mu s$ | **10,246× vs Linear** *(Flat $\mathcal{O}(L)$ latency)* |
| **Exact Search (Miss)** | $0.033\,\mu s$ | **$0.001\,\mu s$** | $0.033\,\mu s$ | $599.445\,\mu s$ | **18,165× vs Linear** *(Instant root miss)* |
| **Prefix Auto (`'algo'`)** | **$1.979\,\mu s$** | $4,682.466\,\mu s$ | **$1.635\,\mu s$** | $629.616\,\mu s$ | **2,366× vs Hash Table** *(Hash scans 370k entries)* |
| **Insert + Delete (Shift)**| $0.177\,\mu s$ | **$0.013\,\mu s$** | **$92.753\,\mu s$** | $287.897\,\mu s$ | **524.0× vs Sorted Array** *(Array shift penalty explodes)* |
| **Autocorrect ($d=1$)** | **$112.111\,\mu s$** | $78,252.208\,\mu s$ | $40,161.125\,\mu s$ | $39,716.125\,\mu s$ | **354.3× vs Linear / 698.0× vs Hash Table** |
| **Nodes Explored ($S$)** | **2,138 nodes** | 370,105 words | 370,105 words | 370,105 words | **173.1× reduction** (Prunes 99.4% of nodes) |

---

### Slide 10: Live Demonstration Walkthrough (< 10 Minutes)
1. **Automated Verification:** Run `test.TestCaseRunner` $\to$ 13 test suites, 101/101 assertions passing (100%).
2. **Autocomplete:** Query `prog` $\to$ Returns `program` (9500), `programming` (8200), `programmer` (6100) ranked by frequency via Bounded Min-Heap.
3. **Autocorrect:** Query `programing` (single deletion typo) $\to$ Instantly corrects to `programming` ($d=1$) in $2.1\,\mu s$.
4. **Memory-Safe Deletion:** Delete `cat` while `caterpillar` exists $\to$ `cat` removed, `caterpillar` preserved.
5. **Input Validation:** Reject strings with digits, spaces, or symbols; handle empty queries gracefully.
6. **Dictionary Switching:** Graceful fallback if switching to an invalid path; active state preserved.

---

### Slide 11: Real-World Applications & Limitations
- **Applications:**
  - Search engine query instant-suggestion & predictive search.
  - IDE symbol indexing, code autocompletion, and syntax typo detection.
  - Smartphone virtual keyboards and predictive messaging engines.
  - IP network routing: Longest Prefix Match (LPM) in router routing tables.
  - Bioinformatics: DNA sequence search with mutation tolerance ($k$-mismatch search).
- **Limitations:**
  - Fixed alphabet: 26 lowercase English letters (`a`-`z`).
  - Memory overhead: 26-pointer array per node yields $\approx 224$ bytes/node ($\approx 17.23$ MB for 25k words).
- **Future Enhancements:**
  - Radix Tree (Patricia Trie) path compression for single-child chains.
  - Ternary Search Trees (TST) for large alphabets or Unicode.
  - Damerau-Levenshtein metric supporting adjacent character transpositions in 1 edit step.

---

### Slide 12: Conclusion & Key Takeaways
1. **Algorithmic Decoupling:** Tries decouple exact search latency from corpus size $N$, depending only on word length $L$.
2. **Dynamic Efficiency:** Tries allow dynamic insertion and deletion in $\mathcal{O}(L)$ without the $\mathcal{O}(N)$ shifting overhead of sorted arrays.
3. **Exponential Pruning:** Levenshtein DP combined with Trie branch pruning achieves a $1,345\times$ speedup by evaluating only 134 nodes out of 25,000 words.
4. **Honest Engineering:** The Trie is not the fastest structure for every query—Hash Tables win on exact lookup, and Sorted Arrays win on memory compactness. The Trie is chosen because it uniquely unifies exact search, prefix search, and fuzzy autocorrect in one dynamic index.

---

## 15 Likely Faculty Viva Questions & Model Answers

### Q1: Why did you choose a fixed 26-element array inside each `TrieNode` instead of a HashMap or BST?
> **Answer:**  
> An array of 26 references provides guaranteed $\mathcal{O}(1)$ child lookup via direct offset calculation `(c - 'a')`, with zero hashing, zero hash collision resolution, and zero object boxing/unboxing overhead. While it consumes more memory for sparse nodes ($\approx 224$ bytes per node), it maximizes instruction-level cache locality and yields the lowest possible transition latency, which is critical when traversing millions of nodes during fuzzy spell checking.

---

### Q2: If a Hash Table achieves $\mathcal{O}(1)$ average lookup time, why would anyone use a Trie for exact search?
> **Answer:**  
> A Hash Table is indeed faster for isolated exact lookups ($0.010\,\mu s$ vs $0.033\,\mu s$ in our benchmarks). However, a Hash Table **destroys prefix relationships**. To perform prefix autocomplete on a Hash Table, one must scan all $N$ entries ($\mathcal{O}(N \cdot P)$), which took $130.3\,\mu s$ in our tests—over $250\times$ slower than the Trie ($0.507\,\mu s$). Furthermore, a Hash Table cannot perform branch-pruned Levenshtein search; it must execute brute-force Levenshtein against all $N$ words. The Trie is selected because it unifies exact search, prefix search, and fuzzy search in a single coherent data structure.

---

### Q3: When does a Sorted Array with Binary Search beat a Trie, and why is the Trie preferred in dynamic systems?
> **Answer:**  
> A Sorted Array achieves exceptional memory efficiency (only $0.29$ MB for 25,000 words vs $17.23$ MB for the Trie) and very fast prefix search ($0.235\,\mu s$) due to contiguous cache locality and CPU prefetching during binary search. However, a Sorted Array suffers an **$\mathcal{O}(N)$ shifting penalty** on every insertion and deletion. In our benchmarks, inserting and deleting words in a 25,000-word sorted array took $1.190\,\mu s$ vs $0.277\,\mu s$ in the Trie ($4.3\times$ faster). In dynamic dictionaries where words are constantly added, updated, or removed, the Trie's $\mathcal{O}(L)$ dynamic update capability is decisively superior.

---

### Q4: How does your Top-$K$ autocomplete with `CustomBoundedMinHeap` optimize memory and runtime over full sorting?
> **Answer:**  
> If a prefix matches $M$ words, the naive approach collects all $M$ candidates and sorts them using MergeSort in $\mathcal{O}(M \log M)$ time, storing all $M$ objects in memory. Our `CustomBoundedMinHeap` maintains a min-heap of fixed capacity $K$. For each of the $M$ candidates, we compare it against the root (the current $K$-th best). If the candidate has higher frequency, we replace the root and sift down in $\mathcal{O}(\log K)$ time. This reduces the time complexity to $\mathcal{O}(M \log K)$ and caps retained memory to $\mathcal{O}(K)$. When $M = 5,000$ and $K = 5$, $\log K \approx 2.3$ vs $\log M \approx 12.3$, giving a theoretical $5.3\times$ reduction in comparator operations.

---

### Q5: Explain how your Trie deletion algorithm guarantees that words sharing a common prefix are never corrupted.
> **Answer:**  
> Deletion is implemented as a post-order recursive backtracking method (`deleteHelper`). When deleting `"cat"` while `"caterpillar"` exists:
> 1. The algorithm recursively descends to depth 3 (the `'t'` node) and unmarks `isEndOfWord = false`.
> 2. It then inspects `childCount` or calls `hasChildren()`. Because `'t'` has a child `'e'` leading to `"caterpillar"`, `hasChildren()` returns `true`.
> 3. The method immediately halts node deletion and returns `false` to the parent caller, preserving `'c'`, `'a'`, `'t'`, and all downstream descendants.
> 4. Physical node nullification (`children[index] = null`) occurs **only** when a node has `isEndOfWord == false` AND `childCount == 0`.

---

### Q6: Walk through the mathematical branch pruning condition in your Levenshtein autocorrect algorithm.
> **Answer:**  
> In the Steve Hanov algorithm, each Trie node represents a character transition and computes a single dynamic programming row $D$ representing the Levenshtein distances between the current Trie path prefix and all prefixes of the query string $Q$:
> $$D[i] = \min(D_{\text{prev}}[i] + 1, D[i-1] + 1, D_{\text{prev}}[i-1] + \text{cost})$$
> Because edit distance can never decrease as we append more characters down a path, the minimum value in the row, $\min_{0 \le i \le |Q|}(D[i])$, represents the absolute theoretical lower bound on edit distance for *any* word in this subtree. If:
> $$\min_{0 \le i \le |Q|}(D[i]) > d_{\max}$$
> no descendant word can possibly match within $d_{\max}$. We immediately prune the entire subtree, bypassing thousands of candidate words in $\mathcal{O}(1)$ time.

---

### Q7: What is the difference between Levenshtein distance and Damerau-Levenshtein distance?
> **Answer:**  
> Standard Levenshtein distance supports three edit operations: **insertion**, **deletion**, and **substitution**, each with a cost of 1. Under Levenshtein, an adjacent character transposition (e.g., `"progarm"` $\to$ `"program"`) requires two operations: substituting `'a'` with `'r'` and `'r'` with `'a'`, resulting in an edit distance of 2.  
> Damerau-Levenshtein distance introduces a fourth primitive operation: **adjacent transposition**, costing 1 edit step. In our case study, we implement standard Levenshtein, which correctly finds `"progarm"` at distance $d=2$.

---

### Q8: What is the exact memory footprint of a `TrieNode` on a 64-bit JVM, and why does this matter?
> **Answer:**  
> On a standard 64-bit HotSpot JVM without Compressed OOPs:
> - Object header (Mark Word + Klass Word): 16 bytes.
> - Reference array `children[26]`: 16-byte array header + 4-byte length + 4-byte padding + $(26 \times 8 \text{ bytes}) = 232$ bytes.
> - Reference pointer to array: 8 bytes.
> - Primitive fields (`boolean isEndOfWord` [1B], `int frequency` [4B], `int childCount` [4B]): 9 bytes + 7 bytes JVM padding = 16 bytes.
> - Total per node $\approx 224$ to $272$ bytes.
> For our 25,000-word dictionary ($80,673$ nodes), this consumes approximately $17.23$ MB of heap memory. This space-time trade-off is the primary drawback of multi-way Tries.

---

### Q9: How do Radix Trees (Patricia Tries) and Ternary Search Trees (TST) solve the Trie's memory overhead?
> **Answer:**  
> 1. **Radix Tree (Patricia Trie):** Compresses non-branching chains of single-child nodes into a single edge storing a string slice (e.g., compressing `c -> a -> r -> e -> e -> r` into `c -> areer`). This reduces total node count by $50\text{--}70\%$.
> 2. **Ternary Search Tree (TST):** Replaces the 26-pointer child array with three pointers: `left` ($< char$), `equal` ($= char$), and `right` ($> char$). Each node holds only 3 pointers instead of 26, dramatically reducing memory overhead for sparse alphabets or full Unicode at the cost of $\mathcal{O}(\log |\Sigma|)$ traversal overhead per character.

---

### Q10: Why did you implement generic geometric resizing in `CustomList<T>`, and what is the amortized cost proof?
> **Answer:**  
> A naive dynamic array that grows by a fixed constant $C$ incurs $\mathcal{O}(N^2)$ total copying cost for $N$ insertions, giving an unacceptably slow $\mathcal{O}(N)$ per-insertion average. By applying a **$2\times$ geometric expansion factor**, array copying occurs at sizes $1, 2, 4, 8, \dots, 2^k$. The total elements copied during $N$ insertions is:
> $$\sum_{j=0}^{\lceil \log_2 N \rceil} 2^j = 2^{\lceil \log_2 N \rceil + 1} - 1 < 2N$$
> Dividing total work $2N$ by $N$ insertions yields an **amortized $\mathcal{O}(1)$ time complexity** per insertion, ensuring that candidate collection never degrades system throughput.

---

### Q11: Why is `CustomQueue<T>` implemented as a linked pointer list rather than a circular array?
> **Answer:**  
> In our BFS prefix traversal (`autocompleteBFS`), the maximum number of nodes enqueued at any level is variable and unpredictable. A circular array requires either pre-allocating a large array or resizing/shifting elements when full. A pointer-based singly-linked FIFO queue with explicit `head` and `tail` pointers provides guaranteed $\mathcal{O}(1)$ `enqueue()` and $\mathcal{O}(1)$ `dequeue()` operations with zero reallocation overhead and zero unused capacity buffers.

---

### Q12: Why did you implement MergeSort rather than QuickSort or HeapSort in `CandidateSorter`?
> **Answer:**  
> 1. **Worst-Case Guarantee:** MergeSort provides a strict $\mathcal{O}(M \log M)$ upper bound under all inputs, whereas QuickSort degrades to $\mathcal{O}(M^2)$ if pivot selection encounters adversarial distributions.
> 2. **Algorithm Stability:** MergeSort is inherently stable. When two candidates share the exact same edit distance and corpus frequency, MergeSort preserves their relative discovery order or alphabetical secondary tie-break without erratic reordering.
> 3. **Academic Coverage:** MergeSort is a classic divide-and-conquer algorithm covered directly in our DSA syllabus.

---

### Q13: Explain your empirical node exploration metric ($S=134$ nodes explored vs $N=25,000$ words).
> **Answer:**  
> In our benchmark on $N=25,000$ words with typo `"progrm"` at $d=1$:
> - The Linear Scan and Hash Table baselines must compute the Wagner-Fischer dynamic programming matrix for **all 25,000 words**, taking $\approx 2,825\,\mu s$.
> - The Trie autocorrect algorithm explored only **$S = 134$ nodes** out of 80,673 total nodes before pruning all remaining subtrees, completing in **$2.1\,\mu s$**.
> - This represents an empirical **$186.6\times$ reduction in search space** and a **$1,345\times$ speedup** in execution latency, proving the practical power of Trie branch pruning.

---

### Q14: How would you extend this Trie implementation to support full Unicode (UTF-8) or case-sensitive text?
> **Answer:**  
> A fixed 26-element array only supports English letters `'a'` to `'z'`. To support full Unicode or mixed-case text:
> 1. In memory-constrained scenarios, replace the 26-array with a **Ternary Search Tree (TST)** or a custom open-addressing primitive integer hash map (`int` Unicode codepoint $\to$ `TrieNode`).
> 2. In performance-critical scenarios, implement a byte-level Trie (Radix-256 tree), where each node has 256 child slots corresponding to individual UTF-8 bytes. A multi-byte Unicode character is represented as a sequence of 1 to 4 byte transitions, maintaining $\mathcal{O}(1)$ edge traversal while supporting all human languages.

---

### Q15: Why did you use JVM warm-up phases, median of 5 repetitions, fixed seeds, and a blackhole accumulator in the benchmark?
> **Answer:**  
> Benchmarking on a managed Java runtime is subject to four major sources of experimental distortion:
> 1. **JIT Compilation:** The HotSpot JVM starts in interpreted mode and compiles hot methods to machine code via the C1/C2 compilers. We execute 5,000 unmeasured warm-up iterations to reach steady-state compiled code.
> 2. **Dead-Code Elimination:** If the return value of a search method is unused, the JIT optimizer may eliminate the entire method call. We accumulate all results into a volatile `blackhole` variable to force full execution.
> 3. **GC Pauses and OS Jitter:** Mean timings are easily distorted by an occasional garbage collection pause or OS context switch. We run 5 distinct measurement batches and report the **Median** along with the `[Min - Max]` range.
> 4. **Reproducibility:** We sample hit and miss words using a fixed `Random(42)` seed so that every test run evaluates the exact same query distribution.
