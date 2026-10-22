package core;

/**
 * Candidate models a search recommendation or spelling correction candidate.
 * It encapsulates the word string, its calculated edit distance from the query,
 * and its statistical corpus frequency.
 */
public class Candidate {
    private final String word;
    private final int distance;
    private final int frequency;

    /**
     * Constructs a Candidate record.
     *
     * @param word the dictionary word
     * @param distance the Levenshtein distance (0 for autocomplete)
     * @param frequency the frequency rank of the word
     */
    public Candidate(String word, int distance, int frequency) {
        this.word = word;
        this.distance = distance;
        this.frequency = frequency;
    }

    public String getWord() {
        return word;
    }

    public int getDistance() {
        return distance;
    }

    public int getFrequency() {
        return frequency;
    }

    @Override
    public String toString() {
        return word + " (edit dist: " + distance + ", freq: " + frequency + ")";
    }
}

