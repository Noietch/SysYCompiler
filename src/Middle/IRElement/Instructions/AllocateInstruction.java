package Middle.IRElement.Instructions;

import Middle.IRElement.Value;

public class AllocateInstruction extends BaseInstruction{
    public AllocateInstruction(Value value){
        this.value1 = value;
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        res.append(value1).append(" = alloca i32");
        if(value1.type == Type.integer) return res.toString();
        return "not implement";
    }
}