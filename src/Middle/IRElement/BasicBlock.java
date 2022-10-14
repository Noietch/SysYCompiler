package Middle.IRElement;

import Middle.IRElement.Instructions.BaseInstruction;
import Middle.IRElement.ValueType.Function;
import Utils.LinkedList;

public class BasicBlock extends Value {
    public Function parent;
    public LinkedList<BaseInstruction> instructions;

    public BasicBlock(String name, Function parent) {
        this.name = name;
        this.parent = parent;
        this.instructions = new LinkedList<>();
    }

    public void appendInst(BaseInstruction instruction) {
        instructions.append(instruction);
    }


    public void insertInstBefore(BaseInstruction nextInst, BaseInstruction newInst) {
        instructions.insertBefore(nextInst, newInst);
    }


    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        res.append(name).append(":\n");
        for (BaseInstruction instruction : instructions) {
            res.append("    ").append(instruction).append("\n");
        }
        return res.toString();
    }
}
