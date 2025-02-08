package clases;

import java.util.LinkedList;

public class Stack<T> {
    private LinkedList<T> elements = new LinkedList<>();

    public void push(T element) {
        elements.addLast(element);
    }

    public T pop() {
        return elements.isEmpty() ? null : elements.removeLast();
    }

    public T peek() {
        return elements.isEmpty() ? null : elements.getLast();
    }

    public boolean isEmpty() {
        return elements.isEmpty();
    }

    public int size() {
        return elements.size();
    }
}
