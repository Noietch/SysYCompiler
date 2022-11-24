package Middle.IRElement.Instructions;

import Middle.IRElement.Basic.Constant;
import Middle.IRElement.Value;

import java.util.ArrayList;

public class StoreInstruction extends BaseInstruction {
    public StoreInstruction(Value value1, Value value2) {
        this.value1 = value1;
        this.value2 = value2;
    }

    @Override
    public String toString() {
        return "store " + value1.getType() + " " + value1.getName() + ", " + value2.getDescriptor();
    }

    // store 指令有两个use, 不能是常量和null
    @Override
    public ArrayList<Value> getUse(){
        ArrayList<Value> use = new ArrayList<>();
        if(!(value1 instanceof Constant)) use.add(value1);
        if(!(value2 instanceof Constant)) use.add(value2);
        return use;
    }

    // store 指令没有def
    @Override
    public ArrayList<Value> getDef(){
        return new ArrayList<>();
    }
}
