package no.sebmik;

public class History {
    private final int capacity;
    private int size;
    private Node first;
    private Node last;

    public History(int cap) {
        this.capacity = (int) (cap * 0.8);
        size = 0;
        first = null;
        last = null;
    }

    public History(History h, int cap) {
        this.capacity = cap;
        this.first = h.first;
        this.last = h.last;
    }

    private class Node {
        Node next;
        long time;
    }

    private boolean isEmpty() {
        return first == null;
    }

    private void add(long time) {
        Node node = new Node();
        node.time = time;
        if (isEmpty()) {
            first = node;
            last = node;
        } else {
            last.next = node;
            last = node;
        }
        size++;
    }

    private long getMessageTime(int t) {
        Node tmp = first;
        for (int i = 0; i < t; i++) {
            tmp = tmp.next;
        }
        return tmp.time;
    }

    private void removeOutdated() {
        while (System.currentTimeMillis() - first.time > 30000) {
            first = first.next;
            size--;
        }
    }

    public long getTimeToWait() {
        if (size == 0) {
            add(System.currentTimeMillis());
            return 0;
        }
        removeOutdated();
        add(System.currentTimeMillis());
        if (size < capacity) {
            return 1250;
        } else {
            return 30000 - (System.currentTimeMillis() - getMessageTime(size - capacity));
        }
    }
}
