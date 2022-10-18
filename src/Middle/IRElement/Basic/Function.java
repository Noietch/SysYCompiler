package Middle.IRElement.Basic;

import Front.SyntaxAnalyzer.Element.FuncFParams;
import Front.SyntaxAnalyzer.Element.FuncType;
import Middle.IRElement.Type.DataType;
import Middle.IRElement.Value;
import Utils.LinkedList;

public class Function extends Value {
    public FuncFParams funcFParams;
    public DataType returnType;
    public LinkedList<BasicBlock> basicBlocks;
    public Module parent;

    public Function(String name, FuncFParams funcFParams, FuncType returnType, Module parent) {
        this.name = name;
        this.funcFParams = funcFParams;
        this.basicBlocks = new LinkedList<>();
        this.parent = parent;

        if (returnType.type.equals("int")) this.returnType = DataType.i32;
        else this.returnType = DataType.Void;
    }

    public void addBasicBlock(BasicBlock basicBlock) {
        basicBlocks.append(basicBlock);
    }

    public String getDescriptor() {
        return returnType + " " + getName() + "()";
    }
    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        res.append("define dso_local ").append(returnType).
                append(" ").append(this.name).append("(){\n");
        for (BasicBlock basicBlock : basicBlocks) res.append(basicBlock);
        res.append("}\n");
        return res.toString();
    }
}
