package Middle.IRElement.Instructions;

import Middle.IRElement.Op;
import Middle.IRElement.User;
import Middle.IRElement.Value;

public class UnaryInstruction extends BaseInstruction{
    public UnaryInstruction(Value value1, Op op) {
        this.value1 = value1;
        this.op = op;
    }

//    @Override
//    public String toString() {
//        if(op.type == Op.Type.sub) return
//    }
}
