package no.sebmik;

public class History {
    private final int capacity;
    private int size;
    private Node first;
    private Node last;

    public History(int cap) {
        this.capacity = (int) (cap * 0.9);
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
        for (int i = 0; i < size; i++) {
            if (System.currentTimeMillis() - first.time >= 30000) {
                first = first.next;
                size--;
            } else {
                break;
            }
        }
    }

    public long getTimeToWait() {
        if (isEmpty()) {
            add(System.currentTimeMillis());
            return 0;
        }
        removeOutdated();
        if (size == 0 || last == null || first == null) {
            return 0;
        }
        if (size <= capacity) {
            long l = System.currentTimeMillis() - last.time;
            add(System.currentTimeMillis());
            return l > 1150 || l < 0 ? 0 : 1100;
        } else {
            add(System.currentTimeMillis());
            return 30000 - (System.currentTimeMillis() - getMessageTime(size - capacity));
        }
    }
}
