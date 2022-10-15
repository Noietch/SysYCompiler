package Middle.IRElement.Instructions;

import Middle.IRElement.BasicBlock;
import Middle.IRElement.Value;

public class BranchInstruction extends BaseInstruction {
    public Value cond;

    public BranchInstruction(Value cond, BasicBlock labelTrue, BasicBlock labelFalse) {
        this.cond = cond;
        this.value1 = labelTrue;
        this.value2 = labelFalse;
    }

    public BranchInstruction(BasicBlock dest) {
        this.value1 = dest;
    }

    public void setLabelTrue(BasicBlock labelTrue) {
        this.value1 = labelTrue;
    }

    public void setLabelFalse(BasicBlock labelFalse) {
        this.value2 = labelFalse;
    }

    @Override
    public String toString() {
        if (cond != null)
            return "br i1 " + cond + ", label %" + value1.name + ", label %" + value2.name;
        else
            return "br label %" + value1.name;
    }
}
