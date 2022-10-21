package Middle.IRElement;

import Middle.IRElement.Type.ValueType;
import Utils.LinkedList;
import Utils.LinkedListNode;

public class Value extends LinkedListNode {
    public String name;
    public LinkedList<Use> uses;

    public ValueType.Type type;

    public boolean isGlobal = false;

    public Value() {
    }

    public Value(String name) {
        this.name = name;
        this.uses = new LinkedList<>();
    }

    public Value(String name, ValueType.Type type) {
        this.name = name;
        this.type = type;
        this.uses = new LinkedList<>();
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(ValueType.Type type) {
        this.type = type;
    }

    public ValueType.Type getInnerType() {
        return this.type.getType();
    }

    public ValueType.Type getType() {
        return this.type;
    }

    public String getName() {
        return "%" + name;
    }

    public String getDescriptor() {
        return String.format("%s %s", this.type, getName());
    }

    @Override
    public String toString() {
        return this.type.toString();
    }
}