package datastructures;

import core.Candidate;
import core.CandidateSorter;

/**
 * Trie (Prefix Tree) implementation supporting O(L) exact word insertion,
 * search, deletion, prefix-based autocomplete, and Levenshtein-distance-pruned
 * autocorrection candidate search.
 *
 * All operations are implemented without external collections or Java utility classes.
 */
public class Trie {
    private final TrieNode root;
    private int wordCount;
    private int nodeCount;

    /**
     * Initializes an empty Trie with a root node.
     */
    public Trie() {
        this.root = new TrieNode();
        this.wordCount = 0;
        this.nodeCount = 1; // Root node itself
    }

    /**
     * Inserts a word with its associated frequency into the Trie.
     * Time Complexity: O(L) where L is the length of the word.
     * Auxiliary Space: O(L) in the worst case (when no prefix exists).
     *
     * @param word the string to insert
     * @param frequency the frequency rank of the word
     * @return true if inserted as a new word, false if existing word updated
     */
    public boolean insert(String word, int frequency) {
        if (word == null) {
            return false;
        }
        String cleanWord = sanitize(word);
        if (cleanWord.isEmpty()) {
            return false;
        }

        TrieNode current = root;
        for (int i = 0; i < cleanWord.length(); i++) {
            char c = cleanWord.charAt(i);
            TrieNode child = current.getChild(c);
            if (child == null) {
                child = new TrieNode();
                current.setChild(c, child);
                nodeCount++;
            }
            current = child;
        }

        boolean isNew = !current.isEndOfWord();
        if (isNew) {
            wordCount++;
        }
        current.setEndOfWord(true);
        current.setFrequency(frequency);
        return isNew;
    }

    /**
     * Searches for an exact word in the Trie.
     * Time Complexity: O(L) where L is the length of the word.
     * Auxiliary Space: O(1)
     *
     * @param word the word to search for
     * @return true if exact word exists and is marked as terminal, false otherwise
     */
    public boolean search(String word) {
        if (word == null) {
            return false;
        }
        String cleanWord = sanitize(word);
        if (cleanWord.isEmpty()) {
            return false;
        }

        TrieNode current = root;
        for (int i = 0; i < cleanWord.length(); i++) {
            char c = cleanWord.charAt(i);
            current = current.getChild(c);
            if (current == null) {
                return false;
            }
        }
        return current.isEndOfWord();
    }

    /**
     * Retrieves the stored frequency for an exact word.
     *
     * @param word the target word
     * @return positive frequency if found, 0 otherwise
     */
    public int getFrequency(String word) {
        if (word == null) {
            return 0;
        }
        String cleanWord = sanitize(word);
        if (cleanWord.isEmpty()) {
            return 0;
        }

        TrieNode current = root;
        for (int i = 0; i < cleanWord.length(); i++) {
            char c = cleanWord.charAt(i);
            current = current.getChild(c);
            if (current == null) {
                return 0;
            }
        }
        return current.isEndOfWord() ? current.getFrequency() : 0;
    }

    /**
     * Deletes a word from the Trie without disturbing other words sharing prefixes.
     * Uses recursive bottom-up post-order traversal to unmark terminal flags and
     * safely prune orphan nodes.
     *
     * Time Complexity: O(L)
     * Auxiliary Space: O(L) for recursion stack.
     *
     * @param word the word to remove
     * @return true if successfully removed, false if word was not present
     */
    public boolean delete(String word) {
        if (word == null) {
            return false;
        }
        String cleanWord = sanitize(word);
        if (cleanWord.isEmpty() || !search(cleanWord)) {
            return false;
        }

        deleteRecursively(root, cleanWord, 0);
        wordCount--;
        return true;
    }

