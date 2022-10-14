package Utils;

import java.util.Iterator;

public class LinkedList<T extends LinkedListNode> implements Iterable<T> {
    public T head;
    public T tail;
    public int size;

    @SuppressWarnings("unchecked")
    public LinkedList() {
        head = (T) new LinkedListNode();
        tail = (T) new LinkedListNode();
        head.next = tail;
        tail.prev = head;
        size = 0;
    }

    public void insertAfter(T prevNode, T newNode) {
        newNode.next = prevNode.next;
        newNode.prev = prevNode;
        prevNode.next = newNode;
        newNode.next.prev = newNode;
        size++;
    }

    public void insertBefore(T nextNode, T newNode) {
        newNode.prev = nextNode.prev;
        newNode.next = nextNode;
        nextNode.prev = newNode;
        newNode.prev.next = newNode;
        size++;
    }

    @SuppressWarnings("unchecked")
    public void append(T newNode) {
        insertBefore(tail,newNode);
    }


    public void delete(T targetNode) {
        targetNode.prev.next = targetNode.next;
        targetNode.next.prev = targetNode.prev;
        size--;
    }

    @Override
    public Iterator<T> iterator() {
        return new IIterator();
    }

    class IIterator implements Iterator<T> {
        T cur = head;

        @Override
        public boolean hasNext() {
            return cur.next != tail;
        }

        @Override
        @SuppressWarnings("unchecked")
        public T next() {
            cur = (T) cur.next;
            return cur;
        }

        @Override
        public void remove() {
            delete(cur);
        }
    }


    public static void main(String[] args) {
        class TestNode extends LinkedListNode {
            public final int value;

            public TestNode(int value) {
                this.value = value;
            }
        }

        LinkedList<TestNode> TestLinkedList = new LinkedList<>();
        TestNode t1 = new TestNode(1);
        TestNode t2 = new TestNode(2);
        TestNode t3 = new TestNode(3);
        TestNode t4 = new TestNode(4);
        TestLinkedList.append(t1);
        TestLinkedList.append(t2);
        TestLinkedList.insertAfter(t2,t3);
        TestLinkedList.insertBefore(t3,t4);
        for(TestNode testNode:TestLinkedList){
            System.out.println(testNode.value);
        }
    }
}
