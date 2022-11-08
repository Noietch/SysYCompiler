package Middle.IRElement.Instructions;

import Middle.IRElement.Basic.Op;
import Middle.IRElement.User;
import Middle.IRElement.Value;

public class BaseInstruction extends Value {
    public Value value1;
    public Value value2;

    public User result;

    public Op op;

    public MipsHelper mipsHelper = new MipsHelper(MipsHelper.Type.normal);

    public BaseInstruction() {}

    public BaseInstruction(User result, Value value1, Value value2, Op op) {
        this.result = result;
        this.value1 = value1;
        this.value2 = value2;
        this.op = op;

        this.result.addOperand(value1);
        this.result.addOperand(value2);
    }
}