    /**
     * Helper method for bottom-up node pruning.
     * Returns true if the child node should be pruned by its parent.
     */
    private boolean deleteRecursively(TrieNode current, String word, int depth) {
        if (depth == word.length()) {
            // Unmark the end of word
            current.setEndOfWord(false);
            current.setFrequency(0);
            // If it has no children, it can be deleted
            return !current.hasChildren();
        }

        char c = word.charAt(depth);
        TrieNode child = current.getChild(c);
        if (child == null) {
            return false;
        }

        boolean shouldDeleteChild = deleteRecursively(child, word, depth + 1);

        if (shouldDeleteChild) {
            current.setChild(c, null);
            nodeCount--;
            // Current node can also be deleted if it is not a word end and has no remaining children
            return !current.isEndOfWord() && !current.hasChildren();
        }

        return false;
    }

    /**
     * Generates Top-K autocomplete suggestions for a given prefix using an in-house
     * Bounded Min-Heap (CustomBoundedMinHeap).
     *
     * Algorithmic Complexity:
     *   1. Prefix traversal: O(P)
     *   2. Subtree exploration: O(V) where V is the count of visited Trie nodes
     *   3. Top-K extraction: O(M log K + K log K) for K > 0 (special-cased to O(1) when K = 0)
     *   Retained Auxiliary Space: O(K) in the heap + O(maxDepth) traversal stack.
     *
     * @param prefix the input prefix
     * @param maxResults maximum suggestions to return (K)
     * @return CustomList of Top-K ranked Candidate suggestions
     */
    public CustomList<Candidate> autocomplete(String prefix, int maxResults) {
        CustomList<Candidate> empty = new CustomList<>();
        if (prefix == null || maxResults <= 0) {
            return empty;
        }
        String cleanPrefix = sanitize(prefix);
        if (cleanPrefix.isEmpty()) {
            return empty;
        }

        TrieNode current = root;
        for (int i = 0; i < cleanPrefix.length(); i++) {
            char c = cleanPrefix.charAt(i);
            current = current.getChild(c);
            if (current == null) {
                return empty; // Prefix not found
            }
        }

        CustomBoundedMinHeap heap = new CustomBoundedMinHeap(maxResults);
        StringBuilder sb = new StringBuilder(cleanPrefix);
        collectWordsIntoHeap(current, sb, heap);
        return heap.extractSorted();
    }

    /**
     * Reference implementation of autocomplete using full DFS collection and MergeSort.
     * Time Complexity: O(P + V + M log M)
     * Auxiliary Space: O(M) for list of all matches
     *
     * @param prefix the input prefix
     * @param maxResults maximum suggestions to return
     * @return CustomList of ranked Candidate suggestions
     */
    public CustomList<Candidate> autocompleteWithMergeSort(String prefix, int maxResults) {
        CustomList<Candidate> results = new CustomList<>();
        if (prefix == null || maxResults <= 0) {
            return results;
        }
        String cleanPrefix = sanitize(prefix);
        if (cleanPrefix.isEmpty()) {
            return results;
        }

        TrieNode current = root;
        for (int i = 0; i < cleanPrefix.length(); i++) {
            char c = cleanPrefix.charAt(i);
            current = current.getChild(c);
            if (current == null) {
                return results;
            }
        }

        StringBuilder sb = new StringBuilder(cleanPrefix);
        collectWords(current, sb, results);
        CandidateSorter.sort(results);
        return limitList(results, maxResults);
    }

