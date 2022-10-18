package Middle.IRElement.Instructions;

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
        return result.getName() + " = load " + result.getType() + ", " + value1.getDescriptor();
    }
}
