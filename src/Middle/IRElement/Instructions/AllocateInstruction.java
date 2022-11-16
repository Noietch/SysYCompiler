package Middle.IRElement.Instructions;

import Middle.IRElement.Value;

public class AllocateInstruction extends BaseInstruction {
    public AllocateInstruction(Value value,Value param) {
        this.result = value;
        this.value1 = param;
    }

    @Override
    public String toString() {
        return String.format("%s = alloca %s", result.getName(), value1);
    }
}