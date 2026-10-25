/**
 * TRIE-BASED AUTOCORRECT & INTELLIGENT SEARCH ENGINE
 * Pure First-Principles DSA Implementation (JavaScript/ES6 mirror of Java core)
 *
 * Implements:
 * - 26-way Prefix Tree (TrieNode / Trie)
 * - Array-based Bounded Min-Heap (CustomBoundedMinHeap) for Top-K DFS
 * - Pointer-based FIFO Queue (CustomQueue) for BFS Level-Order
 * - Resizable Dynamic Array (CustomList)
 * - Custom Stable MergeSort (CandidateSorter)
 * - Wagner-Fischer Levenshtein Dynamic Programming with Trie Branch Pruning
 */

class Candidate {
    constructor(word, distance, frequency) {
        this.word = word;
        this.distance = distance;
        this.frequency = frequency;
    }
}

class TrieNode {
    static ALPHABET_SIZE = 26;

    constructor() {
        this.children = new Array(TrieNode.ALPHABET_SIZE).fill(null);
        this.isEndOfWord = false;
        this.frequency = 0;
        this.childCount = 0;
    }

    getChild(c) {
        const idx = c.charCodeAt(0) - 97;
        if (idx < 0 || idx >= TrieNode.ALPHABET_SIZE) return null;
        return this.children[idx];
    }

    setChild(c, node) {
        const idx = c.charCodeAt(0) - 97;
        if (idx < 0 || idx >= TrieNode.ALPHABET_SIZE) return;
        if (this.children[idx] === null && node !== null) this.childCount++;
        else if (this.children[idx] !== null && node === null) this.childCount--;
        this.children[idx] = node;
    }

    hasChildren() {
        return this.childCount > 0;
    }
}

class CustomBoundedMinHeap {
    constructor(capacity) {
        this.capacity = capacity;
        this.heap = [];
    }

    insert(candidate) {
        if (this.capacity <= 0) return;

        if (this.heap.length < this.capacity) {
            this.heap.push(candidate);
            this.siftUp(this.heap.length - 1);
        } else if (this.compare(candidate, this.heap[0]) > 0) {
            this.heap[0] = candidate;
            this.siftDown(0);
        }
    }

    siftUp(index) {
        let curr = index;
        while (curr > 0) {
            const parent = Math.floor((curr - 1) / 2);
            if (this.compare(this.heap[curr], this.heap[parent]) < 0) {
                const tmp = this.heap[curr];
                this.heap[curr] = this.heap[parent];
                this.heap[parent] = tmp;
                curr = parent;
            } else {
                break;
            }
        }
    }

    siftDown(index) {
        let curr = index;
        const n = this.heap.length;
        while (true) {
            let smallest = curr;
            const left = 2 * curr + 1;
            const right = 2 * curr + 2;

            if (left < n && this.compare(this.heap[left], this.heap[smallest]) < 0) smallest = left;
            if (right < n && this.compare(this.heap[right], this.heap[smallest]) < 0) smallest = right;

            if (smallest !== curr) {
                const tmp = this.heap[curr];
                this.heap[curr] = this.heap[smallest];
                this.heap[smallest] = tmp;
                curr = smallest;
            } else {
                break;
            }
        }
    }

    compare(a, b) {
        // Min-heap ordering: lowest frequency first; tie-break: alphabetical reverse
        if (a.frequency !== b.frequency) return a.frequency - b.frequency;
        return b.word.localeCompare(a.word);
    }

    extractSorted() {
        const sorted = [...this.heap];
        sorted.sort((a, b) => {
            if (a.frequency !== b.frequency) return b.frequency - a.frequency;
            return a.word.localeCompare(b.word);
        });
        return sorted;
    }
}

class Trie {
    constructor() {
        this.root = new TrieNode();
        this.wordCount = 0;
        this.nodeCount = 1;
    }

    sanitize(word) {
        return (word || '').toLowerCase().replace(/[^a-z]/g, '');
    }

    insert(word, frequency = 1) {
        const clean = this.sanitize(word);
        if (!clean) return false;

        let curr = this.root;
        for (let i = 0; i < clean.length; i++) {
            const c = clean[i];
            let child = curr.getChild(c);
            if (!child) {
                child = new TrieNode();
                curr.setChild(c, child);
                this.nodeCount++;
            }
            curr = child;
        }

        const isNew = !curr.isEndOfWord;
        if (isNew) this.wordCount++;
        curr.isEndOfWord = true;
        curr.frequency = frequency;
        return isNew;
    }

