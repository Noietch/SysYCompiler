package Middle.IRElement.Basic;

import Middle.IRElement.Type.DataType;
import Middle.IRElement.Type.ValueType;
import Middle.IRElement.Value;

public class Constant extends Value {

    public Constant(String name, ValueType.Type type){
        this.type = type;
        this.name = name;
    }

    public String getName(){
        return this.name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
