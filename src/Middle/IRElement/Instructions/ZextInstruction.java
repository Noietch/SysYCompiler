package Middle.IRElement.Instructions;

import Middle.IRElement.Basic.Constant;
import Middle.IRElement.Type.ValueType;
import Middle.IRElement.Value;

import java.util.ArrayList;

public class ZextInstruction extends BaseInstruction {
    public ValueType.Type ty;
    public ZextInstruction(Value value, ValueType.Type ty, Value result) {
        this.value1 = value;
        this.ty = ty;
        this.result = result;
    }

    @Override
    public String toString() {
        return String.format("%s = zext %s %s to %s", result.getName(), value1.getType(), value1.getName(), ty);
    }

    @Override
    public ArrayList<Value> getDef() {
        ArrayList<Value> def = new ArrayList<>();
        def.add(result);
        return def;
    }

    // 不能是常量和null
    @Override
    public ArrayList<Value> getUse() {
        ArrayList<Value> use = new ArrayList<>();
        if (!(value1 instanceof Constant)) use.add(value1);
        return use;
    }
}