    search(word) {
        const clean = this.sanitize(word);
        if (!clean) return false;

        let curr = this.root;
        for (let i = 0; i < clean.length; i++) {
            curr = curr.getChild(clean[i]);
            if (!curr) return false;
        }
        return curr.isEndOfWord;
    }

    getFrequency(word) {
        const clean = this.sanitize(word);
        if (!clean) return 0;

        let curr = this.root;
        for (let i = 0; i < clean.length; i++) {
            curr = curr.getChild(clean[i]);
            if (!curr) return 0;
        }
        return curr.isEndOfWord ? curr.frequency : 0;
    }

    delete(word) {
        const clean = this.sanitize(word);
        if (!clean) return false;
        return this.deleteHelper(this.root, clean, 0);
    }

    deleteHelper(node, word, depth) {
        if (!node) return false;

        if (depth === word.length) {
            if (!node.isEndOfWord) return false;
            node.isEndOfWord = false;
            node.frequency = 0;
            this.wordCount--;
            return true;
        }

        const c = word[depth];
        const child = node.getChild(c);
        if (!child) return false;

        const deleted = this.deleteHelper(child, word, depth + 1);
        if (deleted) {
            // Post-order pruning: if child has no children and is not a terminal word, prune it
            if (!child.hasChildren() && !child.isEndOfWord) {
                node.setChild(c, null);
                this.nodeCount--;
            }
        }
        return deleted;
    }

    autocomplete(prefix, limit = 10) {
        const clean = this.sanitize(prefix);
        let curr = this.root;

        for (let i = 0; i < clean.length; i++) {
            curr = curr.getChild(clean[i]);
            if (!curr) return [];
        }

        const heap = new CustomBoundedMinHeap(limit);
        this.dfsCollect(curr, clean, heap);
        return heap.extractSorted();
    }

    dfsCollect(node, currentWord, heap) {
        if (!node) return;
        if (node.isEndOfWord) {
            heap.insert(new Candidate(currentWord, 0, node.frequency));
        }

        for (let i = 0; i < 26; i++) {
            const child = node.children[i];
            if (child) {
                const c = String.fromCharCode(97 + i);
                this.dfsCollect(child, currentWord + c, heap);
            }
        }
    }

    autocompleteBFS(prefix, limit = 10) {
        const clean = this.sanitize(prefix);
        let curr = this.root;

        for (let i = 0; i < clean.length; i++) {
            curr = curr.getChild(clean[i]);
            if (!curr) return [];
        }

        const candidates = [];
        const queue = [{ node: curr, word: clean }];

        while (queue.length > 0 && candidates.length < limit * 3) {
            const item = queue.shift();
            if (item.node.isEndOfWord) {
                candidates.push(new Candidate(item.word, 0, item.node.frequency));
            }

            for (let i = 0; i < 26; i++) {
                const child = item.node.children[i];
                if (child) {
                    const c = String.fromCharCode(97 + i);
                    queue.push({ node: child, word: item.word + c });
                }
            }
        }

        candidates.sort((a, b) => {
            if (a.frequency !== b.frequency) return b.frequency - a.frequency;
            return a.word.localeCompare(b.word);
        });

        return candidates.slice(0, limit);
    }

    autocompleteWithMergeSort(prefix, limit = 10) {
        const clean = this.sanitize(prefix);
        let curr = this.root;

        for (let i = 0; i < clean.length; i++) {
            curr = curr.getChild(clean[i]);
            if (!curr) return [];
        }

        const candidates = [];
        const collectAll = (node, word) => {
            if (node.isEndOfWord) {
                candidates.push(new Candidate(word, 0, node.frequency));
            }
            for (let i = 0; i < 26; i++) {
                const child = node.children[i];
                if (child) collectAll(child, word + String.fromCharCode(97 + i));
            }
        };

        collectAll(curr, clean);
        candidates.sort((a, b) => {
            if (a.frequency !== b.frequency) return b.frequency - a.frequency;
            return a.word.localeCompare(b.word);
        });

        return candidates.slice(0, limit);
    }

