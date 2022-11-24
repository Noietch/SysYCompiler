package Middle.IRElement.Instructions;

import Middle.IRElement.Basic.Constant;
import Middle.IRElement.Basic.Op;
import Middle.IRElement.User;
import Middle.IRElement.Value;

import java.util.ArrayList;

public class BinaryInstruction extends BaseInstruction {

    public Op op;
    public BinaryInstruction(User result, Value value1, Value value2, Op op) {
        this.result = result;
        this.value1 = value1;
        this.value2 = value2;
        this.op = op;
    }

    @Override
    public String toString() {
        return this.result.getName() + " = " + this.op + " " + this.value1.getInnerType() + " " + this.value1.getName() + ", " + this.value2.getName();
    }

    // 二元指令有两个use
    @Override
    public ArrayList<Value> getUse(){
        ArrayList<Value> use = new ArrayList<>();
        if(!(value1 instanceof Constant)) use.add(value1);
        if(!(value2 instanceof Constant)) use.add(value2);
        return use;
    }

    // 二元指令有一个def
    @Override
    public ArrayList<Value> getDef(){
        ArrayList<Value> def = new ArrayList<>();
        def.add(result);
        return def;
    }
}
