package Middle.IRElement;

import Utils.LinkedList;

public class User extends Value{
    LinkedList<Value> operandList;
    public User(String name) {
        super(name);
        operandList = new LinkedList<>();
    }

    public void addOperand(Value value){
        operandList.append(value);
    }

    @Override
    public String toString() {
        return "%" + name;
    }
}
