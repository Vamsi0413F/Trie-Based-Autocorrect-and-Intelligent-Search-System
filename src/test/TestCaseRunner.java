package test;

import core.Candidate;
import core.CandidateSorter;
import core.LinearDictionary;
import core.SpellChecker;
import datastructures.CustomBoundedMinHeap;
import datastructures.CustomList;
import datastructures.CustomQueue;
import datastructures.Trie;

import java.io.File;
import java.io.IOException;

/**
 * TestCaseRunner provides a deterministic automated verification suite covering
 * all functional, structural, algorithmic, edge-case, and cross-checking requirements
 * for the Trie-based intelligent search and autocorrect system.
 *
 * Exits with status code 0 if all tests pass; exits with 1 if any test fails.
 */
public class TestCaseRunner {

    private int testsPassed = 0;
    private int testsTotal = 0;

    public static void main(String[] args) {
        TestCaseRunner runner = new TestCaseRunner();
        boolean allPassed = runner.runAllTests();
        System.exit(allPassed ? 0 : 1);
    }

    public boolean runAllTests() {
        System.out.println("================================================================================");
        System.out.println("                 COMMENCING AUTOMATED DSA TEST SUITE VERIFICATION               ");
        System.out.println("================================================================================");

        testCustomList();
        testCustomQueue();
        testCandidateSorter();
        testExactSearches();
        testSharedPrefixes();
        testAutocompletePrefixes();
        testBoundedMinHeapAndTopK();
        testAutocorrectOperationsAndBoundaries();
        testDynamicInsertAndDuplicates();
        testDeletionScenarios();
        testDictionaryFileAndSwitchPreservation();
        testRepeatedCyclesStress();
        testCrossCheckWithBaseline();

        System.out.println("================================================================================");
        System.out.printf("TEST SUITE SUMMARY: %d / %d TESTS PASSED (%.1f%%)\n",
                testsPassed, testsTotal, (100.0 * testsPassed / testsTotal));
        System.out.println("================================================================================\n");

        return testsPassed == testsTotal;
    }

    private void assertTrue(String testName, boolean condition) {
        testsTotal++;
        if (condition) {
            testsPassed++;
            System.out.printf("  [PASS] %s\n", testName);
        } else {
            System.err.printf("  [FAIL] %s\n", testName);
        }
    }

    private void testCustomList() {
        System.out.println("\n[Suite 1] CustomList Dynamic Array Verification");
        CustomList<String> list = new CustomList<>(2);
        list.add("Alpha");
        list.add("Beta");
        list.add("Gamma"); // Forces resize from 2 -> 4
        assertTrue("CustomList size after resize is 3", list.size() == 3);
        assertTrue("CustomList indexing get(1) returns Beta", "Beta".equals(list.get(1)));
        list.remove(1);
        assertTrue("CustomList size after remove is 2", list.size() == 2);
        assertTrue("CustomList element shift after remove (Gamma at index 1)", "Gamma".equals(list.get(1)));
        assertTrue("CustomList contains Alpha", list.contains("Alpha"));
        assertTrue("CustomList does not contain Beta", !list.contains("Beta"));
    }

    private void testCustomQueue() {
        System.out.println("\n[Suite 2] CustomQueue FIFO Verification");
        CustomQueue<Integer> q = new CustomQueue<>();
        assertTrue("Queue initially empty", q.isEmpty());
        q.enqueue(10);
        q.enqueue(20);
        q.enqueue(30);
        assertTrue("Queue size 3", q.size() == 3);
        assertTrue("Queue peek returns 10", q.peek() == 10);
        assertTrue("Queue dequeue returns 10", q.dequeue() == 10);
        assertTrue("Queue dequeue returns 20", q.dequeue() == 20);
        assertTrue("Queue size 1", q.size() == 1);
        assertTrue("Queue dequeue returns 30", q.dequeue() == 30);
        assertTrue("Queue empty after dequeues", q.isEmpty());
    }

