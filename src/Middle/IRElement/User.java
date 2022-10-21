package Middle.IRElement;

import Middle.IRElement.Type.ValueType;

import java.util.ArrayList;

public class User extends Value{
    ArrayList<Value> operandList;
    public User(String name, ValueType.Type valueType) {
        this.name = name;
        this.type = valueType;
        operandList = new ArrayList<>();
    }
    public void addOperand(Value value){
        operandList.add(value);
    }
}
