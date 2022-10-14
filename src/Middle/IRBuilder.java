package Middle;

import Front.SyntaxAnalyzer.Element.*;
import Middle.IRElement.*;
import Middle.IRElement.Instructions.AllocateInstruction;
import Middle.IRElement.Instructions.BinaryInstruction;
import Middle.IRElement.Instructions.LoadInstruction;
import Middle.IRElement.Instructions.RetInstruction;
import Middle.IRElement.Module;
import Middle.IRElement.ValueType.Constant;
import Middle.IRElement.ValueType.Function;

public class IRBuilder {
    public CompUnit syntaxTreeRoot; // 语法树根
    public ValueTable currentValueTable = new ValueTable(null);
    public BasicBlock currentBasicBlock = null;
    public Module currentModule = new Module();
    public Function currentFunction = null;

    public IRBuilder(CompUnit treeRoot) {
        this.syntaxTreeRoot = treeRoot;
    }

    public void buildIR(){
        travelSyntaxTree(syntaxTreeRoot);
    }

    public String getIR(){
        buildIR();
        return currentModule.toString();
    }

    public void travelSyntaxTree(SyntaxNode node) {
        if (node != null) {
            for (SyntaxNode syntaxNode : node.childrenNode) {
                if(syntaxNode instanceof ConstDef) visitConstDef((ConstDef) syntaxNode);
                else if(syntaxNode instanceof VarDef) visitVarDef((VarDef) syntaxNode);
                else if (syntaxNode instanceof FuncDef) visitFuncDef((FuncDef) syntaxNode);
                else if (syntaxNode instanceof MainFuncDef) visitMainFuncDef((MainFuncDef) syntaxNode);
                else if (syntaxNode instanceof Stmt) {
                    Stmt stmt = (Stmt) syntaxNode;
                    if (stmt.getType() == Stmt.Type.Return) visitReturn(stmt);
                }

                if (syntaxNode instanceof FuncDef || syntaxNode instanceof MainFuncDef ||
                        (syntaxNode instanceof Stmt && ((Stmt) syntaxNode).getType() == Stmt.Type.Block)) {
                    currentValueTable = currentValueTable.newSon();
                    travelSyntaxTree(syntaxNode);
                    currentValueTable = currentValueTable.back();
                } else travelSyntaxTree(syntaxNode);
            }
        }
    }

    public Value visitConstInitVal(ConstInitVal constInitVal){
        throw new RuntimeException("[visitConstInitVal]");
    }

    public void visitConstDef(ConstDef constDef){
        Value value = new Value(VirtualRegister.getRegister());
        if(constDef.constExps.size() == 0) value.setType(Value.Type.integer);
        else throw new RuntimeException("[visitConstDef] Array not implement");
        AllocateInstruction allocateInstruction = new AllocateInstruction(value);
        currentBasicBlock.appendInst(allocateInstruction);

//        visitConstInitVal(constDef.constInitVal);

        currentValueTable.addValue(constDef.ident.token.value,value);
    }

    public void visitVarDef(VarDef varDef){
        Value value = new Value(VirtualRegister.getRegister());
        AllocateInstruction allocateInstruction = new AllocateInstruction(value);
        currentBasicBlock.appendInst(allocateInstruction);
        currentValueTable.addValue(varDef.ident.token.value,value);
    }

    public Value visitExp(Exp exp) {
        return visitAddExp(exp.addExp);
    }

    public Value visitAddExp(AddExp addExp) {
        if (addExp.mulExps.size() == 1) return visitMulExp(addExp.mulExps.get(0));
        else {
            Value value1 = visitMulExp(addExp.mulExps.get(0));
            Value value2 = visitMulExp(addExp.mulExps.get(1));
            Op op = new Op(Op.Op2Type(addExp.unaryOps.get(0).token));
            User res = new User(VirtualRegister.getRegister());
            currentBasicBlock.appendInst(new BinaryInstruction(res, value1, value2, op));
            for (int i = 2; i < addExp.mulExps.size(); i++) {
                value1 = res;
                value2 = visitMulExp(addExp.mulExps.get(i));
                op = new Op(Op.Op2Type(addExp.unaryOps.get(i - 1).token));
                res = new User(VirtualRegister.getRegister());
                currentBasicBlock.appendInst(new BinaryInstruction(res, value1, value2, op));
            }
            return res;
        }
    }