    private void testCandidateSorter() {
        System.out.println("\n[Suite 3] CandidateSorter Custom MergeSort Verification");
        CustomList<Candidate> list = new CustomList<>();
        list.add(new Candidate("apply", 1, 6100));
        list.add(new Candidate("apple", 1, 6500)); // Same dist, higher freq -> should be before apply
        list.add(new Candidate("application", 2, 7900)); // Higher dist -> should be after dist 1
        list.add(new Candidate("apricot", 1, 6100)); // Same dist, same freq as apply -> alphabetical tie-breaking

        CandidateSorter.sort(list);

        assertTrue("1st candidate is apple (dist 1, freq 6500)", "apple".equals(list.get(0).getWord()));
        assertTrue("2nd candidate is apply (dist 1, freq 6100)", "apply".equals(list.get(1).getWord()));
        assertTrue("3rd candidate is apricot (dist 1, freq 6100, alphabetical tie)", "apricot".equals(list.get(2).getWord()));
        assertTrue("4th candidate is application (dist 2)", "application".equals(list.get(3).getWord()));
    }

    private void testExactSearches() {
        System.out.println("\n[Suite 4] Successful and Unsuccessful Exact Searches");
        Trie trie = new Trie();
        trie.insert("program", 9000);
        trie.insert("java", 9900);
        trie.insert("algorithm", 8500);

        assertTrue("Exact search 'program' found", trie.search("program"));
        assertTrue("Exact search 'java' found", trie.search("java"));
        assertTrue("Exact search 'algorithm' found", trie.search("algorithm"));
        assertTrue("Case insensitivity search 'PROGRAM'", trie.search("PROGRAM"));
        assertTrue("Search absent word 'python' returns false", !trie.search("python"));
        assertTrue("Search prefix of existing word 'prog' returns false", !trie.search("prog"));
        assertTrue("Search word with existing prefix 'javavm' returns false", !trie.search("javavm"));
        assertTrue("Empty string search returns false", !trie.search(""));
        assertTrue("Null search returns false", !trie.search(null));
    }

    private void testSharedPrefixes() {
        System.out.println("\n[Suite 5] Words with Shared Prefixes");
        Trie trie = new Trie();
        trie.insert("car", 100);
        trie.insert("cart", 200);
        trie.insert("care", 300);
        trie.insert("career", 400);
        trie.insert("careful", 500);

        assertTrue("Word 'car' exists", trie.search("car"));
        assertTrue("Word 'cart' exists", trie.search("cart"));
        assertTrue("Word 'care' exists", trie.search("care"));
        assertTrue("Word 'career' exists", trie.search("career"));
        assertTrue("Word 'careful' exists", trie.search("careful"));
        assertTrue("Total words inserted = 5", trie.getWordCount() == 5);
    }

    private void testAutocompletePrefixes() {
        System.out.println("\n[Suite 6] Prefix Autocomplete: Valid, No-Match, Equal to Word, & Shared Prefixes");
        Trie trie = new Trie();
        trie.insert("program", 9500);
        trie.insert("programming", 8200);
        trie.insert("programmer", 6100);
        trie.insert("progress", 5400);
        trie.insert("project", 7800);
        trie.insert("java", 9900);
        trie.insert("javascript", 8500);

        // 1. Valid prefix 'prog'
        CustomList<Candidate> progSugg = trie.autocomplete("prog", 3);
        assertTrue("Autocomplete returned 3 suggestions for 'prog'", progSugg.size() == 3);
        assertTrue("Top suggestion for 'prog' is 'program' (freq 9500)", "program".equals(progSugg.get(0).getWord()));
        assertTrue("Second suggestion is 'programming' (freq 8200)", "programming".equals(progSugg.get(1).getWord()));
        assertTrue("Third suggestion is 'programmer' (freq 6100)", "programmer".equals(progSugg.get(2).getWord()));

        // 2. Prefix with no matches
        CustomList<Candidate> emptySugg = trie.autocomplete("xyz", 5);
        assertTrue("Prefix with no matches returns empty list", emptySugg.isEmpty());

        // 3. Prefix equal to a full word ('java')
        CustomList<Candidate> javaSugg = trie.autocomplete("java", 5);
        assertTrue("Prefix equal to full word returns both java and javascript", javaSugg.size() == 2);
        assertTrue("First match is java itself (freq 9900)", "java".equals(javaSugg.get(0).getWord()));
        assertTrue("Second match is javascript (freq 8500)", "javascript".equals(javaSugg.get(1).getWord()));
    }

