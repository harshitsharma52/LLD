import java.util.Objects;

class MyHashMap {
    // --- your full MyHashMap code here (same as yours) ---
    static class Node {
        String key;
        int value;
        Node next;

        Node(String key, int value) {
            this.key = key;
            this.value = value;
        }
    }

    private int capacity = 16;
    private int size = 0;
    private final double loadFactor = 0.75;

    private Node[] buckets;

    public MyHashMap() {
        buckets = new Node[capacity];
    }

    private int getIndex(String key) {
        return (key == null) ? 0 : Math.abs(key.hashCode()) % capacity;
    }

    public void put(String key, int value) {
        int index = getIndex(key);
        Node head = buckets[index];

        Node current = head;
        while (current != null) {
            if (Objects.equals(current.key, key)) {
                current.value = value;
                return;
            }
            current = current.next;
        }

        Node newNode = new Node(key, value);
        newNode.next = head;
        buckets[index] = newNode;

        size++;

        if ((double) size / capacity >= loadFactor) {
            resize();
        }
    }

    public Integer get(String key) {
        int index = getIndex(key);
        Node current = buckets[index];

        while (current != null) {
            if (Objects.equals(current.key, key)) {
                return current.value;
            }
            current = current.next;
        }
        return null;
    }

    public void remove(String key) {
        int index = getIndex(key);
        Node current = buckets[index];
        Node prev = null;

        while (current != null) {
            if (Objects.equals(current.key, key)) {
                if (prev == null) {
                    buckets[index] = current.next;
                } else {
                    prev.next = current.next;
                }
                size--;
                return;
            }
            prev = current;
            current = current.next;
        }
    }

    private void resize() {
        capacity = capacity * 2;
        Node[] oldBuckets = buckets;
        buckets = new Node[capacity];
        size = 0;

        for (Node head : oldBuckets) {
            while (head != null) {
                put(head.key, head.value);
                head = head.next;
            }
        }
    }

    public int size() {
        return size;
    }

    public static void main(String[] args) {

        MyHashMap map = new MyHashMap();

        map.put("A", 10);
        map.put("B", 20);
        map.put("A", 30);

        System.out.println(map.get("A")); // 30

        map.remove("A");

        System.out.println(map.get("A")); // null
        System.out.println(map.size()); // 1
    }
}