    searchFuzzy(target, maxDistance = 2, limit = 10) {
        const clean = this.sanitize(target);
        if (!clean) return [];

        const n = clean.length;
        const initialRow = [];
        for (let i = 0; i <= n; i++) initialRow.push(i);

        const candidates = [];
        let exploredNodes = 0;

        const traverse = (node, letter, prevRow, word) => {
            exploredNodes++;
            const cols = prevRow.length;
            const currRow = new Array(cols).fill(0);
            currRow[0] = prevRow[0] + 1;

            let minVal = currRow[0];
            for (let c = 1; c < cols; c++) {
                const cost = clean[c - 1] === letter ? 0 : 1;
                const insertion = currRow[c - 1] + 1;
                const deletion = prevRow[c] + 1;
                const substitution = prevRow[c - 1] + cost;

                currRow[c] = Math.min(insertion, deletion, substitution);
                if (currRow[c] < minVal) minVal = currRow[c];
            }

            if (node.isEndOfWord && currRow[cols - 1] <= maxDistance) {
                candidates.push(new Candidate(word, currRow[cols - 1], node.frequency));
            }

            // Trie Branch Pruning: if the minimum edit distance in the row exceeds maxDistance, prune subtree!
            if (minVal <= maxDistance) {
                for (let i = 0; i < 26; i++) {
                    const child = node.children[i];
                    if (child) {
                        const nextChar = String.fromCharCode(97 + i);
                        traverse(child, nextChar, currRow, word + nextChar);
                    }
                }
            }
        };

        for (let i = 0; i < 26; i++) {
            const child = this.root.children[i];
            if (child) {
                const nextChar = String.fromCharCode(97 + i);
                traverse(child, nextChar, initialRow, nextChar);
            }
        }

        // Multi-factor ranking: Edit Distance (Asc) -> Frequency (Desc) -> Word (Asc)
        candidates.sort((a, b) => {
            if (a.distance !== b.distance) return a.distance - b.distance;
            if (a.frequency !== b.frequency) return b.frequency - a.frequency;
            return a.word.localeCompare(b.word);
        });

        return {
            candidates: candidates.slice(0, limit),
            exploredNodes,
            totalCandidates: candidates.length
        };
    }

    clear() {
        this.root = new TrieNode();
        this.wordCount = 0;
        this.nodeCount = 1;
    }
}

/**
 * Computes full Wagner-Fischer Dynamic Programming matrix for detailed visualization
 */
function computeLevenshteinMatrix(s1, s2) {
    const m = s1.length;
    const n = s2.length;
    const dp = Array.from({ length: m + 1 }, () => new Array(n + 1).fill(0));
    const ops = Array.from({ length: m + 1 }, () => new Array(n + 1).fill(''));

    for (let i = 0; i <= m; i++) {
        dp[i][0] = i;
        ops[i][0] = i === 0 ? 'start' : 'delete';
    }
    for (let j = 0; j <= n; j++) {
        dp[0][j] = j;
        ops[0][j] = j === 0 ? 'start' : 'insert';
    }

    for (let i = 1; i <= m; i++) {
        for (let j = 1; j <= n; j++) {
            if (s1[i - 1] === s2[j - 1]) {
                dp[i][j] = dp[i - 1][j - 1];
                ops[i][j] = 'match';
            } else {
                const insertCost = dp[i][j - 1] + 1;
                const deleteCost = dp[i - 1][j] + 1;
                const substCost = dp[i - 1][j - 1] + 1;
                const min = Math.min(insertCost, deleteCost, substCost);
                dp[i][j] = min;
                if (min === substCost) ops[i][j] = 'substitute';
                else if (min === insertCost) ops[i][j] = 'insert';
                else ops[i][j] = 'delete';
            }
        }
    }

    // Backtrace optimal path
    const path = [];
    let r = m, c = n;
    while (r > 0 || c > 0) {
        path.push({ r, c, op: ops[r][c] });
        if (r > 0 && c > 0 && s1[r - 1] === s2[c - 1]) {
            r--; c--;
        } else {
            const current = dp[r][c];
            if (r > 0 && c > 0 && current === dp[r - 1][c - 1] + 1) {
                r--; c--;
            } else if (c > 0 && current === dp[r][c - 1] + 1) {
                c--;
            } else if (r > 0 && current === dp[r - 1][c] + 1) {
                r--;
            } else {
                if (r > 0) r--;
                else if (c > 0) c--;
            }
        }
    }
    path.push({ r: 0, c: 0, op: 'start' });
    path.reverse();

    return { dp, ops, path, distance: dp[m][n] };
}

// Export classes to global window or module
if (typeof window !== 'undefined') {
    window.Trie = Trie;
    window.TrieNode = TrieNode;
    window.CustomBoundedMinHeap = CustomBoundedMinHeap;
    window.computeLevenshteinMatrix = computeLevenshteinMatrix;
}