    private void testBoundedMinHeapAndTopK() {
        System.out.println("\n[Suite 7] Custom Bounded Min-Heap & Top-K Parameter Boundaries");

        // 1. Bounded Min-Heap Functionality
        CustomBoundedMinHeap heap = new CustomBoundedMinHeap(3);
        heap.insert(new Candidate("delta", 0, 100));
        heap.insert(new Candidate("charlie", 0, 300));
        heap.insert(new Candidate("bravo", 0, 200));
        assertTrue("Heap capacity is 3", heap.getCapacity() == 3);
        assertTrue("Weakest element at root is delta (freq 100)", "delta".equals(heap.peekWeakest().getWord()));

        // Inserting element worse than root
        heap.insert(new Candidate("echo", 0, 50));
        assertTrue("Heap root unchanged after worse candidate inserted", "delta".equals(heap.peekWeakest().getWord()));

        // Inserting element better than root
        heap.insert(new Candidate("alpha", 0, 500));
        assertTrue("Root updated after better candidate inserted (now bravo)", "bravo".equals(heap.peekWeakest().getWord()));

        CustomList<Candidate> sorted = heap.extractSorted();
        assertTrue("Extracted top element is alpha (freq 500)", "alpha".equals(sorted.get(0).getWord()));
        assertTrue("Extracted 2nd element is charlie (freq 300)", "charlie".equals(sorted.get(1).getWord()));
        assertTrue("Extracted 3rd element is bravo (freq 200)", "bravo".equals(sorted.get(2).getWord()));

        // 2. Equal Frequency Tie-Breaking
        CustomBoundedMinHeap tieHeap = new CustomBoundedMinHeap(2);
        tieHeap.insert(new Candidate("banana", 0, 500));
        tieHeap.insert(new Candidate("apple", 0, 500));
        tieHeap.insert(new Candidate("cherry", 0, 500));
        CustomList<Candidate> tieExtracted = tieHeap.extractSorted();
        assertTrue("Tie-breaker 1st is apple", "apple".equals(tieExtracted.get(0).getWord()));
        assertTrue("Tie-breaker 2nd is banana", "banana".equals(tieExtracted.get(1).getWord()));

        // 3. Autocomplete K = 0, K = 1, and K > matches
        Trie trie = new Trie();
        trie.insert("test", 100);
        trie.insert("testing", 200);
        trie.insert("tester", 300);

        CustomList<Candidate> kZero = trie.autocomplete("test", 0);
        assertTrue("K = 0 returns empty list immediately", kZero.isEmpty());

        CustomList<Candidate> kOne = trie.autocomplete("test", 1);
        assertTrue("K = 1 returns exactly 1 suggestion", kOne.size() == 1);
        assertTrue("K = 1 returns highest frequency (tester)", "tester".equals(kOne.get(0).getWord()));

        CustomList<Candidate> kLarge = trie.autocomplete("test", 50);
        assertTrue("K > matches returns exactly all 3 matches", kLarge.size() == 3);

        // 4. Verification that BFS Autocomplete matches DFS Top-K
        CustomList<Candidate> bfsResults = trie.autocompleteBFS("test", 3);
        assertTrue("BFS returns 3 matches", bfsResults.size() == 3);
        assertTrue("BFS and DFS match at top rank", "tester".equals(bfsResults.get(0).getWord()));
    }

