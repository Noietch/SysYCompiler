package Middle;

import Front.SyntaxAnalyzer.Element.*;
import Middle.IRElement.*;
import Middle.IRElement.Basic.*;
import Middle.IRElement.Basic.Module;
import Middle.IRElement.Instructions.*;
import Middle.IRElement.Type.*;

import java.util.ArrayList;
import java.util.Collections;

public class IRBuilder {
    public CompUnit syntaxTreeRoot; // 语法树根
    public ValueTable currentValueTable = new ValueTable(null);
    public BasicBlock currentBasicBlock = null;

    public BasicBlock.LoopBlock currentLoop = null;
    public Module currentModule = new Module();
    public Function currentFunction = null;

    public int currentPos = 0;

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

    public void visitCompUnit(CompUnit compUnit) {
        if (compUnit.decls.size() != 0)
            for (Decl decl : compUnit.decls) visitDecl(decl);
        if (compUnit.funcDefs.size() != 0)
            for (FuncDef funcDef : compUnit.funcDefs) visitFuncDef(funcDef);
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


    public void visitConstDef(ConstDef constDef) {
        if (currentValueTable.father != null) {
            Value param = new Value();
            if (constDef.constExps.size() == 0) {
                param.setType(ValueType.i32);
                Value pointer = new Value(VirtualRegister.getRegister(), new ValueType.Pointer(param.type));
                // 分配空间
                AllocateInstruction allocateInstruction = new AllocateInstruction(pointer, param);
                currentBasicBlock.appendInst(allocateInstruction);
                // 初值
                visitConstInitVal(pointer, constDef.constInitVal, false);
                // 记录符号
                currentValueTable.addValue(constDef.ident.token.value, pointer);
            } else {
                // 构造数组类型
                ValueType.Type baseType = ValueType.i32;
                Collections.reverse(constDef.constExps);
                for (ConstExp constExp : constDef.constExps) {
                    int size = constExp.eval();
                    baseType = new ValueType.ArrayType(size, baseType);
                }
                param.setType(baseType);
                String name = VirtualRegister.getRegister();
                Value arrayPointer = new Value(name, new ValueType.Pointer(param.type));
                // 分配空间
                AllocateInstruction allocateInstruction = new AllocateInstruction(arrayPointer, param);
                currentBasicBlock.appendInst(allocateInstruction);
                // 获取数组首地址指针
                Value firstAddr = null;
                Value temp = arrayPointer;
                for (ConstExp ignored : constDef.constExps) {
                    firstAddr = new Value(VirtualRegister.getRegister(), new ValueType.Pointer(temp.getType().getType()));
                    GetElementPtr getElementPtr = new GetElementPtr(firstAddr, temp, new Constant("0"), new Constant("0"));
                    currentBasicBlock.appendInst(getElementPtr);
                    temp = firstAddr;
                }
                // 初值
                visitConstInitVal(firstAddr, constDef.constInitVal, true);
                currentPos = 0;
                // 记录符号
                arrayPointer.setFirstAddr(firstAddr);
                currentValueTable.addValue(constDef.ident.token.value, arrayPointer);
            }
        } else {
            GlobalVariable globalVariable = new GlobalVariable(constDef.ident.token.value, true, constDef.constInitVal, null);
            if (constDef.constExps.size() == 0) {
                globalVariable.setType(ValueType.i32);
                currentModule.addGlobalVariable(globalVariable);
            } else {
                // 构造数组类型
                ValueType.Type baseType = ValueType.i32;
                Collections.reverse(constDef.constExps);
                for (ConstExp constExp : constDef.constExps) {
                    int size = constExp.eval();
                    baseType = new ValueType.ArrayType(size, baseType);
                }
                globalVariable.setType(new ValueType.Pointer(baseType));
                currentModule.addGlobalVariable(globalVariable);
            }
            currentValueTable.addValue(constDef.ident.token.value, globalVariable);
        }
    }



    public void visitConstInitVal(Value pointer, ConstInitVal constInitVal, boolean isArray) {
        Value res;
        if (constInitVal.initType == VarType.Var) {
            if (isArray && currentPos != 0) {
                res = new Value(VirtualRegister.getRegister(), pointer.type);
                GetElementPtr getElementPtr = new GetElementPtr(res, pointer, new Constant(Integer.toString(currentPos)));
                currentBasicBlock.appendInst(getElementPtr);
            } else res = pointer;
            Value initValValue = visitConstExp((ConstExp) constInitVal.syntaxNodes.get(0));
            StoreInstruction storeInstruction = new StoreInstruction(initValValue, res);
            currentBasicBlock.appendInst(storeInstruction);
            currentPos++;
        } else {
            for (SyntaxNode syntaxNode : constInitVal.syntaxNodes) {
                ConstInitVal initVal = (ConstInitVal) syntaxNode;
                visitConstInitVal(pointer, initVal, isArray);
            }
        }
    }

    public void visitVarDef(VarDef varDef) {
        if (currentValueTable.father != null) {
            Value param = new Value();
            if (varDef.constExps.size() == 0) {
                param.setType(ValueType.i32);
                Value value = new Value(VirtualRegister.getRegister(), new ValueType.Pointer(param.type));
                // 分配空间
                AllocateInstruction allocateInstruction = new AllocateInstruction(value, param);
                currentBasicBlock.appendInst(allocateInstruction);
                // 初值
                if (varDef.initVal != null) visitInitVal(value, varDef.initVal, false);
                // 记录符号
                currentValueTable.addValue(varDef.ident.token.value, value);
            } else {
                // 构造数组类型
                ValueType.Type baseType = ValueType.i32;
                Collections.reverse(varDef.constExps);
                for (ConstExp constExp : varDef.constExps) {
                    baseType = new ValueType.ArrayType(constExp.eval(), baseType);
                }
                param.setType(baseType);
                String name = VirtualRegister.getRegister();
                Value arrayPointer = new Value(name, new ValueType.Pointer(param.type));
                // 分配空间
                AllocateInstruction allocateInstruction = new AllocateInstruction(arrayPointer, param);
                currentBasicBlock.appendInst(allocateInstruction);
                // 获取数组首地址指针
                Value firstAddr = null;
                Value temp = arrayPointer;
                for (ConstExp ignored : varDef.constExps) {
                    firstAddr = new Value(VirtualRegister.getRegister(), new ValueType.Pointer(temp.getType().getType()));
                    GetElementPtr getElementPtr = new GetElementPtr(firstAddr, temp, new Constant("0"), new Constant("0"));
                    currentBasicBlock.appendInst(getElementPtr);
                    temp = firstAddr;
                }
                if (varDef.initVal != null) {
                    // 初值
                    visitInitVal(firstAddr, varDef.initVal, true);
                    currentPos = 0;
                }
                // 记录符号
                arrayPointer.setFirstAddr(firstAddr);
                currentValueTable.addValue(varDef.ident.token.value, arrayPointer);
            }
        } else {
            GlobalVariable globalVariable = new GlobalVariable(varDef.ident.token.value, false, null, varDef.initVal);
            if (varDef.constExps.size() == 0) {
                globalVariable.setType(ValueType.i32);
                currentModule.addGlobalVariable(globalVariable);
            } else {
                // 构造数组类型
                ValueType.Type baseType = ValueType.i32;
                Collections.reverse(varDef.constExps);
                for (ConstExp constExp : varDef.constExps) {
                    int size = constExp.eval();
                    baseType = new ValueType.ArrayType(size, baseType);
                }
                globalVariable.setType(new ValueType.Pointer(baseType));
                currentModule.addGlobalVariable(globalVariable);
            }
            currentValueTable.addValue(varDef.ident.token.value, globalVariable);
        }
    }

    public void visitInitVal(Value pointer, InitVal initVal, Boolean isArray) {
        if (initVal.initType == VarType.Var) {
            Value res;
            if (isArray && currentPos != 0) {
                res = new Value(VirtualRegister.getRegister(), pointer.type);
                GetElementPtr getElementPtr = new GetElementPtr(res, pointer, new Constant(Integer.toString(currentPos)));
                currentBasicBlock.appendInst(getElementPtr);
            } else res = pointer;
            Value initValValue = visitExp((Exp) initVal.syntaxNodes.get(0));
            StoreInstruction storeInstruction = new StoreInstruction(initValValue, res);
            currentBasicBlock.appendInst(storeInstruction);
            currentPos++;
        } else {
            for (SyntaxNode syntaxNode : initVal.syntaxNodes) {
                visitInitVal(pointer, (InitVal) syntaxNode, isArray);
            }
        }
    }

    public Value visitExp(Exp exp) {
        return visitAddExp(exp.addExp);
    }

    public Value visitUnaryExp(UnaryExp unaryExp) {
        if (unaryExp.primaryExp != null) return visitPrimaryExp(unaryExp.primaryExp);
        else if (unaryExp.ident != null) {
            Function function = (Function) currentValueTable.getRegister("@" + unaryExp.ident.token.value);
            User user = new User(VirtualRegister.getRegister(), ValueType.i32);
            CallInstruction callInstruction = new CallInstruction(function, user);
            currentBasicBlock.appendInst(callInstruction);
            return user;
        } else {
            if (unaryExp.unaryOp.opType == UnaryOp.Type.MINUS) {
                Value value1 = new Constant("0");
                Value value2 = visitUnaryExp(unaryExp.unaryExp);
                Op op = new Op(Op.Type.sub);
                User res = new User(VirtualRegister.getRegister(), ValueType.i32);
                currentBasicBlock.appendInst(new BinaryInstruction(res, value1, value2, op));
                return res;
            } else if (unaryExp.unaryOp.opType == UnaryOp.Type.PLUS) {
                return visitUnaryExp(unaryExp.unaryExp);
            } else throw new RuntimeException();
        }
    }

    public Value visitPrimaryExp(PrimaryExp primaryExp) {
        if (primaryExp.exp != null) return visitExp(primaryExp.exp);
        else if (primaryExp.lVal != null) {
            Value value = visitLVal(primaryExp.lVal);
            User res = new User(VirtualRegister.getRegister(), ValueType.i32);
            LoadInstruction loadInstruction = new LoadInstruction(res, value);
            currentBasicBlock.appendInst(loadInstruction);
            return res;
        } else return new Constant(primaryExp.getNumber());
    }

    public Value visitLVal(LVal lVal) {
        if (lVal.type == VarType.Var) {
            return currentValueTable.getRegister(lVal.ident.token.value);
        } else {
            User pos;
            Value array = currentValueTable.getRegister(lVal.ident.token.value);
            // 一维数组
            if (lVal.exps.size() == 1) {
                Value v1 = visitExp(lVal.exps.get(0));
                Constant v2 = new Constant("0");
                pos = new User(VirtualRegister.getRegister(), v2.getType());
                currentBasicBlock.appendInst(new BinaryInstruction(pos, v1, v2, new Op(Op.Type.add)));
            } else {
                // 二维数组
                ArrayList<Integer> dim = array.getType().getDim();
                Value v1 = visitExp(lVal.exps.get(0));
                Constant v2 = new Constant(Integer.toString(dim.get(1)));
                User result = new User(VirtualRegister.getRegister(), v2.getType());
                currentBasicBlock.appendInst(new BinaryInstruction(result, v1, v2, new Op(Op.Type.mul)));

                v1 = visitExp(lVal.exps.get(1));
                pos = new User(VirtualRegister.getRegister(), v2.getType());
                currentBasicBlock.appendInst(new BinaryInstruction(pos, v1, result, new Op(Op.Type.add)));
            }
            Value res = new Value(VirtualRegister.getRegister(), new ValueType.Pointer(pos.getType()));
            currentBasicBlock.appendInst(new GetElementPtr(res, array.firstAddr, pos));
            return res;
        }
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
        else if (stmt.getType() == Stmt.Type.Loop) visitLoop(stmt);
        else if (stmt.getType() == Stmt.Type.Break) visitBreak();
        else if (stmt.getType() == Stmt.Type.Continue) visitContinue();
        else if (stmt.getType() == Stmt.Type.Block) {
            currentValueTable = currentValueTable.newSon();
            visitBlock(stmt.block);
            currentValueTable = currentValueTable.back();
        }
    }

    public void visitReturn(Stmt stmt) {
        if (stmt.exps.size() == 0) {
            currentBasicBlock.setTerminator(new RetInstruction(null));
        } else {
            currentBasicBlock.setTerminator(new RetInstruction(visitExp(stmt.exps.get(0))));
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

        BasicBlock outBlock;
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
            branchInstruction.setLabelFalse(elseBlock);
        } else {
            outBlock = new BasicBlock(VirtualRegister.getRegister(), currentFunction);
            branchInstruction.setLabelFalse(outBlock);
        }
        out.setLabelTrue(outBlock);
        currentBasicBlock = outBlock;
        currentFunction.addBasicBlock(outBlock);
    }

    public void visitBreak() {
        currentBasicBlock.setTerminator(new BranchInstruction(currentLoop.falseBranch));
    }

    public void visitContinue() {
        currentBasicBlock.setTerminator(new BranchInstruction(currentLoop.judgeBranch));
    }

    public void visitLoop(Stmt stmt) {
        // 新建判断基本分支指令，加到目前的基本块中
        BranchInstruction judge = new BranchInstruction(null);
        currentBasicBlock.appendInst(judge);
        // 新建结束模块
        BasicBlock outBlock = new BasicBlock(currentFunction);
        // 新建判断基本块，回填到判断分支指令
        BasicBlock judgeBlock = new BasicBlock(VirtualRegister.getRegister(), currentFunction);
        currentFunction.addBasicBlock(judgeBlock);
        judge.setLabelTrue(judgeBlock);
        // 进入判断基本块
        currentBasicBlock = judgeBlock;
        Value cond = visitCond(stmt.cond);
        // 新建分支指令
        BranchInstruction branchInstruction = new BranchInstruction(cond, null, null);
        currentBasicBlock.appendInst(branchInstruction);
        // 新建ture基本块,回填判断块中的分支指令
        BasicBlock.LoopBlock ifBlock = new BasicBlock.LoopBlock(VirtualRegister.getRegister(), currentFunction);
        currentLoop = ifBlock;
        currentFunction.addBasicBlock(ifBlock);
        ifBlock.setJudgeBranch(judgeBlock);
        ifBlock.setFalseBranch(outBlock);
        branchInstruction.setLabelTrue(ifBlock);
        currentBasicBlock = ifBlock;
        visitStmt(stmt.stmts.get(0));
        // 跳回判断基本块
        currentBasicBlock.appendInst(new BranchInstruction(judgeBlock));
        // 设置outBlock的计数器
        outBlock.setVirtualNum(VirtualRegister.getRegister());
        branchInstruction.setLabelFalse(outBlock);
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
            User res = new User(VirtualRegister.getRegister(), ValueType.i32);
            currentBasicBlock.appendInst(new BinaryInstruction(res, value1, value2, op));
            for (int i = 2; i < mulExp.unaryExps.size(); i++) {
                value1 = res;
                value2 = visitUnaryExp(mulExp.unaryExps.get(i));
                op = new Op(Op.Op2Type(mulExp.unaryOps.get(i - 1).token));
                res = new User(VirtualRegister.getRegister(), ValueType.i32);
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
            User res = new User(VirtualRegister.getRegister(), ValueType.i32);
            currentBasicBlock.appendInst(new BinaryInstruction(res, value1, value2, op));
            for (int i = 2; i < addExp.mulExps.size(); i++) {
                value1 = res;
                value2 = visitMulExp(addExp.mulExps.get(i));
                op = new Op(Op.Op2Type(addExp.unaryOps.get(i - 1).token));
                res = new User(VirtualRegister.getRegister(), ValueType.i32);
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
            User res = new User(VirtualRegister.getRegister(), ValueType.i32);
            currentBasicBlock.appendInst(new IcmpInstruction(res, value1, value2, op));
            for (int i = 2; i < relExp.addExps.size(); i++) {
                value1 = res;
                value2 = visitAddExp(relExp.addExps.get(i));
                op = new Op(Op.Op2Type(relExp.unaryOps.get(i - 1).token));
                res = new User(VirtualRegister.getRegister(), ValueType.i32);
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
            User res = new User(VirtualRegister.getRegister(), ValueType.i32);
            currentBasicBlock.appendInst(new IcmpInstruction(res, value1, value2, op));
            for (int i = 2; i < eqExp.relExps.size(); i++) {
                value1 = res;
                value2 = visitRelExp(eqExp.relExps.get(i));
                op = new Op(Op.Op2Type(eqExp.unaryOps.get(i - 1).token));
                res = new User(VirtualRegister.getRegister(), ValueType.i32);
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
            User res = new User(VirtualRegister.getRegister(), ValueType.i32);
            currentBasicBlock.appendInst(new IcmpInstruction(res, value1, value2, op));
            for (int i = 2; i < lAndExp.eqExps.size(); i++) {
                value1 = res;
                value2 = visitEqExp(lAndExp.eqExps.get(i));
                op = new Op(Op.Op2Type(lAndExp.unaryOps.get(i - 1).token));
                res = new User(VirtualRegister.getRegister(), ValueType.i32);
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
            User res = new User(VirtualRegister.getRegister(), ValueType.i32);
            currentBasicBlock.appendInst(new IcmpInstruction(res, value1, value2, op));
            for (int i = 2; i < lOrExp.lAndExps.size(); i++) {
                value1 = res;
                value2 = visitLAndExp(lOrExp.lAndExps.get(i));
                op = new Op(Op.Op2Type(lOrExp.unaryOps.get(i - 1).token));
                res = new User(VirtualRegister.getRegister(), ValueType.i32);
                currentBasicBlock.appendInst(new IcmpInstruction(res, value1, value2, op));
            }
            return res;
        }
    }

    public Value visitCond(Cond cond) {
        return visitLOrExp(cond.lOrExp);
    }
}
