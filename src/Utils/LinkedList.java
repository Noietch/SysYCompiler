package Utils;

import java.util.Iterator;

public class LinkedList<T extends LinkedListNode> implements Iterable<T> {
    private final LinkedListNode head;
    private final LinkedListNode tail;
    public int size;

    public LinkedList() {
        head = new LinkedListNode();
        tail = new LinkedListNode();
        head.setNext(tail);
        tail.setPrev(head);
        size = 0;
    }

    public void insertAfter(T prevNode, T newNode) {
        newNode.setNext(prevNode.getNext());
        newNode.setPrev(prevNode);
        prevNode.setNext(newNode);
        newNode.getNext().setPrev(newNode);
        size++;
    }

    public void insertBefore(T nextNode, T newNode) {
        newNode.setPrev(nextNode.getPrev());
        newNode.setNext(nextNode);
        nextNode.setPrev(newNode);
        newNode.getPrev().setNext(newNode);
        size++;
    }

    @SuppressWarnings("unchecked")
    public void append(T newNode) {
        insertBefore((T) tail, newNode);
    }


    public void delete(T targetNode) {
        targetNode.getPrev().setNext(targetNode.getNext());
        targetNode.getNext().setPrev(targetNode.getPrev());
        size--;
    }
    @SuppressWarnings("unchecked")
    public T getHead() {
        return (T) head.getNext();
    }
    @SuppressWarnings("unchecked")
    public T getTail() {
        return (T) tail.getPrev();
    }

    public int getSize() {
        return size;
    }

    @Override
    public Iterator<T> iterator() {
        return new IIterator();
    }

    class IIterator implements Iterator<T> {
        LinkedListNode cur = head;

        @Override
        public boolean hasNext() {
            return cur.getNext() != tail;
        }

        @Override
        @SuppressWarnings("unchecked")
        public T next() {
            cur = cur.getNext();
            return (T) cur;
        }
        @SuppressWarnings("unchecked")
        @Override
        public void remove() {
            delete((T) cur);
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
        TestLinkedList.insertAfter(t2, t3);
        TestLinkedList.insertBefore(t3, t4);
        for (TestNode testNode : TestLinkedList) {
            System.out.println(testNode.value);
        }
    }
}
