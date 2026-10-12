package benchmark;

import core.Candidate;
import core.LinearDictionary;
import datastructures.CustomList;
import datastructures.Trie;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * PerformanceBenchmark conducts empirical runtime comparisons between
 * the Trie data structure and the Linear Search baseline across varying
 * dictionary sizes (N = 1,000 to 25,000 words).
 *
 * It measures:
 *   1. Exact Search (Hits - words present in dictionary)
 *   2. Exact Search (Misses - words absent, exposing worst-case linear traversal)
 *   3. Prefix Autocomplete
 *   4. Fuzzy Autocorrection (Trie Levenshtein Pruning vs Linear Brute-Force)
 *
 * No external collections or statistical libraries are used.
 */
public class PerformanceBenchmark {

    public static class BenchmarkResult {
        public final int datasetSize;
        public final String operation;
        public final double trieTimeUs;
        public final double linearTimeUs;
        public final double speedup;

        public BenchmarkResult(int datasetSize, String operation, double trieTimeUs, double linearTimeUs) {
            this.datasetSize = datasetSize;
            this.operation = operation;
            this.trieTimeUs = trieTimeUs;
            this.linearTimeUs = linearTimeUs;
            this.speedup = (trieTimeUs > 0) ? (linearTimeUs / trieTimeUs) : 1.0;
        }
    }

    private final String dictionaryPath;
    private final CustomList<String> allWords;
    private final CustomList<Integer> allFrequencies;

