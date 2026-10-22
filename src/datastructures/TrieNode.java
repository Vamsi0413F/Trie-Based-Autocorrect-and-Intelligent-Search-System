package datastructures;

/**
 * TrieNode represents an individual state or vertex within the Prefix Tree (Trie).
 * Each node maintains 26 child references (one for each lowercase English letter 'a'-'z'),
 * a terminal word flag, an associated corpus frequency, and a child count for O(1) leaf checks.
 */
public class TrieNode {
    public static final int ALPHABET_SIZE = 26;

    private final TrieNode[] children;
    private boolean isEndOfWord;
    private int frequency;
    private int childCount;

    /**
     * Initializes an empty TrieNode with null children and zero frequency.
     */
    public TrieNode() {
        this.children = new TrieNode[ALPHABET_SIZE];
        this.isEndOfWord = false;
        this.frequency = 0;
        this.childCount = 0;
    }

    /**
     * Retrieves the child node corresponding to the given character.
     * Time Complexity: O(1)
     *
     * @param c character in ['a'...'z']
     * @return child TrieNode or null if not present
     */
    public TrieNode getChild(char c) {
        int index = c - 'a';
        if (index < 0 || index >= ALPHABET_SIZE) {
            return null;
        }
        return children[index];
    }

    /**
     * Assigns a child node for the given character.
     * Time Complexity: O(1)
     *
     * @param c character in ['a'...'z']
     * @param node child TrieNode to attach
     */
    public void setChild(char c, TrieNode node) {
        int index = c - 'a';
        if (index < 0 || index >= ALPHABET_SIZE) {
            return;
        }
        if (children[index] == null && node != null) {
            childCount++;
        } else if (children[index] != null && node == null) {
            childCount--;
        }
        children[index] = node;
    }

    /**
     * Checks whether the current node represents the termination of a valid word.
     * Time Complexity: O(1)
     *
     * @return true if terminal node, false otherwise
     */
    public boolean isEndOfWord() {
        return isEndOfWord;
    }

    /**
     * Sets the terminal word status of this node.
     *
     * @param endOfWord true if this node completes a valid dictionary word
     */
    public void setEndOfWord(boolean endOfWord) {
        this.isEndOfWord = endOfWord;
    }

    /**
     * Gets the word occurrence frequency recorded at this terminal node.
     *
     * @return frequency count
     */
    public int getFrequency() {
        return frequency;
    }

    /**
     * Sets the word occurrence frequency at this terminal node.
     *
     * @param frequency positive integer frequency
     */
    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    /**
     * Returns whether this node has at least one active child.
     * Time Complexity: O(1) via tracked childCount.
     *
     * @return true if non-leaf, false if leaf
     */
    public boolean hasChildren() {
        return childCount > 0;
    }

    /**
     * Returns the total count of non-null child links.
     *
     * @return number of outgoing edges
     */
    public int getChildCount() {
        return childCount;
    }

    /**
     * Provides direct access to the children array for systematic tree traversal.
     *
     * @return array of child TrieNodes
     */
    public TrieNode[] getChildren() {
        return children;
    }
}

