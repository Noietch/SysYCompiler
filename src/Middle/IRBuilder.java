package Middle;

import Front.SyntaxAnalyzer.Element.*;
import Middle.IRElement.*;
import Middle.IRElement.Instructions.*;
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

    public void buildIR() {
        visitCompUnit(syntaxTreeRoot);
    }

    public String getIR() {
        buildIR();
        return currentModule.toString();
    }

    public void initIncludeFunction() {
        throw new RuntimeException("NOT IMPLEMENT YET");
    }

    public void visitCompUnit(CompUnit compUnit) {
        if (compUnit.decls.size() != 0)
            for (Decl decl : compUnit.decls) visitDecl(decl);
        else if (compUnit.funcDefs.size() != 0)
            for (FuncDef funcDef : compUnit.funcDefs) visitFuncDef(funcDef);
        else
            visitMainFuncDef(compUnit.mainFuncDef);
    }

    public void visitDecl(Decl decl) {
        if (decl.constDecl != null) visitConstDecl(decl.constDecl);
        else visitVarDecl(decl.varDecl);
    }

    public void visitConstDecl(ConstDecl constDecl) {
        for (ConstDef constDef : constDecl.constDefs) visitConstDef(constDef);
    }

    public void visitVarDecl(VarDecl varDecl) {
        for (VarDef varDef : varDecl.varDefs) visitVarDef(varDef);
    }

    public void visitFuncDef(FuncDef funcDef) {
        // 归零计数器
        VirtualRegister.setZero();
        // 创建函数
        currentFunction = new Function("@" + funcDef.ident.token.value, funcDef.funcFParams, funcDef.funcType, currentModule);
        // 创建第一个代码块
        currentBasicBlock = new BasicBlock(VirtualRegister.getRegister(), currentFunction);
        // 将代码块添加到函数中
        currentFunction.addBasicBlock(currentBasicBlock);
        currentModule.addFunctions(currentFunction);
        currentValueTable.addValue(currentFunction.name, currentFunction);

        currentValueTable = currentValueTable.newSon();
        visitBlock(funcDef.block);
        currentValueTable = currentValueTable.back();
    }

    public void visitMainFuncDef(MainFuncDef mainFuncDef) {
        // 归零计数器
        VirtualRegister.setZero();
        // 创建函数
        currentFunction = new Function("@" + mainFuncDef.ident.token.value, null, new FuncType("int"), currentModule);
        // 创建第一个代码块
        currentBasicBlock = new BasicBlock(VirtualRegister.getRegister(), currentFunction);
        // 将代码块添加到函数中
        currentFunction.addBasicBlock(currentBasicBlock);
        currentModule.addFunctions(currentFunction);
        currentValueTable.addValue(currentFunction.name, currentFunction);

        currentValueTable = currentValueTable.newSon();
        visitBlock(mainFuncDef.block);
        currentValueTable = currentValueTable.back();
    }

    public Value visitConstExp(ConstExp constExp) {
        return visitAddExp(constExp.addExp);
    }

    public Value visitConstInitVal(ConstInitVal constInitVal) {
        if (constInitVal.initType == VarType.Var) {
            return visitConstExp((ConstExp) constInitVal.syntaxNodes.get(0));
        } else throw new RuntimeException("[visitConstInitVal]");
    }

    public void visitConstDef(ConstDef constDef) {
        Value value = new Value(VirtualRegister.getRegister());
        if (constDef.constExps.size() == 0) value.setType(Value.Type.integer);
        else throw new RuntimeException("[visitConstDef] Array not implement");
        // 分配空间
        AllocateInstruction allocateInstruction = new AllocateInstruction(value);
        currentBasicBlock.appendInst(allocateInstruction);
        // 初值
        Value initValValue = visitConstInitVal(constDef.constInitVal);
        StoreInstruction storeInstruction = new StoreInstruction(initValValue, value);
        currentBasicBlock.appendInst(storeInstruction);
        // 记录符号
        currentValueTable.addValue(constDef.ident.token.value, value);
    }

    public Value visitInitVal(InitVal initVal) {
        if (initVal.initType == VarType.Var) {
            return visitExp((Exp) initVal.syntaxNodes.get(0));
        } else throw new RuntimeException("[visitInitVal]");
    }

    public void visitVarDef(VarDef varDef) {
        Value value = new Value(VirtualRegister.getRegister());
        if (varDef.constExps.size() == 0) value.setType(Value.Type.integer);
        else throw new RuntimeException("[visitVarDef] Array not implement");
        AllocateInstruction allocateInstruction = new AllocateInstruction(value);
        currentBasicBlock.appendInst(allocateInstruction);
        // 初值
        if (varDef.initVal != null) {
            Value initValValue = visitInitVal(varDef.initVal);
            StoreInstruction storeInstruction = new StoreInstruction(initValValue, value);
            currentBasicBlock.appendInst(storeInstruction);
        }
        // 记录符号
        currentValueTable.addValue(varDef.ident.token.value, value);
    }

    public Value visitExp(Exp exp) {
        return visitAddExp(exp.addExp);
    }


    public Value visitUnaryExp(UnaryExp unaryExp) {
        if (unaryExp.primaryExp != null) return visitPrimaryExp(unaryExp.primaryExp);
        else if (unaryExp.ident != null) {
            Function function = (Function) currentValueTable.getRegister("@" + unaryExp.ident.token.value);
            User user = new User(VirtualRegister.getRegister());
            CallInstruction callInstruction = new CallInstruction(function, user);
            currentBasicBlock.appendInst(callInstruction);
            return user;
        } else {
            if (unaryExp.unaryOp.opType == UnaryOp.type.MINUS) {
                Value value1 = new Constant("0");
                Value value2 = visitUnaryExp(unaryExp.unaryExp);
                Op op = new Op(Op.Type.sub);
                User res = new User(VirtualRegister.getRegister());
                currentBasicBlock.appendInst(new BinaryInstruction(res, value1, value2, op));
                return res;
            } else if (unaryExp.unaryOp.opType == UnaryOp.type.PLUS) {
                return visitUnaryExp(unaryExp.unaryExp);
            } else throw new RuntimeException();
        }
    }

    public Value visitPrimaryExp(PrimaryExp primaryExp) {
        if (primaryExp.exp != null) return visitExp(primaryExp.exp);
        else if (primaryExp.lVal != null) {
            User res = new User(VirtualRegister.getRegister());
            Value value = currentValueTable.getRegister(primaryExp.lVal.ident.token.value);
            LoadInstruction loadInstruction = new LoadInstruction(res, value);
            currentBasicBlock.appendInst(loadInstruction);
            return res;
        } else return new Constant(primaryExp.getNumber());
    }

    public Value visitLVal(LVal lVal) {
        if (lVal.type == VarType.Var) {
            return currentValueTable.getRegister(lVal.ident.token.value);
        } else throw new RuntimeException();
    }

    public void visitBlockItem(BlockItem blockItem) {
        if (blockItem.decl != null) visitDecl(blockItem.decl);
        else visitStmt(blockItem.stmt);
    }

    public void visitBlock(Block block) {
        for (BlockItem blockItem : block.blockItems) visitBlockItem(blockItem);
    }

    public void visitStmt(Stmt stmt) {
        if (stmt.getType() == Stmt.Type.Return) visitReturn(stmt);
        else if (stmt.getType() == Stmt.Type.AssignmentInput) visitInput(stmt);
        else if (stmt.getType() == Stmt.Type.AssignmentExp) visitAssignmentExp(stmt);
        else if (stmt.getType() == Stmt.Type.Branch_IF) visitIf(stmt);
        else if (stmt.getType() == Stmt.Type.Branch_ELSE) visitIf(stmt);
        else if (stmt.getType() == Stmt.Type.Block) {
            currentValueTable = currentValueTable.newSon();
            visitBlock(stmt.block);
            currentValueTable = currentValueTable.back();
        }
    }

    public void visitReturn(Stmt stmt) {
        if (stmt.exps.size() == 0)
            currentBasicBlock.appendInst(new RetInstruction(null));
        else {
            currentBasicBlock.appendInst(new RetInstruction(visitExp(stmt.exps.get(0))));
        }
    }

    public void visitIf(Stmt stmt) {
        Value cond = visitCond(stmt.cond);
        BasicBlock ifBlock = new BasicBlock(VirtualRegister.getRegister(), currentFunction);
        currentFunction.addBasicBlock(ifBlock);
        // 加跳转
        BranchInstruction branchInstruction = new BranchInstruction(cond, ifBlock, null);
        currentBasicBlock.appendInst(branchInstruction);

        currentBasicBlock = ifBlock;
        visitStmt(stmt.stmts.get(0));
        BranchInstruction out = new BranchInstruction(null);
        currentBasicBlock.appendInst(out);

        BasicBlock outBlock = null;
        if (stmt.type == Stmt.Type.Branch_ELSE) {
            // 访问else中的内容
            BasicBlock elseBlock = new BasicBlock(VirtualRegister.getRegister(), currentFunction);
            currentFunction.addBasicBlock(elseBlock);
            currentBasicBlock = elseBlock;
            visitStmt(stmt.stmts.get(1));
            BranchInstruction out2 = new BranchInstruction(null);
            currentBasicBlock.appendInst(out2);
            outBlock = new BasicBlock(VirtualRegister.getRegister(), currentFunction);
            out2.setLabelTrue(outBlock);
        }
        else outBlock = new BasicBlock(VirtualRegister.getRegister(), currentFunction);
        branchInstruction.setLabelFalse(outBlock);
        out.setLabelTrue(outBlock);
        currentBasicBlock = outBlock;
        currentFunction.addBasicBlock(outBlock);
    }

    public void visitInput(Stmt stmt) {
        Value value1 = visitLVal(stmt.lVal);
        Value value2 = new Function("getint", null, new FuncType("void"), currentModule);
        StoreInstruction storeInstruction = new StoreInstruction(value2, value1);
        currentBasicBlock.appendInst(storeInstruction);
    }

    public void visitAssignmentExp(Stmt stmt) {
        Value value1 = visitLVal(stmt.lVal);
        Value value2 = visitExp(stmt.exps.get(0));
        StoreInstruction storeInstruction = new StoreInstruction(value2, value1);
        currentBasicBlock.appendInst(storeInstruction);
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


    public Value visitRelExp(RelExp relExp) {
        if (relExp.addExps.size() == 1) return visitAddExp(relExp.addExps.get(0));
        else {
            Value value1 = visitAddExp(relExp.addExps.get(0));
            Value value2 = visitAddExp(relExp.addExps.get(1));
            Op op = new Op(Op.Op2Type(relExp.unaryOps.get(0).token));
            User res = new User(VirtualRegister.getRegister());
            currentBasicBlock.appendInst(new IcmpInstruction(res, value1, value2, op));
            for (int i = 2; i < relExp.addExps.size(); i++) {
                value1 = res;
                value2 = visitAddExp(relExp.addExps.get(i));
                op = new Op(Op.Op2Type(relExp.unaryOps.get(i - 1).token));
                res = new User(VirtualRegister.getRegister());
                currentBasicBlock.appendInst(new IcmpInstruction(res, value1, value2, op));
            }
            return res;
        }
    }

    public Value visitEqExp(EqExp eqExp) {
        if (eqExp.relExps.size() == 1) return visitRelExp(eqExp.relExps.get(0));
        else {
            Value value1 = visitRelExp(eqExp.relExps.get(0));
            Value value2 = visitRelExp(eqExp.relExps.get(1));
            Op op = new Op(Op.Op2Type(eqExp.unaryOps.get(0).token));
            User res = new User(VirtualRegister.getRegister());
            currentBasicBlock.appendInst(new IcmpInstruction(res, value1, value2, op));
            for (int i = 2; i < eqExp.relExps.size(); i++) {
                value1 = res;
                value2 = visitRelExp(eqExp.relExps.get(i));
                op = new Op(Op.Op2Type(eqExp.unaryOps.get(i - 1).token));
                res = new User(VirtualRegister.getRegister());
                currentBasicBlock.appendInst(new IcmpInstruction(res, value1, value2, op));
            }
            return res;
        }
    }

    public Value visitLAndExp(LAndExp lAndExp) {
        if (lAndExp.eqExps.size() == 1) return visitEqExp(lAndExp.eqExps.get(0));
        else {
            Value value1 = visitEqExp(lAndExp.eqExps.get(0));
            Value value2 = visitEqExp(lAndExp.eqExps.get(1));
            Op op = new Op(Op.Op2Type(lAndExp.unaryOps.get(0).token));
            User res = new User(VirtualRegister.getRegister());
            currentBasicBlock.appendInst(new IcmpInstruction(res, value1, value2, op));
            for (int i = 2; i < lAndExp.eqExps.size(); i++) {
                value1 = res;
                value2 = visitEqExp(lAndExp.eqExps.get(i));
                op = new Op(Op.Op2Type(lAndExp.unaryOps.get(i - 1).token));
                res = new User(VirtualRegister.getRegister());
                currentBasicBlock.appendInst(new IcmpInstruction(res, value1, value2, op));
            }
            return res;
        }
    }

    public Value visitLOrExp(LOrExp lOrExp) {
        if (lOrExp.lAndExps.size() == 1) return visitLAndExp(lOrExp.lAndExps.get(0));
        else {
            Value value1 = visitLAndExp(lOrExp.lAndExps.get(0));
            Value value2 = visitLAndExp(lOrExp.lAndExps.get(1));
            Op op = new Op(Op.Op2Type(lOrExp.unaryOps.get(0).token));
            User res = new User(VirtualRegister.getRegister());
            currentBasicBlock.appendInst(new IcmpInstruction(res, value1, value2, op));
            for (int i = 2; i < lOrExp.lAndExps.size(); i++) {
                value1 = res;
                value2 = visitLAndExp(lOrExp.lAndExps.get(i));
                op = new Op(Op.Op2Type(lOrExp.unaryOps.get(i - 1).token));
                res = new User(VirtualRegister.getRegister());
                currentBasicBlock.appendInst(new IcmpInstruction(res, value1, value2, op));
            }
            return res;
        }
    }

    public Value visitCond(Cond cond) {
        return visitLOrExp(cond.lOrExp);
    }
}
