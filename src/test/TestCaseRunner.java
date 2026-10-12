package test;

import core.Candidate;
import core.CandidateSorter;
import core.SpellChecker;
import datastructures.CustomList;
import datastructures.CustomQueue;
import datastructures.Trie;

/**
 * TestCaseRunner provides an automated verification suite covering all functional,
 * structural, algorithmic, and edge-case requirements for the Trie-based intelligent
 * search and autocorrect system.
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
        testAutocomplete();
        testEditDistanceOperations();
        testMultiplePlausibleCorrections();
        testEdgeCasesAndDuplicates();
        testPrefixDeletion();

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
        list.add("Gamma"); // Forces resize
        assertTrue("CustomList size after resize", list.size() == 3);
        assertTrue("CustomList indexing get(1)", "Beta".equals(list.get(1)));
        list.remove(1);
        assertTrue("CustomList size after remove", list.size() == 2);
        assertTrue("CustomList element shift after remove", "Gamma".equals(list.get(1)));
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

        CandidateSorter.sort(list);

        assertTrue("First candidate is apple (dist 1, freq 6500)", "apple".equals(list.get(0).getWord()));
        assertTrue("Second candidate is apply (dist 1, freq 6100)", "apply".equals(list.get(1).getWord()));
        assertTrue("Third candidate is application (dist 2)", "application".equals(list.get(2).getWord()));
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

    private void testAutocomplete() {
        System.out.println("\n[Suite 6] Autocomplete Prefix Traversal & Frequency Ranking");
        Trie trie = new Trie();
        trie.insert("program", 9500);
        trie.insert("programming", 8200);
        trie.insert("programmer", 6100);
        trie.insert("project", 7800);

        CustomList<Candidate> suggestions = trie.autocomplete("prog", 5);
        assertTrue("Autocomplete returned 3 suggestions for 'prog'", suggestions.size() == 3);
        assertTrue("Top suggestion is 'program' (highest freq 9500)", "program".equals(suggestions.get(0).getWord()));
        assertTrue("Second suggestion is 'programming' (freq 8200)", "programming".equals(suggestions.get(1).getWord()));
        assertTrue("Third suggestion is 'programmer' (freq 6100)", "programmer".equals(suggestions.get(2).getWord()));

        CustomList<Candidate> emptyPrefix = trie.autocomplete("xyz", 5);
        assertTrue("Non-existent prefix returns empty suggestions", emptyPrefix.isEmpty());
    }

    private void testEditDistanceOperations() {
        System.out.println("\n[Suite 7] Autocorrection: Edit Distance Operations");
        Trie trie = new Trie();
        trie.insert("program", 9000);

        // Insertion typo (query has extra letter)
        CustomList<Candidate> ins = trie.searchFuzzy("prograam", 1, 3);
        assertTrue("Insertion typo 'prograam' corrected to 'program'",
                ins.size() > 0 && "program".equals(ins.get(0).getWord()));

        // Deletion typo (query is missing letter)
        CustomList<Candidate> del = trie.searchFuzzy("progrm", 1, 3);
        assertTrue("Deletion typo 'progrm' corrected to 'program'",
                del.size() > 0 && "program".equals(del.get(0).getWord()));

        // Substitution typo (query has replaced letter)
        CustomList<Candidate> sub = trie.searchFuzzy("progran", 1, 3);
        assertTrue("Substitution typo 'progran' corrected to 'program'",
                sub.size() > 0 && "program".equals(sub.get(0).getWord()));

        // Transposition typo (adjacent swap -> edit dist 2)
        CustomList<Candidate> trans = trie.searchFuzzy("progarm", 2, 3);
        assertTrue("Transposition typo 'progarm' corrected within dist 2",
                trans.size() > 0 && "program".equals(trans.get(0).getWord()));
    }

    private void testMultiplePlausibleCorrections() {
        System.out.println("\n[Suite 8] Multiple Plausible Corrections Ranking");
        Trie trie = new Trie();
        trie.insert("apple", 6500);
        trie.insert("apply", 6100);
        trie.insert("application", 7900);

        // Target typo "appl" (dist 1 to apple, dist 1 to apply)
        CustomList<Candidate> results = trie.searchFuzzy("appl", 1, 5);
        assertTrue("Two corrections within distance 1 found", results.size() == 2);
        assertTrue("Apple ranks ahead of apply due to higher frequency",
                "apple".equals(results.get(0).getWord()) && "apply".equals(results.get(1).getWord()));
    }

    private void testEdgeCasesAndDuplicates() {
        System.out.println("\n[Suite 9] Input Validation, Empty Strings, and Duplicates");
        Trie trie = new Trie();

        assertTrue("Empty string insertion rejected", !trie.insert("", 100));
        assertTrue("Null insertion rejected", !trie.insert(null, 100));
        assertTrue("Empty string search returns false", !trie.search(""));
        assertTrue("Null search returns false", !trie.search(null));

        // Duplicate insertion
        assertTrue("First insert of 'duplicate' is new", trie.insert("duplicate", 100));
        int wordsBefore = trie.getWordCount();
        assertTrue("Second insert of 'duplicate' updates existing", !trie.insert("duplicate", 500));
        assertTrue("Word count remains unchanged on duplicate", trie.getWordCount() == wordsBefore);
        assertTrue("Frequency updated to 500", trie.getFrequency("duplicate") == 500);
    }

    private void testPrefixDeletion() {
        System.out.println("\n[Suite 10] Deletion of Word That Is Prefix of Another Word");
        Trie trie = new Trie();
        trie.insert("cat", 500);
        trie.insert("caterpillar", 100);

        assertTrue("Both 'cat' and 'caterpillar' exist initially",
                trie.search("cat") && trie.search("caterpillar"));

        // Delete the prefix word 'cat'
        boolean deleted = trie.delete("cat");
        assertTrue("Prefix 'cat' successfully deleted", deleted);
        assertTrue("'cat' no longer in dictionary", !trie.search("cat"));
        assertTrue("'caterpillar' remains intact and searchable", trie.search("caterpillar"));

        // Delete 'caterpillar'
        trie.delete("caterpillar");
        assertTrue("'caterpillar' deleted", !trie.search("caterpillar"));
        assertTrue("Trie empty of words", trie.getWordCount() == 0);
    }
}

