package Middle.IRElement.Instructions;

import Middle.IRElement.Type.ValueType;
import Middle.IRElement.Value;

public class AllocateInstruction extends BaseInstruction{
    public AllocateInstruction(Value value){
        this.value1 = value;
    }

    @Override
    public String toString() {
        return String.format("%s = alloca %s",value1.getName(),value1.getType());
    }
}