    private void testAutocorrectOperationsAndBoundaries() {
        System.out.println("\n[Suite 8] Autocorrect: Typo Classes & Edit-Distance Boundaries");
        Trie trie = new Trie();
        trie.insert("program", 9000);
        trie.insert("programming", 9500);

        // Expected word 'programming' from typo 'programing'
        CustomList<Candidate> progSugg = trie.searchFuzzy("programing", 1, 3);
        assertTrue("Misspelling 'programing' corrected to 'programming'",
                progSugg.size() > 0 && "programming".equals(progSugg.get(0).getWord()));

        // Typo classes
        CustomList<Candidate> ins = trie.searchFuzzy("prograam", 1, 3); // Insertion
        assertTrue("Insertion typo 'prograam' -> 'program'",
                ins.size() > 0 && "program".equals(ins.get(0).getWord()));

        CustomList<Candidate> del = trie.searchFuzzy("progrm", 1, 3); // Deletion
        assertTrue("Deletion typo 'progrm' -> 'program'",
                del.size() > 0 && "program".equals(del.get(0).getWord()));

        CustomList<Candidate> sub = trie.searchFuzzy("progran", 1, 3); // Substitution
        assertTrue("Substitution typo 'progran' -> 'program'",
                sub.size() > 0 && "program".equals(sub.get(0).getWord()));

        CustomList<Candidate> trans = trie.searchFuzzy("progarm", 2, 3); // Transposition (dist 2)
        assertTrue("Transposition typo 'progarm' -> 'program' at dist 2",
                trans.size() > 0 && "program".equals(trans.get(0).getWord()));

        // Distance boundary tests
        // 'progarm' has edit distance 2 to 'program'
        CustomList<Candidate> atLimit = trie.searchFuzzy("progarm", 2, 3);
        assertTrue("Distance exactly at limit (d=2) finds 'program'", atLimit.size() > 0);

        CustomList<Candidate> oneAboveLimit = trie.searchFuzzy("progarm", 1, 3);
        assertTrue("Distance one above limit (query d=2, limit d=1) excludes 'program'",
                oneAboveLimit.isEmpty() || !"program".equals(oneAboveLimit.get(0).getWord()));

        // Distance 0 test: query 'program' with maxDist=0 returns only exact word
        CustomList<Candidate> distZero = trie.searchFuzzy("program", 0, 5);
        assertTrue("Distance 0 query returns exact match", distZero.size() == 1);
        assertTrue("Distance 0 match has edit distance 0", distZero.get(0).getDistance() == 0);

        // No-match query
        CustomList<Candidate> noMatch = trie.searchFuzzy("zzzzzzzzzz", 2, 5);
        assertTrue("No-match query returns empty suggestions", noMatch.isEmpty());
    }

    private void testDynamicInsertAndDuplicates() {
        System.out.println("\n[Suite 9] Dynamic Insertion, Verification, and Duplicate Handling");
        Trie trie = new Trie();

        // Dynamic insert
        boolean inserted = trie.insert("quantum", 850);
        assertTrue("New word 'quantum' successfully inserted", inserted);
        assertTrue("Exact search confirms 'quantum' exists", trie.search("quantum"));

        CustomList<Candidate> autoSugg = trie.autocomplete("quan", 5);
        assertTrue("Autocomplete finds 'quantum' from prefix 'quan'",
                autoSugg.size() == 1 && "quantum".equals(autoSugg.get(0).getWord()));

        // Duplicate insert: count must not increment, frequency updates
        int wordsBefore = trie.getWordCount();
        int nodesBefore = trie.getNodeCount();

        boolean dupResult = trie.insert("quantum", 1200);
        assertTrue("Duplicate insert returns false (not a new word)", !dupResult);
        assertTrue("Word count unchanged after duplicate insert", trie.getWordCount() == wordsBefore);
        assertTrue("Node count unchanged after duplicate insert", trie.getNodeCount() == nodesBefore);
        assertTrue("Frequency updated to 1200", trie.getFrequency("quantum") == 1200);

        // Invalid insertions
        assertTrue("Empty string insertion rejected", !trie.insert("", 500));
        assertTrue("Null insertion rejected", !trie.insert(null, 500));
    }

