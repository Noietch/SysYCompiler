package Middle.IRElement.Instructions;

import Middle.IRElement.Basic.Op;
import Middle.IRElement.User;
import Middle.IRElement.Value;

public class IcmpInstruction extends BaseInstruction {
    public Op op;
    public IcmpInstruction(User result, Value value1, Value value2, Op op) {
        this.result = result;
        this.value1 = value1;
        this.value2 = value2;
        this.op = op;
    }

    @Override
    public String toString() {
        return this.result.getName() + " = icmp " + op + " " + value1.getInnerType() + " " + value1.getName() + ", " + value2.getName();
    }
}
