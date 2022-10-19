package Middle.IRElement.Instructions;

import Middle.IRElement.Value;

public class AllocateInstruction extends BaseInstruction {
    public AllocateInstruction(Value value,Value param) {
        this.value1 = value;
        this.value2 = param;
    }

    @Override
    public String toString() {
        return String.format("%s = alloca %s", value1.getName(), value2);
    }
}