package Middle.IRElement.ValueType;

import Front.SyntaxAnalyzer.Element.FuncFParams;
import Front.SyntaxAnalyzer.Element.FuncType;
import Middle.IRElement.BasicBlock;
import Middle.IRElement.Module;
import Middle.IRElement.Value;
import Utils.LinkedList;

public class Function extends Value {
    public FuncFParams funcFParams;
    public FuncType returnType;
    public LinkedList<BasicBlock> basicBlocks;
    public Module parent;

    public Function(String name, FuncFParams funcFParams, FuncType returnType, Module parent) {
        this.name = name;
        this.funcFParams = funcFParams;
        this.returnType = returnType;
        this.basicBlocks = new LinkedList<>();
        this.parent = parent;
    }

    public void addBasicBlock(BasicBlock basicBlock) {
        basicBlocks.append(basicBlock);
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        if (returnType.type.equals("int"))
            res.append("define dso_local i32 @").append(this.name).append("(){\n");
        else
            res.append("define dso_local void @").append(this.name).append("(){\n");
        for (BasicBlock basicBlock : basicBlocks) {
            res.append(basicBlock);
        }
        res.append("}\n");
        return res.toString();
    }
}
