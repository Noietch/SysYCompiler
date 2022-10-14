package Middle.IRElement;

import Utils.LinkedList;
import Utils.LinkedListNode;

public class Value extends LinkedListNode {
    public enum Type {
        globalVariable,
        integer,
        function,
        array,
        pointer
    }

    public String name;
    public LinkedList<Use> uses;

    public Type type;

    public Value() {

    }

    public Value(String name) {
        this.name = name;
        this.uses = new LinkedList<>();
    }

    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "%" + name;
    }
}


