package Middle.IRElement.Instructions;

import Middle.IRElement.Basic.Constant;
import Middle.IRElement.Value;

import java.util.ArrayList;

public class GetElementPtr extends BaseInstruction {
    public Value bound1;
    public Value bound2;


    public GetElementPtr(Value result, Value value1, Value bound1, Value bound2) {
        this.result = result;
        this.value1 = value1;
        this.bound1 = bound1;
        this.bound2 = bound2;
    }
    public GetElementPtr(Value result, Value value1, Value bound1) {
        this.result = result;
        this.value1 = value1;
        this.bound1 = bound1;
    }

    @Override
    public String toString() {
        if(bound2 != null)
            return String.format("%s = getelementptr %s, %s, %s, %s", result.getName(), value1.getInnerType(), value1.getDescriptor(), bound1.getDescriptor(), bound2.getDescriptor());
        else
            return String.format("%s = getelementptr %s, %s, %s", result.getName(), value1.getInnerType(), value1.getDescriptor(), bound1.getDescriptor());
    }

    // getelementptr 指令有两个use, 不能是常量和null
    @Override
    public ArrayList<Value> getUse(){
        ArrayList<Value> use = new ArrayList<>();
        if(!(value1 instanceof Constant)) use.add(value1);
        if(!(bound1 instanceof Constant)) use.add(bound1);
        if(bound2 != null && !(bound2 instanceof Constant)) use.add(bound2);
        return use;
    }

    // getelementptr 指令有一个def
    @Override
    public ArrayList<Value> getDef(){
        ArrayList<Value> def = new ArrayList<>();
        def.add(result);
        return def;
    }
}
