package Middle.IRElement.Instructions;

import Middle.IRElement.User;
import Middle.IRElement.ValueType.Function;

public class CallInstruction extends BaseInstruction{

    public CallInstruction(Function function, User user) {
        this.result = user;
        this.value1 = function;
    }

    @Override
    public String toString() {
        Function function = (Function) this.value1;
        if(function.returnType.type.equals("int"))
            return this.result + " = call i32 " + function.name + "()";
        else
            return this.result + " = call void " + function.name + "()";
    }
}
