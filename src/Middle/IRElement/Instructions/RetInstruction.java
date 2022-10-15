package Middle.IRElement.Instructions;

import Middle.IRElement.Value;

public class RetInstruction extends BaseInstruction {
    public RetInstruction(Value value) {
        this.value1 = value;
    }

    @Override
    public String toString() {
        if (value1 == null) return "ret void";
        return "ret i32 " + value1;
    }
}