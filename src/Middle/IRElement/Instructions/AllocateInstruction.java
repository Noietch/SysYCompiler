package Middle.IRElement.Instructions;

import Middle.IRElement.Value;

import java.util.ArrayList;

public class AllocateInstruction extends BaseInstruction {
    public AllocateInstruction(Value value,Value param) {
        this.result = value;
        this.value1 = param;
    }

    @Override
    public String toString() {
        return String.format("%s = alloca %s", result.getName(), value1);
    }

    // alloc 指令没有use
    @Override
    public ArrayList<Value> getUse(){
        return new ArrayList<>();
    }

    // alloc 指令有一个def
    @Override
    public ArrayList<Value> getDef(){
        ArrayList<Value> def = new ArrayList<>();
        def.add(result);
        return def;
    }
}