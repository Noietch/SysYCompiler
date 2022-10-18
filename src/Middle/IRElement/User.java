package Middle.IRElement;

import Middle.IRElement.Type.DataType;
import Middle.IRElement.Type.ValueType;
import Utils.LinkedList;

public class User extends Value{
    LinkedList<Value> operandList;
    public User(String name, ValueType.Type valueType) {
        this.name = name;
        this.type = valueType;
        operandList = new LinkedList<>();
    }

    public void addOperand(Value value){
        operandList.append(value);
    }
}
