package datastructures;

/**
 * CustomList is a generic dynamic array implementation written from first principles.
 * It provides dynamic resizing, random-access indexing, element insertion, and removal
 * without relying on any Java Collections Framework classes (such as java.util.ArrayList).
 *
 * @param <T> the type of elements stored in this dynamic list
 */
public class CustomList<T> {
    private static final int DEFAULT_CAPACITY = 16;
    private Object[] data;
    private int size;

    /**
     * Constructs an empty CustomList with default initial capacity.
     */
    public CustomList() {
        this(DEFAULT_CAPACITY);
    }

    /**
     * Constructs an empty CustomList with the specified initial capacity.
     *
     * @param initialCapacity the initial array allocation size
     */
    public CustomList(int initialCapacity) {
        if (initialCapacity < 1) {
            initialCapacity = DEFAULT_CAPACITY;
        }
        this.data = new Object[initialCapacity];
        this.size = 0;
    }

    /**
     * Appends an element to the end of the list, automatically doubling capacity if full.
     * Amortized Time Complexity: O(1)
     *
     * @param element the element to add
     */
    public void add(T element) {
        if (size == data.length) {
            grow();
        }
        data[size++] = element;
    }

    /**
     * Retrieves the element at the specified index.
     * Time Complexity: O(1)
     *
     * @param index the zero-based index of the element
     * @return the element at the index
     * @throws IndexOutOfBoundsException if index is negative or >= size
     */
    @SuppressWarnings("unchecked")
    public T get(int index) {
        checkBounds(index);
        return (T) data[index];
    }

    /**
     * Replaces the element at the specified index with a new element.
     * Time Complexity: O(1)
     *
     * @param index the zero-based index to update
     * @param element the new element to store
     * @return the old element previously stored at this index
     */
    @SuppressWarnings("unchecked")
    public T set(int index, T element) {
        checkBounds(index);
        T oldValue = (T) data[index];
        data[index] = element;
        return oldValue;
    }

    /**
     * Removes and returns the element at the specified index, shifting trailing elements left.
     * Time Complexity: O(N - index)
     *
     * @param index the index of the element to remove
     * @return the removed element
     */
    @SuppressWarnings("unchecked")
    public T remove(int index) {
        checkBounds(index);
        T oldValue = (T) data[index];
        int numMoved = size - index - 1;
        if (numMoved > 0) {
            for (int i = 0; i < numMoved; i++) {
                data[index + i] = data[index + i + 1];
            }
        }
        data[--size] = null; // Prevent memory leak
        return oldValue;
    }

    /**
     * Returns the current number of elements stored in the list.
     * Time Complexity: O(1)
     *
     * @return size of the list
     */
    public int size() {
        return size;
    }

    /**
     * Checks if the list contains any elements.
     * Time Complexity: O(1)
     *
     * @return true if empty, false otherwise
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Clears all elements from the list, resetting size to zero.
     */
    public void clear() {
        for (int i = 0; i < size; i++) {
            data[i] = null;
        }
        size = 0;
    }

    /**
     * Checks if an element exists in the list by value comparison.
     * Time Complexity: O(N)
     *
     * @param element the element to search for
     * @return true if found, false otherwise
     */
    public boolean contains(T element) {
        for (int i = 0; i < size; i++) {
            if (element == null) {
                if (data[i] == null) return true;
            } else {
                if (element.equals(data[i])) return true;
            }
        }
        return false;
    }

    /**
     * Doubles the internal backing array capacity.
     */
    private void grow() {
        int newCapacity = data.length * 2;
        Object[] newData = new Object[newCapacity];
        for (int i = 0; i < size; i++) {
            newData[i] = data[i];
        }
        data = newData;
    }

    /**
     * Validates that the provided index is within [0, size - 1].
     */
    private void checkBounds(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds for size " + size);
        }
    }
}

