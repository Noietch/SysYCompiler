package Middle.IRElement.Instructions;

import Middle.IRElement.Type.ValueType;
import Middle.IRElement.Value;

public class ZextInstruction extends BaseInstruction {
    public ValueType.Type ty;
    public Value res;

    public ZextInstruction(Value value, ValueType.Type ty, Value res) {
        this.value1 = value;
        this.ty = ty;
        this.res = res;
    }

    @Override
    public String toString() {
        return String.format("%s = zext %s %s to %s", res.getName(), value1.getType(), value1.getName(), ty);
    }
}
