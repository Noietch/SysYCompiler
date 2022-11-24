package Middle.IRElement.Instructions;

import Middle.IRElement.Basic.Constant;
import Middle.IRElement.Basic.Op;
import Middle.IRElement.User;
import Middle.IRElement.Value;

import java.util.ArrayList;

public class IcmpInstruction extends BaseInstruction {
    public Op op;
    public IcmpInstruction(User result, Value value1, Value value2, Op op) {
        this.result = result;
        this.value1 = value1;
        this.value2 = value2;
        this.op = op;
    }

    @Override
    public String toString() {
        return this.result.getName() + " = icmp " + op + " " + value1.getInnerType() + " " + value1.getName() + ", " + value2.getName();
    }

    // icmp 指令有两个use, 不能是常量和null
    @Override
    public ArrayList<Value> getUse(){
        ArrayList<Value> use = new ArrayList<>();
        if(!(value1 instanceof Constant)) use.add(value1);
        if(!(value2 instanceof Constant)) use.add(value2);
        return use;
    }

    // icmp 指令有一个def
    @Override
    public ArrayList<Value> getDef(){
        ArrayList<Value> def = new ArrayList<>();
        def.add(result);
        return def;
    }
}
