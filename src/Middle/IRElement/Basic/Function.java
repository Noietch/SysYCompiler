package Middle.IRElement.Basic;

import Front.SyntaxAnalyzer.Element.FuncType;
import Middle.IRElement.Type.DataType;
import Middle.IRElement.Value;
import Utils.LinkedList;

import java.util.ArrayList;

public class Function extends Value {
    public ArrayList<Value> funcFParams;
    public DataType returnType;
    public LinkedList<BasicBlock> basicBlocks;
    public Module parent;

    public boolean isReturn = false;

    public boolean define = true;

    public Function(String name, FuncType returnType, Module parent) {
        this.name = name;
        this.basicBlocks = new LinkedList<>();
        this.parent = parent;
        this.funcFParams = new ArrayList<>();
        if (returnType.type.equals("int")) this.returnType = DataType.i32;
        else this.returnType = DataType.Void;
    }

    public void addParams(Value value) {
        funcFParams.add(value);
    }

    public void addBasicBlock(BasicBlock basicBlock) {
        basicBlocks.append(basicBlock);
    }

    public String getName() {
        return "@" + name;
    }

    public String getDescriptor() {
        return returnType + " " + getName() + "()";
    }

    public boolean isReturn() {
        return isReturn;
    }

    public void setReturn() {
        isReturn = true;
    }

    @Override
    public String toString() {
        if (define) return define();
        else return declare();
    }

    public void setNotDefine(){
        define = false;
    }

    public String declare() {
        StringBuilder res = new StringBuilder();
        res.append("declare ").append(returnType).append(" ").append(this.getName());
        res.append("(");
        for (int i = 0; i < funcFParams.size(); i++) {
            res.append(funcFParams.get(i).getType());
            if (i != funcFParams.size() - 1) res.append(", ");
        }
        res.append(")");
        return res.toString();
    }

    public String define() {
        StringBuilder res = new StringBuilder();
        res.append("define dso_local ").append(returnType).append(" ").append(this.getName());
        res.append("(");
        for (int i = 0; i < funcFParams.size(); i++) {
            res.append(funcFParams.get(i).getDescriptor());
            if (i != funcFParams.size() - 1) res.append(", ");
        }
        res.append("){\n");
        for (BasicBlock basicBlock : basicBlocks) res.append(basicBlock);
        res.append("}\n");
        return res.toString();
    }
}
