package Middle.IRElement.Instructions;

import Middle.IRElement.Basic.Op;
import Middle.IRElement.User;
import Middle.IRElement.Value;

public class IcmpInstruction extends BaseInstruction {
    public IcmpInstruction(User result, Value value1, Value value2, Op op) {
        this.result = result;
        this.value1 = value1;
        this.value2 = value2;
        this.op = op;

        this.result.addOperand(value1);
        this.result.addOperand(value2);
    }

    @Override
    public String toString() {
        return this.result.getName() + " = icmp " + op + " " + value1.getInnerType() + " " + value1.getName() + ", " + value2.getName();
    }
}