    /**
     * Breadth-First Search (level-order) autocomplete prefix traversal utilizing
     * the CustomQueue data structure.
     *
     * @param prefix the input prefix
     * @param maxResults maximum suggestions to return (K)
     * @return CustomList of Top-K ranked Candidate suggestions
     */
    public CustomList<Candidate> autocompleteBFS(String prefix, int maxResults) {
        CustomList<Candidate> empty = new CustomList<>();
        if (prefix == null || maxResults <= 0) {
            return empty;
        }
        String cleanPrefix = sanitize(prefix);
        if (cleanPrefix.isEmpty()) {
            return empty;
        }

        TrieNode current = root;
        for (int i = 0; i < cleanPrefix.length(); i++) {
            char c = cleanPrefix.charAt(i);
            current = current.getChild(c);
            if (current == null) {
                return empty;
            }
        }

        CustomBoundedMinHeap heap = new CustomBoundedMinHeap(maxResults);
        CustomQueue<BFSQueueItem> queue = new CustomQueue<>();
        queue.enqueue(new BFSQueueItem(current, cleanPrefix));

        while (!queue.isEmpty()) {
            BFSQueueItem item = queue.dequeue();
            if (item.node.isEndOfWord()) {
                heap.insert(new Candidate(item.path, 0, item.node.getFrequency()));
            }

            TrieNode[] children = item.node.getChildren();
            for (int i = 0; i < TrieNode.ALPHABET_SIZE; i++) {
                if (children[i] != null) {
                    char nextChar = (char) ('a' + i);
                    queue.enqueue(new BFSQueueItem(children[i], item.path + nextChar));
                }
            }
        }

        return heap.extractSorted();
    }

    private static class BFSQueueItem {
        final TrieNode node;
        final String path;
        BFSQueueItem(TrieNode node, String path) {
            this.node = node;
            this.path = path;
        }
    }

    /**
     * Recursive DFS helper collecting terminal words directly into the bounded min-heap.
     */
    private void collectWordsIntoHeap(TrieNode node, StringBuilder currentWord, CustomBoundedMinHeap heap) {
        if (node.isEndOfWord()) {
            heap.insert(new Candidate(currentWord.toString(), 0, node.getFrequency()));
        }

        TrieNode[] children = node.getChildren();
        for (int i = 0; i < TrieNode.ALPHABET_SIZE; i++) {
            if (children[i] != null) {
                char c = (char) ('a' + i);
                currentWord.append(c);
                collectWordsIntoHeap(children[i], currentWord, heap);
                currentWord.deleteCharAt(currentWord.length() - 1);
            }
        }
    }

    /**
     * Recursive DFS helper to gather all words branching from the given node.
     */
    private void collectWords(TrieNode node, StringBuilder currentWord, CustomList<Candidate> results) {
        if (node.isEndOfWord()) {
            results.add(new Candidate(currentWord.toString(), 0, node.getFrequency()));
        }

        TrieNode[] children = node.getChildren();
        for (int i = 0; i < TrieNode.ALPHABET_SIZE; i++) {
            if (children[i] != null) {
                char c = (char) ('a' + i);
                currentWord.append(c);
                collectWords(children[i], currentWord, results);
                currentWord.deleteCharAt(currentWord.length() - 1); // Backtrack
            }
        }
    }

    public static class FuzzyResult {
        public final CustomList<Candidate> candidates;
        public final int nodesExplored;

        public FuzzyResult(CustomList<Candidate> candidates, int nodesExplored) {
            this.candidates = candidates;
            this.nodesExplored = nodesExplored;
        }
    }

    /**
     * Performs fuzzy autocorrection search using Trie-based Levenshtein Dynamic Programming
     * with subtree branch pruning.
     *
     * @param target the misspelled query string
     * @param maxDistance maximum allowable Levenshtein edit distance
     * @param maxResults maximum candidate corrections to return
     * @return CustomList of ranked Candidate corrections
     */
    public CustomList<Candidate> searchFuzzy(String target, int maxDistance, int maxResults) {
        return searchFuzzyWithStats(target, maxDistance, maxResults).candidates;
    }

