package Middle.IRElement.Instructions;

import Middle.IRElement.Basic.Op;
import Middle.IRElement.User;
import Middle.IRElement.Value;

public class BinaryInstruction extends BaseInstruction {

    public Op op;
    public BinaryInstruction(User result, Value value1, Value value2, Op op) {
        this.result = result;
        this.value1 = value1;
        this.value2 = value2;
        this.op = op;
    }

    @Override
    public String toString() {
        return this.result.getName() + " = " + this.op + " " + this.value1.getInnerType() + " " + this.value1.getName() + ", " + this.value2.getName();
    }
}
