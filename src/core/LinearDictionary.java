package core;

import datastructures.CustomList;

/**
 * LinearDictionary implements a conventional list-based dictionary baseline.
 * It stores words sequentially and performs linear scans for exact matching,
 * prefix matching, and edit-distance evaluation.
 *
 * This class serves as the empirical comparison baseline to evaluate the
 * asymptotic superiority of the Trie data structure.
 */
public class LinearDictionary {

    public static class WordEntry {
        public final String word;
        public final int frequency;

        public WordEntry(String word, int frequency) {
            this.word = word;
            this.frequency = frequency;
        }
    }

    private final CustomList<WordEntry> entries;

    /**
     * Initializes an empty LinearDictionary.
     */
    public LinearDictionary() {
        this.entries = new CustomList<>();
    }

    /**
     * Inserts a word entry at the tail of the list.
     * Time Complexity: O(1) amortized.
     *
     * @param word word string
     * @param frequency frequency rank
     */
    public void insert(String word, int frequency) {
        if (word == null) return;
        String clean = sanitize(word);
        if (clean.isEmpty()) return;
        entries.add(new WordEntry(clean, frequency));
    }

    /**
     * Deletes a word from the linear dictionary if present.
     * Keeps state synchronized with Trie.
     *
     * @param target the word to delete
     * @return true if found and removed, false otherwise
     */
    public boolean delete(String target) {
        if (target == null) return false;
        String clean = sanitize(target);
        if (clean.isEmpty()) return false;

        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).word.equals(clean)) {
                entries.remove(i);
                return true;
            }
        }
        return false;
    }

    /**
     * Clears all entries from the linear dictionary.
     */
    public void clear() {
        entries.clear();
    }

    /**
     * Performs a sequential linear search across all N dictionary entries.
     * Time Complexity: O(N * L) in the worst and average cases.
     * Auxiliary Space: O(1)
     *
     * @param target the target word
     * @return true if exact word found, false otherwise
     */
    public boolean search(String target) {
        if (target == null) return false;
        String clean = sanitize(target);
        if (clean.isEmpty()) return false;

        for (int i = 0; i < entries.size(); i++) {
            WordEntry entry = entries.get(i);
            if (entry.word.equals(clean)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Linearly scans all N entries to find words starting with the prefix.
     * Time Complexity: O(N * P + M log M)
     *
     * @param prefix the prefix string
     * @param maxResults maximum results to return
     * @return ranked CustomList of Candidate suggestions
     */
    public CustomList<Candidate> autocomplete(String prefix, int maxResults) {
        CustomList<Candidate> matches = new CustomList<>();
        if (prefix == null) return matches;
        String clean = sanitize(prefix);
        if (clean.isEmpty()) return matches;

        for (int i = 0; i < entries.size(); i++) {
            WordEntry entry = entries.get(i);
            if (entry.word.startsWith(clean)) {
                matches.add(new Candidate(entry.word, 0, entry.frequency));
            }
        }

        CandidateSorter.sort(matches);
        return limitList(matches, maxResults);
    }

    /**
     * Linearly computes the Levenshtein distance for every single word in the dictionary.
     * Time Complexity: O(N * L_query * L_word + M log M)
     *
     * @param target misspelled query
     * @param maxDistance maximum edit distance allowed
     * @param maxResults maximum results to return
     * @return ranked CustomList of Candidate suggestions
     */
    public CustomList<Candidate> searchFuzzy(String target, int maxDistance, int maxResults) {
        CustomList<Candidate> matches = new CustomList<>();
        if (target == null) return matches;
        String clean = sanitize(target);
        if (clean.isEmpty()) return matches;

        for (int i = 0; i < entries.size(); i++) {
            WordEntry entry = entries.get(i);
            int dist = computeLevenshtein(clean, entry.word);
            if (dist <= maxDistance) {
                matches.add(new Candidate(entry.word, dist, entry.frequency));
            }
        }

        CandidateSorter.sort(matches);
        return limitList(matches, maxResults);
    }

    /**
     * Computes the standard Wagner-Fischer Levenshtein distance between two strings.
     * Time Complexity: O(L1 * L2)
     * Auxiliary Space: O(L2) using 2-row rolling array optimization.
     */
    public static int computeLevenshtein(String s1, String s2) {
        int len1 = s1.length();
        int len2 = s2.length();

        int[] prev = new int[len2 + 1];
        int[] curr = new int[len2 + 1];

        for (int j = 0; j <= len2; j++) {
            prev[j] = j;
        }

        for (int i = 1; i <= len1; i++) {
            curr[0] = i;
            char c1 = s1.charAt(i - 1);
            for (int j = 1; j <= len2; j++) {
                char c2 = s2.charAt(j - 1);
                int cost = (c1 == c2) ? 0 : 1;
                int insertOp = curr[j - 1] + 1;
                int deleteOp = prev[j] + 1;
                int replaceOp = prev[j - 1] + cost;

                int minOp = insertOp;
                if (deleteOp < minOp) minOp = deleteOp;
                if (replaceOp < minOp) minOp = replaceOp;
                curr[j] = minOp;
            }
            // Swap row buffers
            int[] temp = prev;
            prev = curr;
            curr = temp;
        }

        return prev[len2];
    }

    private static String sanitize(String str) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = Character.toLowerCase(str.charAt(i));
            if (c >= 'a' && c <= 'z') {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private CustomList<Candidate> limitList(CustomList<Candidate> list, int limit) {
        if (list.size() <= limit) return list;
        CustomList<Candidate> trimmed = new CustomList<>(limit);
        for (int i = 0; i < limit; i++) {
            trimmed.add(list.get(i));
        }
        return trimmed;
    }

    public int size() {
        return entries.size();
    }
}