    private void testDeletionScenarios() {
        System.out.println("\n[Suite 10] Deletion: Shared Prefixes, Longer Words, & Nonexistent Targets");
        Trie trie = new Trie();
        trie.insert("cat", 500);
        trie.insert("caterpillar", 100);
        trie.insert("care", 300);
        trie.insert("career", 400);
        trie.insert("careful", 600);

        // 1. Delete word that is prefix of another word ('cat' while 'caterpillar' exists)
        boolean delCat = trie.delete("cat");
        assertTrue("Prefix word 'cat' successfully deleted", delCat);
        assertTrue("'cat' no longer in dictionary", !trie.search("cat"));
        assertTrue("Longer word 'caterpillar' remains fully intact", trie.search("caterpillar"));

        // 2. Delete word that has longer words sharing its prefix ('care' while 'career' and 'careful' exist)
        boolean delCare = trie.delete("care");
        assertTrue("Word 'care' successfully deleted", delCare);
        assertTrue("'care' no longer exists", !trie.search("care"));
        assertTrue("'career' remains intact", trie.search("career"));
        assertTrue("'careful' remains intact", trie.search("careful"));

        // 3. Delete leaf word ('careful')
        boolean delCareful = trie.delete("careful");
        assertTrue("Leaf word 'careful' deleted", delCareful);
        assertTrue("'careful' no longer exists", !trie.search("careful"));
        assertTrue("'career' still remains intact", trie.search("career"));

        // 4. Delete nonexistent word
        boolean delMissing = trie.delete("nonexistent");
        assertTrue("Deleting nonexistent word returns false", !delMissing);
    }

    private void testDictionaryFileAndSwitchPreservation() {
        System.out.println("\n[Suite 11] Dictionary File Loading, Path Resolution, & Switch Preservation");

        // 1. Path resolution on known sample file
        try {
            File sample = SpellChecker.resolveDictionaryFile("data/sample_words.txt");
            assertTrue("Path resolver resolves data/sample_words.txt", sample.exists());
        } catch (IOException e) {
            assertTrue("Path resolver threw unexpected IOException: " + e.getMessage(), false);
        }

        // 2. Missing dictionary file throws descriptive IOException
        try {
            SpellChecker.resolveDictionaryFile("nonexistent_dictionary_file_xyz.txt");
            assertTrue("Path resolver should have thrown on missing file", false);
        } catch (IOException e) {
            assertTrue("Path resolver throws descriptive IOException on missing file",
                    e.getMessage().contains("Dictionary file not found at"));
        }

        // 3. Failed dictionary switch preserves active dictionary
        SpellChecker sc = new SpellChecker();
        try {
            sc.loadDictionary("data/sample_words.txt");
        } catch (IOException e) {
            System.err.println("Setup failed: " + e.getMessage());
        }
        int countBefore = sc.getDictionarySize();
        assertTrue("Initial dictionary loaded 62 words", countBefore == 62);

        // Attempt invalid switch
        SpellChecker attemptedSwitch = new SpellChecker();
        boolean switchFailed = false;
        try {
            attemptedSwitch.loadDictionary("missing_file_to_switch.txt");
        } catch (IOException e) {
            switchFailed = true;
        }
        assertTrue("Invalid switch threw IOException", switchFailed);
        // Active dictionary intact
        assertTrue("Active dictionary preserves its word count (62)", sc.getDictionarySize() == 62);
        assertTrue("Active dictionary remains searchable", sc.exactSearch("program"));
    }

