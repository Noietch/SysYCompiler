package Middle.IRElement;

import Middle.IRElement.Type.DataType;
import Middle.IRElement.Type.ValueType;
import Utils.LinkedList;
import Utils.LinkedListNode;

public class Value extends LinkedListNode {
    public String name;
    public LinkedList<Use> uses;

    public ValueType.Type type;

    public Value() {

    }

    public Value(String name, ValueType.Type type) {
        this.name = name;
        this.type = type;
        this.uses = new LinkedList<>();
    }

    public String getType(){
        return this.type.getType();
    }

    public String getName(){
        return "%" + name;
    }

    public String getDescriptor(){
        return String.format("%s %s",this.type,getName());
    }
}




