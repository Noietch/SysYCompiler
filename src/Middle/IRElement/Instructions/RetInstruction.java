package Middle.IRElement.Instructions;

import Middle.IRElement.Basic.Constant;
import Middle.IRElement.Value;

import java.util.ArrayList;

public class RetInstruction extends BaseInstruction {
    public RetInstruction(Value value) {
        this.value1 = value;
    }

    @Override
    public String toString() {
        if (value1 == null) return "ret void";
        return "ret i32 " + value1.getName();
    }

    // ret 指令有一个use, 不能是常量和null
    @Override
    public ArrayList<Value> getUse(){
        ArrayList<Value> use = new ArrayList<>();
        if(!(value1 instanceof Constant)) use.add(value1);
        return use;
    }

    // ret 指令没有def
    @Override
    public ArrayList<Value> getDef(){
        return new ArrayList<>();
    }
}