    /**
     * Performs fuzzy autocorrection search and reports both candidate corrections
     * and the exact count of Trie nodes explored (S) for benchmark reporting.
     *
     * @param target the misspelled query string
     * @param maxDistance maximum allowable edit distance
     * @param maxResults maximum candidate corrections to return
     * @return FuzzyResult containing ranked candidates and nodesExplored count
     */
    public FuzzyResult searchFuzzyWithStats(String target, int maxDistance, int maxResults) {
        CustomList<Candidate> candidates = new CustomList<>();
        if (target == null) {
            return new FuzzyResult(candidates, 0);
        }
        String cleanTarget = sanitize(target);
        if (cleanTarget.isEmpty()) {
            return new FuzzyResult(candidates, 0);
        }

        int targetLen = cleanTarget.length();
        int[] initialRow = new int[targetLen + 1];
        for (int i = 0; i <= targetLen; i++) {
            initialRow[i] = i;
        }

        int[] nodeCounter = new int[1];
        StringBuilder pathBuilder = new StringBuilder();
        TrieNode[] rootChildren = root.getChildren();

        for (int i = 0; i < TrieNode.ALPHABET_SIZE; i++) {
            if (rootChildren[i] != null) {
                char c = (char) ('a' + i);
                pathBuilder.append(c);
                searchFuzzyRecursive(rootChildren[i], c, cleanTarget, initialRow, pathBuilder, maxDistance, candidates, nodeCounter);
                pathBuilder.deleteCharAt(pathBuilder.length() - 1);
            }
        }

        CandidateSorter.sort(candidates);
        return new FuzzyResult(limitList(candidates, maxResults), nodeCounter[0]);
    }

    /**
     * Recursive helper traversing Trie nodes while computing DP matrix rows.
     */
    private void searchFuzzyRecursive(TrieNode node, char currentChar, String target,
                                      int[] prevRow, StringBuilder currentWord,
                                      int maxDistance, CustomList<Candidate> candidates,
                                      int[] nodeCounter) {
        nodeCounter[0]++;
        int targetLen = target.length();
        int[] currentRow = new int[targetLen + 1];
        currentRow[0] = prevRow[0] + 1;
        int rowMin = currentRow[0];

        for (int i = 1; i <= targetLen; i++) {
            int cost = (target.charAt(i - 1) == currentChar) ? 0 : 1;
            int insertCost = currentRow[i - 1] + 1;
            int deleteCost = prevRow[i] + 1;
            int replaceCost = prevRow[i - 1] + cost;

            int minOp = insertCost;
            if (deleteCost < minOp) minOp = deleteCost;
            if (replaceCost < minOp) minOp = replaceCost;

            currentRow[i] = minOp;
            if (currentRow[i] < rowMin) {
                rowMin = currentRow[i];
            }
        }

        if (node.isEndOfWord() && currentRow[targetLen] <= maxDistance) {
            candidates.add(new Candidate(currentWord.toString(), currentRow[targetLen], node.getFrequency()));
        }

        if (rowMin <= maxDistance) {
            TrieNode[] children = node.getChildren();
            for (int i = 0; i < TrieNode.ALPHABET_SIZE; i++) {
                if (children[i] != null) {
                    char nextChar = (char) ('a' + i);
                    currentWord.append(nextChar);
                    searchFuzzyRecursive(children[i], nextChar, target, currentRow, currentWord, maxDistance, candidates, nodeCounter);
                    currentWord.deleteCharAt(currentWord.length() - 1);
                }
            }
        }
    }

    /**
     * Sanitizes input strings by converting to lowercase and stripping non-alphabetic characters.
     */
    public static String sanitize(String str) {
        if (str == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = Character.toLowerCase(str.charAt(i));
            if (c >= 'a' && c <= 'z') {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Limits the candidate list to at most limit items.
     */
    private CustomList<Candidate> limitList(CustomList<Candidate> list, int limit) {
        if (list.size() <= limit) {
            return list;
        }
        CustomList<Candidate> trimmed = new CustomList<>(limit);
        for (int i = 0; i < limit; i++) {
            trimmed.add(list.get(i));
        }
        return trimmed;
    }

    public int getWordCount() {
        return wordCount;
    }

    public int getNodeCount() {
        return nodeCount;
    }

    public TrieNode getRoot() {
        return root;
    }

    /**
     * Resets the Trie to an empty state with a fresh root.
     */
    public void clear() {
        TrieNode[] children = root.getChildren();
        for (int i = 0; i < TrieNode.ALPHABET_SIZE; i++) {
            children[i] = null;
        }
        this.wordCount = 0;
        this.nodeCount = 1;
    }
}

