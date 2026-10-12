package datastructures;

/**
 * CustomQueue is a generic First-In-First-Out (FIFO) queue implemented from scratch
 * using a singly linked node structure. It eliminates reliance on java.util.Queue
 * or java.util.LinkedList.
 *
 * @param <T> the type of elements held in this queue
 */
public class CustomQueue<T> {

    /**
     * Internal node representing an element in the queue.
     */
    private static class Node<E> {
        E data;
        Node<E> next;

        Node(E data) {
            this.data = data;
            this.next = null;
        }
    }

    private Node<T> head;
    private Node<T> tail;
    private int size;

    /**
     * Constructs an empty CustomQueue.
     */
    public CustomQueue() {
        this.head = null;
        this.tail = null;
        this.size = 0;
    }

    /**
     * Inserts an element at the back of the queue.
     * Time Complexity: O(1)
     *
     * @param element the element to enqueue
     */
    public void enqueue(T element) {
        Node<T> newNode = new Node<>(element);
        if (tail == null) {
            head = newNode;
            tail = newNode;
        } else {
            tail.next = newNode;
            tail = newNode;
        }
        size++;
    }

    /**
     * Removes and returns the element at the front of the queue.
     * Time Complexity: O(1)
     *
     * @return the dequeued element, or null if the queue is empty
     */
    public T dequeue() {
        if (isEmpty()) {
            return null;
        }
        T data = head.data;
        head = head.next;
        if (head == null) {
            tail = null;
        }
        size--;
        return data;
    }

    /**
     * Peeks at the front element without removing it.
     * Time Complexity: O(1)
     *
     * @return the front element, or null if empty
     */
    public T peek() {
        if (isEmpty()) {
            return null;
        }
        return head.data;
    }

    /**
     * Checks if the queue contains no elements.
     * Time Complexity: O(1)
     *
     * @return true if empty, false otherwise
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Returns the number of elements in the queue.
     * Time Complexity: O(1)
     *
     * @return queue size
     */
    public int size() {
        return size;
    }

    /**
     * Resets the queue to empty.
     */
    public void clear() {
        head = null;
        tail = null;
        size = 0;
    }
}

