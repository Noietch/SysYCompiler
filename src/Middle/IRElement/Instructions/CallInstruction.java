package Middle.IRElement.Instructions;

import Middle.IRElement.User;
import Middle.IRElement.Basic.Function;

public class CallInstruction extends BaseInstruction{

    public CallInstruction(Function function, User user) {
        this.result = user;
        this.value1 = function;
    }

    @Override
    public String toString() {
        Function function = (Function) this.value1;
        return this.result + " = call " + function.getDescriptor();
    }
}
