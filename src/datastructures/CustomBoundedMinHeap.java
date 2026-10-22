package datastructures;

import core.Candidate;
import core.CandidateSorter;

/**
 * CustomBoundedMinHeap implements an in-house Bounded Min-Heap tailored for Top-K
 * candidate extraction without using java.util.PriorityQueue.
 *
 * Algorithmic Concept:
 *   - The heap maintains a maximum capacity of K elements.
 *   - The root of the min-heap always stores the "weakest" (lowest priority)
 *     candidate among the current top-K candidates.
 *   - When a new candidate arrives:
 *       1. If size < K: insert candidate and siftUp in O(log K).
 *       2. If size == K: compare with root. If the new candidate has higher
 *          priority than root, replace root and siftDown in O(log K).
 *          Otherwise, discard immediately in O(1).
 *   - After evaluating M total candidates, extracting the top-K elements in
 *     strictly sorted order takes O(K log K) time.
 *
 * Total Time Complexity: O(M log K + K log K) vs Full Sort O(M log M).
 * Retained Auxiliary Space: O(K) vs Full Sort O(M).
 */
public class CustomBoundedMinHeap {
    private final Candidate[] heap;
    private final int capacity;
    private int size;

    /**
     * Initializes a bounded min-heap with fixed maximum capacity K.
     *
     * @param capacity maximum number of candidates to retain (K)
     */
    public CustomBoundedMinHeap(int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException("Heap capacity must be at least 1, received: " + capacity);
        }
        this.capacity = capacity;
        this.heap = new Candidate[capacity];
        this.size = 0;
    }

    /**
     * Inserts a candidate into the bounded heap, keeping only the best K items.
     * Time Complexity: O(log K) worst-case, O(1) best-case.
     *
     * @param candidate candidate to consider
     */
    public void insert(Candidate candidate) {
        if (candidate == null) return;

        if (size < capacity) {
            heap[size] = candidate;
            siftUp(size);
            size++;
        } else {
            // If candidate is strictly better than the weakest element at root
            if (isBetter(candidate, heap[0])) {
                heap[0] = candidate;
                siftDown(0);
            }
        }
    }

    /**
     * Extracts all retained candidates sorted from highest priority (best) to lowest.
     * Time Complexity: O(K log K)
     *
     * @return CustomList containing up to K candidates in ranked order
     */
    public CustomList<Candidate> extractSorted() {
        int count = size;
        Candidate[] sortedArray = new Candidate[count];

        // Repeatedly extract root (the weakest remaining) from back to front
        for (int i = count - 1; i >= 0; i--) {
            sortedArray[i] = extractRoot();
        }

        CustomList<Candidate> result = new CustomList<>(count);
        for (int i = 0; i < count; i++) {
            result.add(sortedArray[i]);
        }
        return result;
    }

    /**
     * Removes and returns the root element (weakest among current elements).
     */
    private Candidate extractRoot() {
        if (size == 0) return null;
        Candidate root = heap[0];
        heap[0] = heap[size - 1];
        heap[size - 1] = null;
        size--;
        if (size > 0) {
            siftDown(0);
        }
        return root;
    }

    /**
     * Sifts up an element at index to restore the min-heap property.
     */
    private void siftUp(int index) {
        while (index > 0) {
            int parent = (index - 1) / 2;
            // Min-heap condition: parent must be worse than or equal to child.
            // If child is worse than parent, swap up to let the worst bubble toward root.
            if (isWorse(heap[index], heap[parent])) {
                swap(index, parent);
                index = parent;
            } else {
                break;
            }
        }
    }

    /**
     * Sifts down an element at index to restore the min-heap property.
     */
    private void siftDown(int index) {
        while (index * 2 + 1 < size) {
            int left = index * 2 + 1;
            int right = index * 2 + 2;
            int worstChild = left;

            if (right < size && isWorse(heap[right], heap[left])) {
                worstChild = right;
            }

            if (isWorse(heap[worstChild], heap[index])) {
                swap(index, worstChild);
                index = worstChild;
            } else {
                break;
            }
        }
    }

    /**
     * Returns true if candidate 'a' has higher ranking priority than candidate 'b'.
     * Priority: lower distance, higher frequency, alphabetical tie-breaking.
     */
    private boolean isBetter(Candidate a, Candidate b) {
        return CandidateSorter.compare(a, b) < 0;
    }

    /**
     * Returns true if candidate 'a' has lower ranking priority (is worse) than 'b'.
     * The weakest candidate sits at the root of our min-heap.
     */
    private boolean isWorse(Candidate a, Candidate b) {
        return CandidateSorter.compare(a, b) > 0;
    }

    private void swap(int i, int j) {
        Candidate temp = heap[i];
        heap[i] = heap[j];
        heap[j] = temp;
    }

    public int size() {
        return size;
    }

    public int getCapacity() {
        return capacity;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public Candidate peekWeakest() {
        return (size > 0) ? heap[0] : null;
    }

    public void clear() {
        for (int i = 0; i < size; i++) {
            heap[i] = null;
        }
        size = 0;
    }
}

