package Middle.IRElement.Instructions;

import Middle.IRElement.Op;
import Middle.IRElement.User;
import Middle.IRElement.Value;

public class LoadInstruction extends BaseInstruction {

    public LoadInstruction(User result, Value value) {
        this.result = result;
        this.value1 = value;
        this.result.addOperand(value1);
    }

    @Override
    public String toString() {
        return result + " = load i32, i32* " + value1;
    }
}
