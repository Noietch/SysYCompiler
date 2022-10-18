package Middle.IRElement.Instructions;

import Middle.IRElement.Basic.Op;
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
        return this.result.getName() + " = " + this.op + " " + this.value1.getType() + " " + this.value1.getName() + ", " + this.value2.getName();
    }
}