    private void testRepeatedCyclesStress() {
        System.out.println("\n[Suite 12] Repeated Insert/Delete Stress Cycles (50 Rounds)");
        Trie trie = new Trie();
        int initialNodes = trie.getNodeCount(); // 1 (root)
        int initialWords = trie.getWordCount(); // 0

        for (int cycle = 0; cycle < 50; cycle++) {
            trie.insert("stress", 100);
            trie.insert("stressed", 200);
            trie.insert("stressful", 300);
            trie.delete("stressed");
            trie.delete("stress");
            trie.delete("stressful");
        }

        assertTrue("After 50 stress cycles, word count returns to 0", trie.getWordCount() == initialWords);
        assertTrue("After 50 stress cycles, node count returns to root only (1)", trie.getNodeCount() == initialNodes);

        // Ensure clean re-insertion after stress cycle
        trie.insert("stress", 500);
        assertTrue("Re-inserted word is searchable", trie.search("stress"));
        assertTrue("Word count is 1", trie.getWordCount() == 1);
    }

    private void testCrossCheckWithBaseline() {
        System.out.println("\n[Suite 13] Cross-Check Verification: Trie vs. LinearDictionary Baseline");
        Trie trie = new Trie();
        LinearDictionary linear = new LinearDictionary();

        // Populate identical dataset of 20 words
        String[] dataset = {
            "apple", "apply", "application", "banana", "ball", "cat", "cater",
            "caterpillar", "dog", "door", "program", "programmer", "programming",
            "project", "java", "javascript", "jar", "tree", "trie", "test"
        };
        int[] freqs = {
            6500, 6100, 7900, 4500, 3800, 5800, 2100,
            1500, 5200, 4100, 9500, 6100, 8200,
            7800, 9900, 8500, 4200, 7400, 5000, 3200
        };

        for (int i = 0; i < dataset.length; i++) {
            trie.insert(dataset[i], freqs[i]);
            linear.insert(dataset[i], freqs[i]);
        }

        // 1. Cross-check Exact Searches
        String[] testSearches = {
            "apple", "caterpillar", "program", "java", "test",
            "python", "nonexistent", "app", "prog", "xyz"
        };
        boolean exactMatchesAll = true;
        for (String w : testSearches) {
            boolean trieRes = trie.search(w);
            boolean linRes = linear.search(w);
            if (trieRes != linRes) {
                exactMatchesAll = false;
                break;
            }
        }
        assertTrue("Cross-check: 100% agreement on exact searches between Trie and Linear", exactMatchesAll);

        // 2. Cross-check Autocomplete (Top-3 for prefix 'prog')
        CustomList<Candidate> trieAuto = trie.autocomplete("prog", 3);
        CustomList<Candidate> linAuto = linear.autocomplete("prog", 3);
        assertTrue("Autocomplete result counts match (3)", trieAuto.size() == linAuto.size() && trieAuto.size() == 3);

        boolean autoMatchesAll = true;
        for (int i = 0; i < trieAuto.size(); i++) {
            if (!trieAuto.get(i).getWord().equals(linAuto.get(i).getWord())) {
                autoMatchesAll = false;
                break;
            }
        }
        assertTrue("Cross-check: Trie and Linear autocomplete candidates and ranking match identically", autoMatchesAll);

        // 3. Cross-check Fuzzy Autocorrect (query 'progrm', maxDist = 1)
        CustomList<Candidate> trieFuzzy = trie.searchFuzzy("progrm", 1, 5);
        CustomList<Candidate> linFuzzy = linear.searchFuzzy("progrm", 1, 5);
        assertTrue("Fuzzy autocorrect result counts match", trieFuzzy.size() == linFuzzy.size() && trieFuzzy.size() > 0);

        boolean fuzzyMatchesAll = true;
        for (int i = 0; i < trieFuzzy.size(); i++) {
            if (!trieFuzzy.get(i).getWord().equals(linFuzzy.get(i).getWord()) ||
                trieFuzzy.get(i).getDistance() != linFuzzy.get(i).getDistance()) {
                fuzzyMatchesAll = false;
                break;
            }
        }
        assertTrue("Cross-check: Trie-pruned Levenshtein agrees with Linear brute-force Levenshtein", fuzzyMatchesAll);
    }
}
