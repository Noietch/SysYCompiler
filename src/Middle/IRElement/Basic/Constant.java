package Middle.IRElement.Basic;

import Middle.IRElement.Type.ValueType;
import Middle.IRElement.Value;

public class Constant extends Value {

    public Constant(String name) {
        this.type = ValueType.i32;
        this.name = name;
    }

    public int getValue() {
        return Integer.parseInt(name);
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