    public Value visitMulExp(MulExp mulExp) {
        if (mulExp.unaryExps.size() == 1) return visitUnaryExp(mulExp.unaryExps.get(0));
        else {
            Value value1 = visitUnaryExp(mulExp.unaryExps.get(0));
            Value value2 = visitUnaryExp(mulExp.unaryExps.get(1));
            Op op = new Op(Op.Op2Type(mulExp.unaryOps.get(0).token));
            User res = new User(VirtualRegister.getRegister());
            currentBasicBlock.appendInst(new BinaryInstruction(res, value1, value2, op));
            for (int i = 2; i < mulExp.unaryExps.size(); i++) {
                value1 = res;
                value2 = visitUnaryExp(mulExp.unaryExps.get(i));
                op = new Op(Op.Op2Type(mulExp.unaryOps.get(i - 1).token));
                res = new User(VirtualRegister.getRegister());
                currentBasicBlock.appendInst(new BinaryInstruction(res, value1, value2, op));
            }
            return res;
        }
    }

    public Value visitUnaryExp(UnaryExp unaryExp){
        if(unaryExp.primaryExp != null) return visitPrimaryExp(unaryExp.primaryExp);
        else if(unaryExp.ident != null) throw new RuntimeException();
        else {
            if(unaryExp.unaryOp.opType == UnaryOp.type.MINUS) {
                Value value1 = new Constant("0");
                Value value2 = visitUnaryExp(unaryExp.unaryExp);
                Op op = new Op(Op.Type.sub);
                User res = new User(VirtualRegister.getRegister());
                currentBasicBlock.appendInst(new BinaryInstruction(res, value1, value2, op));
                return res;
            }
            else if(unaryExp.unaryOp.opType == UnaryOp.type.PLUS){
                return visitUnaryExp(unaryExp.unaryExp);
            }
            else throw new RuntimeException();
        }
    }

    public Value visitPrimaryExp(PrimaryExp primaryExp){
        if(primaryExp.exp != null) return visitExp(primaryExp.exp);
        else if(primaryExp.lVal != null) return visitLVal(primaryExp.lVal);
        else return new Constant(primaryExp.getNumber());
    }

    public Value visitLVal(LVal lVal){
        if(lVal.type == VarType.Var){
            User res = new User(VirtualRegister.getRegister());
            Value value = currentValueTable.getRegister(lVal.ident.token.value);
            LoadInstruction loadInstruction = new LoadInstruction(res,value);
            currentBasicBlock.appendInst(loadInstruction);
            return res;
        }
        else throw new RuntimeException();
    }

    public void visitFuncDef(FuncDef funcDef) {
        // 归零计数器
        VirtualRegister.setZero();
        // 创建函数
        currentFunction = new Function(funcDef.ident.token.value, funcDef.funcFParams, funcDef.funcType, currentModule);
        // 创建第一个代码块
        currentBasicBlock = new BasicBlock(VirtualRegister.getRegister(), currentFunction);
        // 将代码块添加到函数中
        currentFunction.addBasicBlock(currentBasicBlock);
        currentModule.addFunctions(currentFunction);
    }

    public void visitMainFuncDef(MainFuncDef mainFuncDef) {
        // 归零计数器
        VirtualRegister.setZero();
        // 创建函数
        currentFunction = new Function(mainFuncDef.ident.token.value, null, new FuncType("int"), currentModule);
        // 创建第一个代码块
        currentBasicBlock = new BasicBlock(VirtualRegister.getRegister(), currentFunction);
        // 将代码块添加到函数中
        currentFunction.addBasicBlock(currentBasicBlock);
        currentModule.addFunctions(currentFunction);
    }

    public void visitReturn(Stmt stmt) {
        if (stmt.exps.size() == 0)
            currentBasicBlock.appendInst(new RetInstruction(null));
        else{
            currentBasicBlock.appendInst(new RetInstruction(visitExp(stmt.exps.get(0))));
        }
    }

}
