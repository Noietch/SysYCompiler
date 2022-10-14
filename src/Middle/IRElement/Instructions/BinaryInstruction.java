package Middle.IRElement.Instructions;

import Middle.IRElement.Op;
import Middle.IRElement.User;
import Middle.IRElement.Value;

public class BinaryInstruction extends BaseInstruction {
    public BinaryInstruction(User result, Value value1, Value value2, Op op) {
        this.result = result;
        this.value1 = value1;
        this.value2 = value2;
        this.op = op;

        this.result.addOperand(value1);
        this.result.addOperand(value2);
    }

    @Override
    public String toString() {
        return this.result + " = " + this.op + " i32 " + this.value1 + ", " + this.value2;
    }
}
