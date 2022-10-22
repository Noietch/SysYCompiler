package Middle.IRElement.Basic;

import Middle.IRElement.Instructions.BaseInstruction;
import Middle.IRElement.Value;
import Utils.LinkedList;

public class BasicBlock extends Value {
    public Function parent;
    public LinkedList<BaseInstruction> instructions;
    public boolean isTerminate = false;

    public BasicBlock(String name, Function parent) {
        this.name = name;
        this.parent = parent;
        this.instructions = new LinkedList<>();
    }

    public BasicBlock(Function parent) {
        this.parent = parent;
        this.instructions = new LinkedList<>();
    }

    public void appendInst(BaseInstruction instruction) {
        if (!isTerminate) {
            instructions.append(instruction);
        }
    }

    public void setName(String name){
        if(this.name == null) this.name = name;
    }

    public void setVirtualNum(String num) {
        this.name = num;
    }

    public void insertInstBefore(BaseInstruction nextInst, BaseInstruction newInst) {
        instructions.insertBefore(nextInst, newInst);
    }

    public void setTerminator(BaseInstruction newNode) {
        instructions.append(newNode);
        isTerminate = true;
    }

    public String getDescriptor() {
        return "label " + getName();
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        res.append(";<label>:").append(name).append(":\n");
        for (BaseInstruction instruction : instructions) {
            res.append("    ").append(instruction).append("\n");
        }
        return res.toString();
    }

    public static class LoopBlock extends BasicBlock {

        public BasicBlock judgeBranch;
        public BasicBlock falseBranch;

        public LoopBlock(String name, Function parent) {
            super(name, parent);
        }

        public void setJudgeBranch(BasicBlock judgeBranch) {
            this.judgeBranch = judgeBranch;
        }

        public void setFalseBranch(BasicBlock falseBranch) {
            this.falseBranch = falseBranch;
        }
    }
}
