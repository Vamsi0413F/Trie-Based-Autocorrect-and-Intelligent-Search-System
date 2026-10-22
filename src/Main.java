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
    private static final String LARGE_DICT_PATH = "data/words_alpha.txt";

    private SpellChecker spellChecker;
    private final BufferedReader reader;
    private String currentDictPath;

    public Main() {
        this(null);
    }

    public Main(String initialDictPath) {
        this.spellChecker = new SpellChecker();
        this.reader = new BufferedReader(new InputStreamReader(System.in));
        this.currentDictPath = (initialDictPath != null && !initialDictPath.trim().isEmpty())
                ? initialDictPath.trim()
                : DEFAULT_DICT_PATH;
    }

    public static void main(String[] args) {
        String cliDict = (args != null && args.length > 0) ? args[0] : null;
        Main app = new Main(cliDict);
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
                    System.out.println("\n[!] Invalid selection '" + choiceStr + "'. Please enter a number between 0 and 9.");
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
        try {
            int count = spellChecker.loadDictionary(currentDictPath);
            System.out.printf("[✓] Loaded %d dictionary words from: %s\n\n", count, currentDictPath);
        } catch (IOException e1) {
            System.err.println("[!] Notice: Could not load primary dictionary at: " + currentDictPath);
            if (!SAMPLE_DICT_PATH.equals(currentDictPath)) {
                System.out.println("    Attempting fallback to sample dictionary: " + SAMPLE_DICT_PATH);
                try {
                    int count = spellChecker.loadDictionary(SAMPLE_DICT_PATH);
                    currentDictPath = SAMPLE_DICT_PATH;
                    System.out.printf("[✓] Loaded %d fallback dictionary words from: %s\n\n", count, currentDictPath);
                } catch (IOException e2) {
                    System.err.println("[!] Error: Could not load fallback dictionary either: " + e2.getMessage());
                    System.err.println("    Starting with an empty dictionary. You can load words via Option 4 or 9.");
                }
            }
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
        System.out.println("  [9] Switch Dictionary File  : Choose full (25k), large (370k), or sample (62)");
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
        if (!isPureAlphabetical(word)) {
            System.out.printf("[!] Invalid input: Search word must contain alphabetic letters (a-z) only. Found: '%s'\n", word);
            return;
        }

        String clean = word.toLowerCase();
        long start = System.nanoTime();
        boolean found = spellChecker.exactSearch(clean);
        long elapsed = System.nanoTime() - start;

        if (found) {
            int freq = spellChecker.getFrequency(clean);
            System.out.printf("[✓] Exact match FOUND: '%s' (Corpus Frequency: %d) [Time: %.3f µs]\n",
                    clean, freq, elapsed / 1000.0);
        } else {
            System.out.printf("[✗] Word '%s' NOT FOUND in dictionary. [Time: %.3f µs]\n",
                    clean, elapsed / 1000.0);
            System.out.println("    Checking for spelling corrections...");
            CustomList<Candidate> corrections = spellChecker.autocorrect(clean, 2, 3);
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
        if (!isPureAlphabetical(prefix)) {
            System.out.printf("[!] Invalid input: Prefix must contain alphabetic letters (a-z) only. Found: '%s'\n", prefix);
            return;
        }

        System.out.print("Maximum suggestions to display [default 5]: ");
        String limitStr = readLineTrimmed();
        int limit = 5;
        if (!limitStr.isEmpty()) {
            try {
                int parsed = Integer.parseInt(limitStr);
                if (parsed > 0) {
                    limit = parsed;
                } else {
                    System.out.println("[!] Limit must be positive. Using default (5).");
                }
            } catch (NumberFormatException ignored) {
                System.out.println("[!] Non-numeric limit entered. Using default (5).");
            }
        }

        String clean = prefix.toLowerCase();
        long start = System.nanoTime();
        CustomList<Candidate> suggestions = spellChecker.autocomplete(clean, limit);
        long elapsed = System.nanoTime() - start;

        System.out.printf("\nAutocomplete suggestions for prefix '%s' [Time: %.3f µs]:\n",
                clean, elapsed / 1000.0);

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
        if (!isPureAlphabetical(word)) {
            System.out.printf("[!] Invalid input: Word must contain alphabetic letters (a-z) only. Found: '%s'\n", word);
            return;
        }

        System.out.print("Maximum edit distance allowed [1 or 2, default 2]: ");
        String distStr = readLineTrimmed();
        int maxDist = 2;
        if (!distStr.isEmpty()) {
            try {
                int parsed = Integer.parseInt(distStr);
                if (parsed >= 0 && parsed <= 3) {
                    maxDist = parsed;
                } else {
                    System.out.println("[!] Distance outside recommended range [0-3]. Defaulting to 2.");
                }
            } catch (NumberFormatException ignored) {
                System.out.println("[!] Non-numeric distance entered. Defaulting to 2.");
            }
        }

        String clean = word.toLowerCase();
        long start = System.nanoTime();
        CustomList<Candidate> candidates = spellChecker.autocorrect(clean, maxDist, 5);
        long elapsed = System.nanoTime() - start;

        System.out.printf("\nAutocorrect suggestions for '%s' (max edit dist = %d) [Time: %.3f µs]:\n",
                clean, maxDist, elapsed / 1000.0);

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
        if (!isPureAlphabetical(word)) {
            System.out.printf("[!] Invalid input: Word to insert must contain alphabetic letters (a-z) only. Rejected: '%s'\n", word);
            return;
        }

        System.out.print("Enter corpus frequency [e.g. 1000]: ");
        String freqStr = readLineTrimmed();
        int freq = 1000;
        if (!freqStr.isEmpty()) {
            try {
                int parsed = Integer.parseInt(freqStr);
                if (parsed > 0) {
                    freq = parsed;
                } else {
                    System.out.println("[!] Frequency must be positive. Defaulting to 1000.");
                }
            } catch (NumberFormatException ignored) {
                System.out.println("[!] Non-numeric frequency entered. Defaulting to 1000.");
            }
        }

        String clean = word.toLowerCase();
        boolean isNew = spellChecker.insertWord(clean, freq);
        if (isNew) {
            System.out.printf("[✓] Successfully inserted new word '%s' with frequency %d.\n", clean, freq);
        } else {
            System.out.printf("[✓] Word '%s' already existed; updated frequency to %d.\n", clean, freq);
        }
    }

    private void handleDeleteWord() {
        System.out.print("\nEnter word to delete: ");
        String word = readLineTrimmed();
        if (word.isEmpty()) {
            System.out.println("[!] Word cannot be empty.");
            return;
        }
        if (!isPureAlphabetical(word)) {
            System.out.printf("[!] Invalid input: Word to delete must contain alphabetic letters (a-z) only. Found: '%s'\n", word);
            return;
        }

        String clean = word.toLowerCase();
        boolean deleted = spellChecker.deleteWord(clean);
        if (deleted) {
            System.out.printf("[✓] Successfully deleted '%s' from dictionary.\n", clean);
        } else {
            System.out.printf("[✗] Word '%s' was not found in dictionary.\n", clean);
        }
    }

    private void handleDictionaryStats() {
        int words = spellChecker.getDictionarySize();
        int nodes = spellChecker.getNodeCount();
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
        System.out.println("  1. Full Dictionary     (" + DEFAULT_DICT_PATH + " ~ 25,000 words  | frequencies included)");
        System.out.println("  2. Large Dictionary    (" + LARGE_DICT_PATH   + " ~ 370,105 words | public domain, uniform freq=1)");
        System.out.println("  3. Sample Dictionary   (" + SAMPLE_DICT_PATH  + " ~ 62 curated words)");
        System.out.println("  4. Custom path         (enter a relative path from project root)");
        System.out.print("Select [1 / 2 / 3 / 4]: ");
        String sel = readLineTrimmed();

        String target;
        if ("2".equals(sel)) {
            target = LARGE_DICT_PATH;
        } else if ("3".equals(sel)) {
            target = SAMPLE_DICT_PATH;
        } else if ("4".equals(sel)) {
            System.out.print("Enter dictionary path: ");
            target = readLineTrimmed();
            if (target.isEmpty()) {
                System.out.println("[!] No path entered. Keeping current dictionary.");
                return;
            }
        } else {
            target = DEFAULT_DICT_PATH;
        }

        System.out.printf("Loading '%s' — please wait...%n", target);
        SpellChecker newChecker = new SpellChecker();
        try {
            long t0 = System.currentTimeMillis();
            int loaded = newChecker.loadDictionary(target);
            long elapsed = System.currentTimeMillis() - t0;
            this.spellChecker = newChecker;
            this.currentDictPath = target;
            System.out.printf("[✓] Switched to '%s': %,d unique words loaded in %d ms (nodes: %,d).%n",
                    target, loaded, elapsed, newChecker.getNodeCount());
        } catch (IOException e) {
            System.err.println("[!] Failed to switch dictionary: " + e.getMessage());
            System.out.printf("    Active dictionary preserved: %s (%,d words).%n",
                    currentDictPath, spellChecker.getDictionarySize());
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

    /**
     * Checks if input contains exclusively English alphabetic letters.
     */
    private static boolean isPureAlphabetical(String str) {
        if (str == null || str.isEmpty()) return false;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'))) {
                return false;
            }
        }
        return true;
    }
}
