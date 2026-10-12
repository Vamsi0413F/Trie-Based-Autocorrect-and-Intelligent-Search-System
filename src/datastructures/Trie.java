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
     * Generates autocomplete suggestions for a given prefix.
     *
     * Algorithm:
     *   1. Traverse down to the prefix node: O(P)
     *   2. Perform DFS traversal to collect all descendant words: O(K) where K is nodes in subtree
     *   3. Rank suggestions by frequency descending: O(M log M)
     *   4. Return top maxResults candidates.
     *
     * @param prefix the input prefix
     * @param maxResults maximum suggestions to return
     * @return CustomList of ranked Candidate suggestions
     */
    public CustomList<Candidate> autocomplete(String prefix, int maxResults) {
        CustomList<Candidate> results = new CustomList<>();
        if (prefix == null) {
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
                return results; // Prefix not found
            }
        }

        // DFS to collect all words in subtree
        StringBuilder sb = new StringBuilder(cleanPrefix);
        collectWords(current, sb, results);

        // Sort by frequency descending using custom MergeSort
        CandidateSorter.sort(results);

        // Limit results to maxResults
        return limitList(results, maxResults);
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

    /**
     * Performs fuzzy autocorrection search using Trie-based Levenshtein Dynamic Programming
     * with subtree branch pruning.
     *
     * Instead of computing edit distance independently for every dictionary entry,
     * this algorithm maintains DP rows across Trie levels, pruning subtrees whose minimum
     * edit cost already exceeds maxDistance.
     *
     * Time Complexity: O(Alphabet_Size * maxDistance * K_visited) — orders of magnitude
     * faster than linear dictionary scanning.
     *
     * @param target the misspelled query string
     * @param maxDistance maximum allowable Levenshtein edit distance
     * @param maxResults maximum candidate corrections to return
     * @return CustomList of ranked Candidate corrections
     */
    public CustomList<Candidate> searchFuzzy(String target, int maxDistance, int maxResults) {
        CustomList<Candidate> candidates = new CustomList<>();
        if (target == null) {
            return candidates;
        }
        String cleanTarget = sanitize(target);
        if (cleanTarget.isEmpty()) {
            return candidates;
        }

        int targetLen = cleanTarget.length();
        // Initialize 0th DP row representing transformations from empty prefix
        int[] initialRow = new int[targetLen + 1];
        for (int i = 0; i <= targetLen; i++) {
            initialRow[i] = i;
        }

        StringBuilder pathBuilder = new StringBuilder();
        TrieNode[] rootChildren = root.getChildren();

        for (int i = 0; i < TrieNode.ALPHABET_SIZE; i++) {
            if (rootChildren[i] != null) {
                char c = (char) ('a' + i);
                pathBuilder.append(c);
                searchFuzzyRecursive(rootChildren[i], c, cleanTarget, initialRow, pathBuilder, maxDistance, candidates);
                pathBuilder.deleteCharAt(pathBuilder.length() - 1);
            }
        }

        // Rank candidates: primary = distance (asc), secondary = freq (desc), tertiary = alpha
        CandidateSorter.sort(candidates);

        return limitList(candidates, maxResults);
    }

    /**
     * Recursive helper traversing Trie nodes while computing DP matrix rows.
     */
    private void searchFuzzyRecursive(TrieNode node, char currentChar, String target,
                                      int[] prevRow, StringBuilder currentWord,
                                      int maxDistance, CustomList<Candidate> candidates) {
        int targetLen = target.length();
        int[] currentRow = new int[targetLen + 1];
        // Cost of insertion at current prefix length
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

        // If current node forms a valid word and its edit distance is within budget
        if (node.isEndOfWord() && currentRow[targetLen] <= maxDistance) {
            candidates.add(new Candidate(currentWord.toString(), currentRow[targetLen], node.getFrequency()));
        }

        // Pruning: if the minimum edit distance in currentRow exceeds maxDistance,
        // no descendant node can possibly match within maxDistance.
        if (rowMin <= maxDistance) {
            TrieNode[] children = node.getChildren();
            for (int i = 0; i < TrieNode.ALPHABET_SIZE; i++) {
                if (children[i] != null) {
                    char nextChar = (char) ('a' + i);
                    currentWord.append(nextChar);
                    searchFuzzyRecursive(children[i], nextChar, target, currentRow, currentWord, maxDistance, candidates);
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

