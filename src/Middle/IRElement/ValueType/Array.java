package Middle.IRElement.ValueType;

import Middle.IRElement.Value;

public class Array extends Value {
    public int dim;

    public Array(String name, int dim) {
        this.name = name;
        this.dim = dim;
    }
}
