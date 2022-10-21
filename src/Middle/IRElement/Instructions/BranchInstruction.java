package Middle.IRElement.Instructions;

import Middle.IRElement.Basic.BasicBlock;
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
        BasicBlock B1 = (BasicBlock) value1;
        if (cond != null) {
            BasicBlock B2 = (BasicBlock) value2;
            return "br " + cond.getType() + " " + cond.getName() + ", " + B1.getDescriptor() + ", " + B2.getDescriptor();
        } else {
            return "br " + B1.getDescriptor();
        }
    }
}
