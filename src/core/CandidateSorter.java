package core;

import datastructures.CustomList;

/**
 * CandidateSorter implements an in-house MergeSort algorithm designed to sort
 * CustomList<Candidate> without invoking any built-in Java sorting utilities.
 *
 * Ranking Criteria:
 *   1. Primary: Edit Distance (Ascending — closest matches first)
 *   2. Secondary: Word Frequency (Descending — most popular words first)
 *   3. Tertiary: Lexicographical Order (Alphabetical ascending)
 *
 * Time Complexity: O(M log M) where M is the number of candidates.
 * Auxiliary Space: O(M) for auxiliary merge buffers.
 */
public class CandidateSorter {

    /**
     * Sorts the provided CustomList of Candidate objects in-place using MergeSort.
     *
     * @param list the CustomList to sort
     */
    public static void sort(CustomList<Candidate> list) {
        if (list == null || list.size() <= 1) {
            return;
        }
        mergeSort(list, 0, list.size() - 1);
    }

    /**
     * Recursive divide-and-conquer function.
     */
    private static void mergeSort(CustomList<Candidate> list, int left, int right) {
        if (left < right) {
            int mid = left + (right - left) / 2;
            mergeSort(list, left, mid);
            mergeSort(list, mid + 1, right);
            merge(list, left, mid, right);
        }
    }

    /**
     * Merges two sorted sublists [left..mid] and [mid+1..right].
     */
    private static void merge(CustomList<Candidate> list, int left, int mid, int right) {
        int n1 = mid - left + 1;
        int n2 = right - mid;

        Candidate[] leftArr = new Candidate[n1];
        Candidate[] rightArr = new Candidate[n2];

        for (int i = 0; i < n1; i++) {
            leftArr[i] = list.get(left + i);
        }
        for (int j = 0; j < n2; j++) {
            rightArr[j] = list.get(mid + 1 + j);
        }

        int i = 0;
        int j = 0;
        int k = left;

        while (i < n1 && j < n2) {
            if (compare(leftArr[i], rightArr[j]) <= 0) {
                list.set(k++, leftArr[i++]);
            } else {
                list.set(k++, rightArr[j++]);
            }
        }

        while (i < n1) {
            list.set(k++, leftArr[i++]);
        }

        while (j < n2) {
            list.set(k++, rightArr[j++]);
        }
    }

    /**
     * Multi-criteria comparator function:
     * Returns negative if a should come before b, positive if b should come before a, 0 if equal.
     */
    public static int compare(Candidate a, Candidate b) {
        // 1. Primary: Distance (Ascending)
        if (a.getDistance() != b.getDistance()) {
            return a.getDistance() - b.getDistance();
        }

        // 2. Secondary: Frequency (Descending)
        if (a.getFrequency() != b.getFrequency()) {
            return b.getFrequency() - a.getFrequency();
        }

        // 3. Tertiary: Alphabetical (Ascending)
        return a.getWord().compareTo(b.getWord());
    }
}

