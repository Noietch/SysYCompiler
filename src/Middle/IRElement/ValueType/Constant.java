package Middle.IRElement.ValueType;

import Middle.IRElement.Value;

public class Constant extends Value {

    public Constant(String name){
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
