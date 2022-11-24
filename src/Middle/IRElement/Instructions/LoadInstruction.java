package Middle.IRElement.Instructions;

import Middle.IRElement.Basic.Constant;
import Middle.IRElement.User;
import Middle.IRElement.Value;

import java.util.ArrayList;

public class LoadInstruction extends BaseInstruction {
    public LoadInstruction(User result, Value value) {
        this.result = result;
        this.value1 = value;
    }

    @Override
    public String toString() {
        return result.getName() + " = load " + result.getType() + ", " + value1.getDescriptor();
    }

    // load 指令有一个use, 不能是常量和null
    @Override
    public ArrayList<Value> getUse(){
        ArrayList<Value> use = new ArrayList<>();
        if(!(value1 instanceof Constant)) use.add(value1);
        return use;
    }

    // load 指令有一个def
    @Override
    public ArrayList<Value> getDef(){
        ArrayList<Value> def = new ArrayList<>();
        def.add(result);
        return def;
    }
}
