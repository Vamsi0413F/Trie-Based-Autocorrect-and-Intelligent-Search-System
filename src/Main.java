import benchmark.PerformanceBenchmark;
import core.Candidate;
import core.SpellChecker;
import datastructures.CustomList;
import test.TestCaseRunner;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.IOException;

/**
 * Main application console driver for the Trie-Based Autocorrect and Intelligent
 * Search System. Provides a menu-driven interface with zero reliance on java.util collections.
 */
public class Main {
    private static final String DEFAULT_DICT_PATH = "data/dictionary.txt";
    private static final String SAMPLE_DICT_PATH = "data/sample_words.txt";

    private SpellChecker spellChecker;
    private final BufferedReader reader;
    private String currentDictPath;

    public Main() {
        this.spellChecker = new SpellChecker();
        this.reader = new BufferedReader(new InputStreamReader(System.in));
        this.currentDictPath = DEFAULT_DICT_PATH;
    }

    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }

    public void start() {
        printBanner();
        loadInitialDictionary();

        boolean running = true;
        while (running) {
            printMenu();
            System.out.print("Enter your choice [0-9]: ");
            String choiceStr = readLineTrimmed();

            switch (choiceStr) {
                case "1":
                    handleExactSearch();
                    break;
                case "2":
                    handleAutocomplete();
                    break;
                case "3":
                    handleAutocorrect();
                    break;
                case "4":
                    handleInsertWord();
                    break;
                case "5":
                    handleDeleteWord();
                    break;
                case "6":
                    handleDictionaryStats();
                    break;
                case "7":
                    handleRunBenchmark();
                    break;
                case "8":
                    handleRunTests();
                    break;
                case "9":
                    handleSwitchDictionary();
                    break;
                case "0":
                    System.out.println("\nExiting Trie Search System. Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("\n[!] Invalid selection. Please enter a number between 0 and 9.");
            }
        }
    }

    private void printBanner() {
        System.out.println("================================================================================");
        System.out.println("     TRIE-BASED AUTOCORRECT AND INTELLIGENT SEARCH SYSTEM (JAVA DSA)            ");
        System.out.println("            Pure First-Principles Implementation (Zero java.util)               ");
        System.out.println("================================================================================");
    }

    private void loadInitialDictionary() {
        File file = new File(DEFAULT_DICT_PATH);
        String pathToLoad = file.exists() ? DEFAULT_DICT_PATH : SAMPLE_DICT_PATH;
        try {
            int count = spellChecker.loadDictionary(pathToLoad);
            currentDictPath = pathToLoad;
            System.out.printf("[✓] Loaded %d dictionary words from: %s\n\n", count, currentDictPath);
        } catch (IOException e) {
            System.err.println("[!] Warning: Could not auto-load dictionary: " + e.getMessage());
        }
    }

    private void printMenu() {
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("                          MAIN CONTROL MENU                                     ");
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("  [1] Exact Word Search       : Check if word exists in dictionary");
        System.out.println("  [2] Prefix Autocomplete     : Retrieve top completions for a prefix");
        System.out.println("  [3] Intelligent Autocorrect : Suggest corrections using Levenshtein distance");
        System.out.println("  [4] Insert New Word         : Add word with custom corpus frequency");
        System.out.println("  [5] Delete Word             : Prune word without affecting shared prefixes");
        System.out.println("  [6] Dictionary Statistics   : Inspect node count, total words, and memory");
        System.out.println("  [7] Performance Benchmark   : Compare Trie Search vs Linear Search");
        System.out.println("  [8] Run Automated Tests     : Verify correctness across 10 DSA test suites");
        System.out.println("  [9] Switch Dictionary File  : Toggle between full (25,000) & sample (62)");
        System.out.println("  [0] Exit Application");
        System.out.println("--------------------------------------------------------------------------------");
    }

    private void handleExactSearch() {
        System.out.print("\nEnter word to search: ");
        String word = readLineTrimmed();
        if (word.isEmpty()) {
            System.out.println("[!] Input cannot be empty.");
            return;
        }

        long start = System.nanoTime();
        boolean found = spellChecker.exactSearch(word);
        long elapsed = System.nanoTime() - start;

        if (found) {
            int freq = spellChecker.getFrequency(word);
            System.out.printf("[✓] Exact match FOUND: '%s' (Corpus Frequency: %d) [Time: %.3f µs]\n",
                    word.toLowerCase(), freq, elapsed / 1000.0);
        } else {
            System.out.printf("[✗] Word '%s' NOT FOUND in dictionary. [Time: %.3f µs]\n",
                    word, elapsed / 1000.0);
            System.out.println("    Checking for spelling corrections...");
            CustomList<Candidate> corrections = spellChecker.autocorrect(word, 2, 3);
            if (corrections.isEmpty()) {
                System.out.println("    No close suggestions found within edit distance 2.");
            } else {
                System.out.println("    Did you mean:");
                for (int i = 0; i < corrections.size(); i++) {
                    Candidate c = corrections.get(i);
                    System.out.printf("      -> %-16s (Edit Distance: %d, Frequency: %d)\n",
                            c.getWord(), c.getDistance(), c.getFrequency());
                }
            }
        }
    }

    private void handleAutocomplete() {
        System.out.print("\nEnter prefix: ");
        String prefix = readLineTrimmed();
        if (prefix.isEmpty()) {
            System.out.println("[!] Prefix cannot be empty.");
            return;
        }

        System.out.print("Maximum suggestions to display [default 5]: ");
        String limitStr = readLineTrimmed();
        int limit = 5;
        try {
            if (!limitStr.isEmpty()) limit = Integer.parseInt(limitStr);
        } catch (NumberFormatException ignored) {}

        long start = System.nanoTime();
        CustomList<Candidate> suggestions = spellChecker.autocomplete(prefix, limit);
        long elapsed = System.nanoTime() - start;

        System.out.printf("\nAutocomplete suggestions for prefix '%s' [Time: %.3f µs]:\n",
                prefix.toLowerCase(), elapsed / 1000.0);

        if (suggestions.isEmpty()) {
            System.out.println("  (No words matching this prefix)");
        } else {
            for (int i = 0; i < suggestions.size(); i++) {
                Candidate c = suggestions.get(i);
                System.out.printf("  %d. %-18s [Corpus Frequency: %d]\n",
                        (i + 1), c.getWord(), c.getFrequency());
            }
        }
    }

    private void handleAutocorrect() {
        System.out.print("\nEnter misspelled word: ");
        String word = readLineTrimmed();
        if (word.isEmpty()) {
            System.out.println("[!] Input cannot be empty.");
            return;
        }

        System.out.print("Maximum edit distance allowed [1 or 2, default 2]: ");
        String distStr = readLineTrimmed();
        int maxDist = 2;
        try {
            if (!distStr.isEmpty()) maxDist = Integer.parseInt(distStr);
        } catch (NumberFormatException ignored) {}

        long start = System.nanoTime();
        CustomList<Candidate> candidates = spellChecker.autocorrect(word, maxDist, 5);
        long elapsed = System.nanoTime() - start;

        System.out.printf("\nAutocorrect suggestions for '%s' (max edit dist = %d) [Time: %.3f µs]:\n",
                word, maxDist, elapsed / 1000.0);

        if (candidates.isEmpty()) {
            System.out.println("  (No candidate corrections found within specified edit distance)");
        } else {
            for (int i = 0; i < candidates.size(); i++) {
                Candidate c = candidates.get(i);
                System.out.printf("  %d. %-18s (Edit Dist: %d, Frequency: %d)\n",
                        (i + 1), c.getWord(), c.getDistance(), c.getFrequency());
            }
        }
    }

    private void handleInsertWord() {
        System.out.print("\nEnter new word to insert: ");
        String word = readLineTrimmed();
        if (word.isEmpty()) {
            System.out.println("[!] Word cannot be empty.");
            return;
        }

        System.out.print("Enter corpus frequency [e.g. 1000]: ");
        String freqStr = readLineTrimmed();
        int freq = 1000;
        try {
            if (!freqStr.isEmpty()) freq = Integer.parseInt(freqStr);
        } catch (NumberFormatException ignored) {}

        boolean isNew = spellChecker.insertWord(word, freq);
        if (isNew) {
            System.out.printf("[✓] Successfully inserted '%s' with frequency %d.\n", word.toLowerCase(), freq);
        } else {
            System.out.printf("[✓] Word '%s' already existed; updated frequency to %d.\n", word.toLowerCase(), freq);
        }
    }

    private void handleDeleteWord() {
        System.out.print("\nEnter word to delete: ");
        String word = readLineTrimmed();
        if (word.isEmpty()) {
            System.out.println("[!] Word cannot be empty.");
            return;
        }

        boolean deleted = spellChecker.deleteWord(word);
        if (deleted) {
            System.out.printf("[✓] Successfully deleted '%s' from dictionary.\n", word.toLowerCase());
        } else {
            System.out.printf("[✗] Word '%s' was not found in dictionary.\n", word.toLowerCase());
        }
    }

    private void handleDictionaryStats() {
        int words = spellChecker.getDictionarySize();
        int nodes = spellChecker.getNodeCount();
        // Each TrieNode holds 26 references (8 bytes each on 64-bit JVM) + boolean/int fields ~ 224 bytes
        long estimatedMemoryBytes = (long) nodes * (26 * 8 + 16);
        double memoryMb = estimatedMemoryBytes / (1024.0 * 1024.0);

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("                     DICTIONARY STRUCTURE METRICS                               ");
        System.out.println("--------------------------------------------------------------------------------");
        System.out.printf("  Current Source File    : %s\n", currentDictPath);
        System.out.printf("  Total Words Stored     : %,d words\n", words);
        System.out.printf("  Total Trie Nodes       : %,d nodes\n", nodes);
        System.out.printf("  Avg Nodes per Word     : %.2f nodes/word\n", (words > 0 ? (double) nodes / words : 0.0));
        System.out.printf("  Estimated Memory Size  : %.2f MB\n", memoryMb);
        System.out.println("--------------------------------------------------------------------------------\n");
    }

    private void handleRunBenchmark() {
        System.out.println("\nExecuting comprehensive Trie vs. Linear Search benchmark...");
        try {
            PerformanceBenchmark benchmark = new PerformanceBenchmark(currentDictPath);
            benchmark.runAll();
        } catch (IOException e) {
            System.err.println("[!] Benchmark failed: " + e.getMessage());
        }
    }

    private void handleRunTests() {
        System.out.println("\nRunning automated DSA test verification suite...");
        TestCaseRunner runner = new TestCaseRunner();
        runner.runAllTests();
    }

    private void handleSwitchDictionary() {
        System.out.println("\nAvailable Dictionaries:");
        System.out.println("  1. Full Dictionary  (" + DEFAULT_DICT_PATH + " ~ 25,000 words)");
        System.out.println("  2. Sample Dictionary (" + SAMPLE_DICT_PATH + " ~ 62 curated words)");
        System.out.print("Select [1 or 2]: ");
        String sel = readLineTrimmed();

        String target = "2".equals(sel) ? SAMPLE_DICT_PATH : DEFAULT_DICT_PATH;
        SpellChecker newChecker = new SpellChecker();
        try {
            int loaded = newChecker.loadDictionary(target);
            this.spellChecker = newChecker;
            this.currentDictPath = target;
            System.out.printf("[✓] Successfully switched to %s (%d words loaded).\n", target, loaded);
        } catch (IOException e) {
            System.err.println("[!] Failed to load target dictionary: " + e.getMessage());
        }
    }

    private String readLineTrimmed() {
        try {
            String line = reader.readLine();
            return (line != null) ? line.trim() : "";
        } catch (IOException e) {
            return "";
        }
    }
}