    public static void main(String[] args) {
        String dictPath = (args.length > 0) ? args[0] : "data/dictionary.txt";
        try {
            PerformanceBenchmark benchmark = new PerformanceBenchmark(dictPath);
            benchmark.runAll();
        } catch (IOException e) {
            System.err.println("Benchmark failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public PerformanceBenchmark(String dictionaryPath) throws IOException {
        this.dictionaryPath = dictionaryPath;
        this.allWords = new CustomList<>(30000);
        this.allFrequencies = new CustomList<>(30000);
        loadCorpus();
    }

    private void loadCorpus() throws IOException {
        File file = new File(dictionaryPath);
        if (!file.exists()) {
            throw new IOException("Benchmark dictionary file not found: " + dictionaryPath);
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split("\\s+");
                String w = Trie.sanitize(parts[0]);
                if (w.isEmpty()) continue;
                int freq = 1;
                if (parts.length > 1) {
                    try {
                        freq = Integer.parseInt(parts[1]);
                    } catch (NumberFormatException ignored) {}
                }
                allWords.add(w);
                allFrequencies.add(freq);
            }
        }
    }

    /**
     * Executes the comprehensive benchmark suite across multiple dataset sizes.
     */
    public CustomList<BenchmarkResult> runAll() {
        CustomList<BenchmarkResult> results = new CustomList<>();
        int[] targetSizes = {1000, 5000, 10000, 20000, 25000};

        // Words to test
        String[] hitWords = {"program", "algorithm", "structure", "binary", "system", "search", "computer", "data"};
        String[] missWords = {"nonexistentword", "zzzzzz", "qwertyuiop", "xyzabc123", "pseudocodez"};
        String[] prefixes = {"pro", "alg", "str", "com", "sea"};
        String[] typos = {"progrm", "algoritm", "structur", "computr", "serch"};

        System.out.println("================================================================================");
        System.out.println("            COMMENCING EMPIRICAL BENCHMARK: TRIE VS. LINEAR SEARCH             ");
        System.out.println("================================================================================");
        System.out.println("Total corpus available: " + allWords.size() + " words.");
        System.out.println("Performing warm-up to ensure JVM JIT convergence...");

        // Warmup JIT
        warmup();
        System.out.println("Warm-up complete. Commencing measurement rounds...\n");

        for (int size : targetSizes) {
            if (size > allWords.size()) continue;

            Trie trie = new Trie();
            LinearDictionary linear = new LinearDictionary();

            for (int i = 0; i < size; i++) {
                String w = allWords.get(i);
                int f = allFrequencies.get(i);
                trie.insert(w, f);
                linear.insert(w, f);
            }

            int iterations = 1000;

            // 1. Exact Search (Hits)
            long trieStart = System.nanoTime();
            for (int it = 0; it < iterations; it++) {
                for (String w : hitWords) {
                    trie.search(w);
                }
            }
            long trieEnd = System.nanoTime();
            double trieHitUs = (double) (trieEnd - trieStart) / (iterations * hitWords.length * 1000.0);

            long linStart = System.nanoTime();
            for (int it = 0; it < iterations; it++) {
                for (String w : hitWords) {
                    linear.search(w);
                }
            }
            long linEnd = System.nanoTime();
            double linHitUs = (double) (linEnd - linStart) / (iterations * hitWords.length * 1000.0);

            results.add(new BenchmarkResult(size, "Exact Search (Hit)", trieHitUs, linHitUs));

            // 2. Exact Search (Misses - Worst Case Linear)
            trieStart = System.nanoTime();
            for (int it = 0; it < iterations; it++) {
                for (String w : missWords) {
                    trie.search(w);
                }
            }
            trieEnd = System.nanoTime();
            double trieMissUs = (double) (trieEnd - trieStart) / (iterations * missWords.length * 1000.0);

            linStart = System.nanoTime();
            for (int it = 0; it < iterations; it++) {
                for (String w : missWords) {
                    linear.search(w);
                }
            }
            linEnd = System.nanoTime();
            double linMissUs = (double) (linEnd - linStart) / (iterations * missWords.length * 1000.0);

            results.add(new BenchmarkResult(size, "Exact Search (Miss)", trieMissUs, linMissUs));

            // 3. Autocomplete Prefix Search
            int autoIterations = 200;
            trieStart = System.nanoTime();
            for (int it = 0; it < autoIterations; it++) {
                for (String p : prefixes) {
                    trie.autocomplete(p, 5);
                }
            }
            trieEnd = System.nanoTime();
            double trieAutoUs = (double) (trieEnd - trieStart) / (autoIterations * prefixes.length * 1000.0);

            linStart = System.nanoTime();
            for (int it = 0; it < autoIterations; it++) {
                for (String p : prefixes) {
                    linear.autocomplete(p, 5);
                }
            }
            linEnd = System.nanoTime();
            double linAutoUs = (double) (linEnd - linStart) / (autoIterations * prefixes.length * 1000.0);

            results.add(new BenchmarkResult(size, "Prefix Autocomplete", trieAutoUs, linAutoUs));

            // 4. Fuzzy Autocorrect (Edit Distance <= 1)
            int fuzzyIterations = 50;
            trieStart = System.nanoTime();
            for (int it = 0; it < fuzzyIterations; it++) {
                for (String t : typos) {
                    trie.searchFuzzy(t, 1, 5);
                }
            }
            trieEnd = System.nanoTime();
            double trieFuzzyUs = (double) (trieEnd - trieStart) / (fuzzyIterations * typos.length * 1000.0);

            linStart = System.nanoTime();
            for (int it = 0; it < fuzzyIterations; it++) {
                for (String t : typos) {
                    linear.searchFuzzy(t, 1, 5);
                }
            }
            linEnd = System.nanoTime();
            double linFuzzyUs = (double) (linEnd - linStart) / (fuzzyIterations * typos.length * 1000.0);

            results.add(new BenchmarkResult(size, "Fuzzy Autocorrect (d<=1)", trieFuzzyUs, linFuzzyUs));

            System.out.printf("Completed evaluation for N = %-6d words.\n", size);
        }

        printReport(results);
        return results;
    }

    private void warmup() {
        Trie t = new Trie();
        LinearDictionary l = new LinearDictionary();
        int sample = Math.min(1000, allWords.size());
        for (int i = 0; i < sample; i++) {
            t.insert(allWords.get(i), allFrequencies.get(i));
            l.insert(allWords.get(i), allFrequencies.get(i));
        }
        for (int i = 0; i < 500; i++) {
            t.search("program");
            l.search("program");
            t.autocomplete("pro", 5);
            l.autocomplete("pro", 5);
            t.searchFuzzy("progrm", 1, 5);
            l.searchFuzzy("progrm", 1, 5);
        }
    }

    private void printReport(CustomList<BenchmarkResult> results) {
        System.out.println("\n================================================================================");
        System.out.println("                       EMPIRICAL BENCHMARK SUMMARY TABLE                        ");
        System.out.println("================================================================================");
        System.out.printf("| %-8s | %-24s | %-12s | %-12s | %-10s |\n",
                "Size (N)", "Operation", "Trie (µs)", "Linear (µs)", "Speedup");
        System.out.println("|----------|--------------------------|--------------|--------------|------------|");

        for (int i = 0; i < results.size(); i++) {
            BenchmarkResult r = results.get(i);
            System.out.printf("| %-8d | %-24s | %10.3f µs | %10.3f µs | %9.1fx |\n",
                    r.datasetSize, r.operation, r.trieTimeUs, r.linearTimeUs, r.speedup);
        }
        System.out.println("================================================================================\n");
    }
}

