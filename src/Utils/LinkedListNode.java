package Utils;

public class LinkedListNode {
    private LinkedListNode prev;
    private LinkedListNode next;

    public LinkedListNode getPrev() {
        return prev;
    }

    public LinkedListNode getNext() {
        return next;
    }

    public void setPrev(LinkedListNode prev) {
        this.prev = prev;
    }

    public void setNext(LinkedListNode next) {
        this.next = next;
    }
}
