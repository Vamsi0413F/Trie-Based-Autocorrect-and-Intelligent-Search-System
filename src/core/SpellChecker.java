package core;

import datastructures.CustomList;
import datastructures.Trie;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * SpellChecker coordinates the Trie-based dictionary engine, offering high-level
 * interfaces for dictionary file ingestion, exact word lookup, prefix autocomplete,
 * and fuzzy Levenshtein spell checking.
 */
public class SpellChecker {
    private final Trie trie;
    private final LinearDictionary linearDictionary;

    /**
     * Initializes a new SpellChecker with empty Trie and Linear dictionaries.
     */
    public SpellChecker() {
        this.trie = new Trie();
        this.linearDictionary = new LinearDictionary();
    }

    /**
     * Loads a dictionary text file where each line is formatted as:
     *   "word [optional_frequency]"
     *
     * @param filePath the path to the dictionary file
     * @return number of words successfully loaded
     * @throws IOException if file reading fails
     */
    public int loadDictionary(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("Dictionary file not found at: " + filePath);
        }

        int loadedCount = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue; // Skip comments and blank lines
                }

                String[] parts = line.split("\\s+");
                String word = parts[0];
                int frequency = 1; // Default frequency

                if (parts.length > 1) {
                    try {
                        frequency = Integer.parseInt(parts[1]);
                    } catch (NumberFormatException ignored) {
                        frequency = 1;
                    }
                }

                if (insertWord(word, frequency)) {
                    loadedCount++;
                }
            }
        }
        return loadedCount;
    }

    /**
     * Inserts a word and its frequency into both the Trie and Linear structures.
     *
     * @param word word to insert
     * @param frequency frequency rank
     * @return true if added as a new word in Trie, false otherwise
     */
    public boolean insertWord(String word, int frequency) {
        linearDictionary.insert(word, frequency);
        return trie.insert(word, frequency);
    }

    /**
     * Deletes a word from the Trie.
     *
     * @param word word to delete
     * @return true if deleted, false if word not present
     */
    public boolean deleteWord(String word) {
        return trie.delete(word);
    }

    /**
     * Performs exact word lookup.
     *
     * @param word target word
     * @return true if word exists in dictionary
     */
    public boolean exactSearch(String word) {
        return trie.search(word);
    }

    /**
     * Gets the frequency of an exact word.
     *
     * @param word target word
     * @return frequency score or 0 if not found
     */
    public int getFrequency(String word) {
        return trie.getFrequency(word);
    }

    /**
     * Returns autocomplete suggestions for a given prefix.
     *
     * @param prefix the prefix query
     * @param maxResults maximum suggestions
     * @return ranked list of Candidate suggestions
     */
    public CustomList<Candidate> autocomplete(String prefix, int maxResults) {
        return trie.autocomplete(prefix, maxResults);
    }

    /**
     * Returns spelling corrections for a misspelled word using Levenshtein distance
     * and frequency-based ranking.
     *
     * @param misspelledWord the query word
     * @param maxDistance maximum allowed edit distance (typically 1 or 2)
     * @param maxResults maximum candidate corrections
     * @return ranked list of Candidate corrections
     */
    public CustomList<Candidate> autocorrect(String misspelledWord, int maxDistance, int maxResults) {
        return trie.searchFuzzy(misspelledWord, maxDistance, maxResults);
    }

    public Trie getTrie() {
        return trie;
    }

    public LinearDictionary getLinearDictionary() {
        return linearDictionary;
    }

    public int getDictionarySize() {
        return trie.getWordCount();
    }

    public int getNodeCount() {
        return trie.getNodeCount();
    }
}

