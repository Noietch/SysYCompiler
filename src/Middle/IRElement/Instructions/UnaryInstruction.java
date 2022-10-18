package Middle.IRElement.Instructions;

import Middle.IRElement.Basic.Op;
import Middle.IRElement.Value;

public class UnaryInstruction extends BaseInstruction {
    public UnaryInstruction(Value value1, Op op) {
        this.value1 = value1;
        this.op = op;
    }
}
