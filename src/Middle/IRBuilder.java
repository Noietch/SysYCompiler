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
        buildDeclare();
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

    public void buildDeclare() {
        Function getInt = new Function("getint", new FuncType("int"), currentModule);
        getInt.setNotDefine();
        currentModule.addFunctions(getInt);
        currentValueTable.addValue(getInt.name, getInt);

        Function putInt = new Function("putint", new FuncType("void"), currentModule);
        putInt.addParams(new Value("declare", ValueType.i32));
        putInt.setNotDefine();
        currentModule.addFunctions(putInt);
        currentValueTable.addValue(putInt.name, putInt);

        Function putCh = new Function("putch", new FuncType("void"), currentModule);
        putCh.addParams(new Value("declare", ValueType.i32));
        putCh.setNotDefine();
        currentModule.addFunctions(putCh);
        currentValueTable.addValue(putCh.name, putCh);
    }

    public void visitFuncDef(FuncDef funcDef) {
        // 归零计数器
        VirtualRegister.setZero();
        // 创建函数
        currentFunction = new Function(funcDef.ident.token.value, funcDef.funcType, currentModule);
        currentModule.addFunctions(currentFunction);
        currentValueTable.addValue(currentFunction.name, currentFunction);
        // 创建第一个代码块
        currentBasicBlock = new BasicBlock(null, currentFunction);
        // 将代码块添加到函数中
        currentFunction.addBasicBlock(currentBasicBlock);
        // 进入符号表
        currentValueTable = currentValueTable.newSon();
        // 访问参数表
        if (funcDef.funcFParams != null)
            visitFuncFParams(funcDef.funcFParams);
        else currentBasicBlock.setName(VirtualRegister.getRegister());
        visitBlock(funcDef.block);
        if (!funcDef.isReturn()) {
            if (currentFunction.returnType == DataType.Void)
                currentBasicBlock.appendInst(new RetInstruction(null));
            else
                currentBasicBlock.appendInst(new RetInstruction(new Constant("0")));
        }
        currentValueTable = currentValueTable.back();
    }

    public void visitFuncFParams(FuncFParams funcFParams) {
        for (FuncFParam funcFParam : funcFParams.funcFParams)
            visitFuncFParam(funcFParam);
        // 回填一个块的名称
        currentBasicBlock.setName(VirtualRegister.getRegister());
        // 将参数到里的值填到新的内存空间里去
        for (int i = 0; i < currentFunction.funcFParams.size(); i++)
            initFParam(currentFunction.funcFParams.get(i), funcFParams.funcFParams.get(i));
    }

    public void initFParam(Value value, FuncFParam funcFParam) {
        Value pointer = new Value(VirtualRegister.getRegister(), new ValueType.Pointer(value.type));
        AllocateInstruction allocateInstruction = new AllocateInstruction(pointer, value);
        StoreInstruction storeInstruction = new StoreInstruction(value, pointer);
        currentBasicBlock.appendInst(allocateInstruction);
        currentBasicBlock.appendInst(storeInstruction);
        currentValueTable.addValue(funcFParam.ident.token.value, pointer);
    }

    public void visitFuncFParam(FuncFParam funcFParam) {
        ValueType.Type paramType;
        if (funcFParam.varType == VarType.Var) paramType = ValueType.i32;
        else if (funcFParam.varType == VarType.oneDimArray) paramType = new ValueType.Pointer(ValueType.i32);
        else if (funcFParam.varType == VarType.twoDimArray) {
            int size = ((Constant) visitConstExp(funcFParam.constExps.get(0))).getValue();
            paramType = new ValueType.ArrayType(size, ValueType.i32);
            paramType = new ValueType.Pointer(paramType);
        } else throw new RuntimeException("funcFParam TYpe");
        Value param = new Value(VirtualRegister.getRegister(), paramType);
        currentFunction.addParams(param);
    }

    public void visitMainFuncDef(MainFuncDef mainFuncDef) {
        // 归零计数器
        VirtualRegister.setZero();
        // 创建函数`
        currentFunction = new Function(mainFuncDef.ident.token.value, new FuncType("int"), currentModule);
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
                ConstExp constExp = (ConstExp) constDef.constInitVal.syntaxNodes.get(0);
                int size = ((Constant) visitConstExp(constExp)).getValue();
                currentValueTable.addValue(constDef.ident.token.value, new Constant(Integer.toString(size)));
            } else {
                // 构造数组类型
                ValueType.Type baseType = ValueType.i32;
                Collections.reverse(constDef.constExps);
                for (ConstExp constExp : constDef.constExps) {
                    int size = ((Constant) visitConstExp(constExp)).getValue();
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
                    firstAddr = new Value(VirtualRegister.getRegister(), new ValueType.Pointer(temp.getInnerType().getType()));
                    GetElementPtr getElementPtr = new GetElementPtr(firstAddr, temp, new Constant("0"), new Constant("0"));
                    currentBasicBlock.appendInst(getElementPtr);
                    temp = firstAddr;
                }
                // 初值
                visitConstInitVal(firstAddr, constDef.constInitVal, true);
                currentPos = 0;
                // 记录符号
                currentValueTable.addValue(constDef.ident.token.value, arrayPointer);
            }
        } else {
            ArrayList<Object> init = buildInit(constDef.constInitVal);
            GlobalVariable globalVariable = new GlobalVariable(constDef.ident.token.value, true, init);
            if (constDef.constExps.size() == 0) {
                // 增加
                globalVariable.setType(new ValueType.Pointer(ValueType.i32));
                currentModule.addGlobalVariable(globalVariable);

                ConstExp constExp = (ConstExp) constDef.constInitVal.syntaxNodes.get(0);
                int size = ((Constant) visitConstExp(constExp)).getValue();
                currentValueTable.addValue(constDef.ident.token.value, new Constant(Integer.toString(size)));
            } else {
                // 构造数组类型
                ValueType.Type baseType = ValueType.i32;
                Collections.reverse(constDef.constExps);
                for (ConstExp constExp : constDef.constExps) {
                    int size = ((Constant) visitConstExp(constExp)).getValue();
                    baseType = new ValueType.ArrayType(size, baseType);
                }
                globalVariable.setType(new ValueType.Pointer(baseType));
                currentModule.addGlobalVariable(globalVariable);
                currentValueTable.addValue(constDef.ident.token.value, globalVariable);
            }
        }
    }

    public ArrayList<Object> buildInit(ConstInitVal constInitVal) {
        ArrayList<Object> res = new ArrayList<>();
        if (constInitVal.initType == VarType.Var) {
            Constant initValValue = (Constant) visitConstExp((ConstExp) constInitVal.syntaxNodes.get(0));
            res.add(initValValue.getValue());
        } else {
            for (SyntaxNode syntaxNode : constInitVal.syntaxNodes)
                res.add(buildInit((ConstInitVal) syntaxNode));
        }
        return res;
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
                    int size = ((Constant) visitConstExp(constExp)).getValue();
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
                for (ConstExp ignored : varDef.constExps) {
                    firstAddr = new Value(VirtualRegister.getRegister(), new ValueType.Pointer(temp.getInnerType().getType()));
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
                currentValueTable.addValue(varDef.ident.token.value, arrayPointer);
            }
        } else {
            ArrayList<Object> init = null;
            if (varDef.initVal != null) init = buildInit(varDef.initVal);
            GlobalVariable globalVariable = new GlobalVariable(varDef.ident.token.value, false, init);
            if (varDef.constExps.size() == 0) {
                globalVariable.setType(new ValueType.Pointer(ValueType.i32));
                currentModule.addGlobalVariable(globalVariable);
            } else {
                // 构造数组类型
                ValueType.Type baseType = ValueType.i32;
                Collections.reverse(varDef.constExps);
                for (ConstExp constExp : varDef.constExps) {
                    int size = ((Constant) visitConstExp(constExp)).getValue();
                    baseType = new ValueType.ArrayType(size, baseType);
                }
                globalVariable.setType(new ValueType.Pointer(baseType));
                currentModule.addGlobalVariable(globalVariable);
            }
            currentValueTable.addValue(varDef.ident.token.value, globalVariable);
        }
    }

    public ArrayList<Object> buildInit(InitVal initVal) {
        ArrayList<Object> res = new ArrayList<>();
        if (initVal.initType == VarType.Var) {
            Constant initValValue = (Constant) visitExp((Exp) initVal.syntaxNodes.get(0));
            res.add(initValValue.getValue());
        } else {
            for (SyntaxNode syntaxNode : initVal.syntaxNodes)
                res.add(buildInit((InitVal) syntaxNode));
        }
        return res;
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
            Function function = (Function) currentValueTable.getRegister(unaryExp.ident.token.value);
            User user = new User(null, ValueType.i32);
            CallInstruction callInstruction = new CallInstruction(function, user);
            if (unaryExp.funcRParams != null) {
                for (Exp exp : unaryExp.funcRParams.exps)
                    callInstruction.addParam(visitExp(exp));
            }
            user.setName(VirtualRegister.getRegister());
            currentBasicBlock.appendInst(callInstruction);
            return user;
        } else {
            if (unaryExp.unaryOp.opType == UnaryOp.Type.MINUS) {
                Value value1 = new Constant("0");
                Value value2 = visitUnaryExp(unaryExp.unaryExp);
                Op op = new Op(Op.Type.sub);
                return addBinaryInstruction(value1,value2,op);
            } else if (unaryExp.unaryOp.opType == UnaryOp.Type.PLUS) {
                return visitUnaryExp(unaryExp.unaryExp);
            } else {
                Value value1 = visitUnaryExp(unaryExp.unaryExp);
                Value value2 = new Constant("0");
                User res = new User(VirtualRegister.getRegister(), ValueType.i1);
                IcmpInstruction notInst = new IcmpInstruction(res, value1, value2, new Op(Op.Type.eq));
                currentBasicBlock.appendInst(notInst);
                return res;
            }
        }
    }

    public Value visitPrimaryExp(PrimaryExp primaryExp) {
        if (primaryExp.exp != null) return visitExp(primaryExp.exp);
        else if (primaryExp.lVal != null) {
            Value value = visitLVal(primaryExp.lVal);
            // 保证如果是常量，则直接返回
            if (value.getType() instanceof ValueType.Pointer) {
                User res = new User(VirtualRegister.getRegister(), ValueType.i32);
                LoadInstruction loadInstruction = new LoadInstruction(res, value);
                currentBasicBlock.appendInst(loadInstruction);
                return res;
            } else return value;
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
                pos = new User(VirtualRegister.getRegister(), v2.getInnerType());
                currentBasicBlock.appendInst(new BinaryInstruction(pos, v1, v2, new Op(Op.Type.add)));
            } else {
                // 二维数组
                int secondDim;
                if (array.getInnerType() instanceof ValueType.ArrayType)
                    secondDim = array.getInnerType().getDim().get(1);
                else secondDim = array.getInnerType().getType().getDim().get(0);
                Value v1 = visitExp(lVal.exps.get(0));
                Constant v2 = new Constant(Integer.toString(secondDim));
                User result = new User(VirtualRegister.getRegister(), v2.getInnerType());
                currentBasicBlock.appendInst(new BinaryInstruction(result, v1, v2, new Op(Op.Type.mul)));
                v1 = visitExp(lVal.exps.get(1));
                pos = new User(VirtualRegister.getRegister(), v2.getInnerType());
                currentBasicBlock.appendInst(new BinaryInstruction(pos, v1, result, new Op(Op.Type.add)));
            }
            // 加载数组首地址指针
            Value firstAddr = null;
            Value temp = currentValueTable.getRegister(lVal.getName());
            ValueType.Type lValType = temp.getInnerType();
            if (lValType instanceof ValueType.Pointer) {
                firstAddr = new User(VirtualRegister.getRegister(), new ValueType.Pointer(temp.getInnerType().getType()));
                LoadInstruction loadFirst = new LoadInstruction((User) firstAddr, temp);
                currentBasicBlock.appendInst(loadFirst);
                if (firstAddr.getInnerType() instanceof ValueType.ArrayType) {
                    temp = firstAddr;
                    firstAddr = new Value(VirtualRegister.getRegister(), new ValueType.Pointer(firstAddr.getInnerType().getType().getType()));
                    GetElementPtr getElementPtr = new GetElementPtr(firstAddr, temp, new Constant("0"), new Constant("0"));
                    currentBasicBlock.appendInst(getElementPtr);
                }
            } else {
                for (int i = 0; i < lVal.exps.size(); i++) {
                    firstAddr = new Value(VirtualRegister.getRegister(), new ValueType.Pointer(temp.getInnerType().getType()));
                    GetElementPtr getElementPtr = new GetElementPtr(firstAddr, temp, new Constant("0"), new Constant("0"));
                    currentBasicBlock.appendInst(getElementPtr);
                    temp = firstAddr;
                }
            }
            // 首地址指针偏移
            Value res = new Value(VirtualRegister.getRegister(), new ValueType.Pointer(pos.getInnerType()));
            currentBasicBlock.appendInst(new GetElementPtr(res, firstAddr, pos));
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
        else if (stmt.getType() == Stmt.Type.Print) visitPrint(stmt);
        else if (stmt.getType() == Stmt.Type.Block) {
            currentValueTable = currentValueTable.newSon();
            visitBlock(stmt.block);
            currentValueTable = currentValueTable.back();
        }
    }

    public void visitPrint(Stmt stmt) {
        String format = stmt.formatString.token.value;
        Function putch = (Function) currentValueTable.getRegister("putch");
        Function putInt = (Function) currentValueTable.getRegister("putint");
        int expIndex = 0;
        for (int i = 1; i < format.length() - 1; i++) {
            char ch = format.charAt(i);
            if (ch == '\\') {
                Constant content = new Constant("10");
                CallInstruction put = new CallInstruction(putch, null);
                put.addParam(content);
                currentBasicBlock.appendInst(put);
                i++;
            } else if (ch == '%') {
                CallInstruction put = new CallInstruction(putInt, null);
                put.addParam(visitExp(stmt.exps.get(expIndex)));
                currentBasicBlock.appendInst(put);
                expIndex++;
                i++;
            }
            else {
                Constant content = new Constant(Integer.toString(ch));
                CallInstruction put = new CallInstruction(putch, null);
                put.addParam(content);
                currentBasicBlock.appendInst(put);
            }
        }
    }

    public void visitReturn(Stmt stmt) {
        if (stmt.exps.size() == 0) {
            currentBasicBlock.setTerminator(new RetInstruction(null));
        } else {
            currentBasicBlock.setTerminator(new RetInstruction(visitExp(stmt.exps.get(0))));
        }
        currentFunction.setReturn();
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
        Value res = new Value(VirtualRegister.getRegister(), ValueType.i32);
        Function function = (Function) currentValueTable.getRegister("getint");
        currentBasicBlock.appendInst(new CallInstruction(function, res));
        currentBasicBlock.appendInst(new StoreInstruction(res, value1));
    }

    public void visitAssignmentExp(Stmt stmt) {
        Value value1 = visitLVal(stmt.lVal);
        Value value2 = visitExp(stmt.exps.get(0));
        StoreInstruction storeInstruction = new StoreInstruction(value2, value1);
        currentBasicBlock.appendInst(storeInstruction);
    }

    public Value addBinaryInstruction(Value value1, Value value2, Op op) {
        if (value1 instanceof Constant && value2 instanceof Constant)
            return eval((Constant) value1, (Constant) value2, op);
        else {
            Value newValue1 = value1;
            Value newValue2 = value2;
            if (value1.getType() != ValueType.i32) {
                newValue1 = new User(VirtualRegister.getRegister(), ValueType.i32);
                currentBasicBlock.appendInst(new ZextInstruction(value1, ValueType.i32, newValue1));
            }
            if (value2.getType() != ValueType.i32) {
                newValue2 = new User(VirtualRegister.getRegister(), ValueType.i32);
                currentBasicBlock.appendInst(new ZextInstruction(value2, ValueType.i32, newValue2));
            }
            User res = new User(VirtualRegister.getRegister(), ValueType.i32);
            currentBasicBlock.appendInst(new BinaryInstruction(res, newValue1, newValue2, op));
            return res;
        }
    }

    public Constant eval(Constant c1, Constant c2, Op op) {
        if (op.type == Op.Type.add)
            return new Constant(Integer.toString(Integer.parseInt(c1.name) + Integer.parseInt(c2.name)));
        else if (op.type == Op.Type.sub)
            return new Constant(Integer.toString(Integer.parseInt(c1.name) - Integer.parseInt(c2.name)));
        else if (op.type == Op.Type.mul)
            return new Constant(Integer.toString(Integer.parseInt(c1.name) * Integer.parseInt(c2.name)));
        else if (op.type == Op.Type.sdiv)
            return new Constant(Integer.toString(Integer.parseInt(c1.name) / Integer.parseInt(c2.name)));
        else if (op.type == Op.Type.srem)
            return new Constant(Integer.toString(Integer.parseInt(c1.name) % Integer.parseInt(c2.name)));
        else throw new RuntimeException();
    }

    public Value visitMulExp(MulExp mulExp) {
        if (mulExp.unaryExps.size() == 1) return visitUnaryExp(mulExp.unaryExps.get(0));
        else {
            Value value1 = visitUnaryExp(mulExp.unaryExps.get(0));
            Value value2 = visitUnaryExp(mulExp.unaryExps.get(1));
            Op op = new Op(Op.Op2Type(mulExp.unaryOps.get(0).token));
            Value res = addBinaryInstruction(value1, value2, op);
            for (int i = 2; i < mulExp.unaryExps.size(); i++) {
                value1 = res;
                value2 = visitUnaryExp(mulExp.unaryExps.get(i));
                op = new Op(Op.Op2Type(mulExp.unaryOps.get(i - 1).token));
                res = addBinaryInstruction(value1, value2, op);
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
            Value res = addBinaryInstruction(value1, value2, op);
            for (int i = 2; i < addExp.mulExps.size(); i++) {
                value1 = res;
                value2 = visitMulExp(addExp.mulExps.get(i));
                op = new Op(Op.Op2Type(addExp.unaryOps.get(i - 1).token));
                res = addBinaryInstruction(value1, value2, op);
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
            User res = new User(VirtualRegister.getRegister(), ValueType.i1);
            currentBasicBlock.appendInst(new IcmpInstruction(res, value1, value2, op));
            for (int i = 2; i < relExp.addExps.size(); i++) {
                value1 = res;
                value2 = visitAddExp(relExp.addExps.get(i));
                op = new Op(Op.Op2Type(relExp.unaryOps.get(i - 1).token));
                res = new User(VirtualRegister.getRegister(), ValueType.i1);
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
            User res = new User(VirtualRegister.getRegister(), ValueType.i1);
            currentBasicBlock.appendInst(new IcmpInstruction(res, value1, value2, op));
            for (int i = 2; i < eqExp.relExps.size(); i++) {
                value1 = res;
                value2 = visitRelExp(eqExp.relExps.get(i));
                op = new Op(Op.Op2Type(eqExp.unaryOps.get(i - 1).token));
                res = new User(VirtualRegister.getRegister(), ValueType.i1);
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
            User res = new User(VirtualRegister.getRegister(), ValueType.i1);
            currentBasicBlock.appendInst(new IcmpInstruction(res, value1, value2, op));
            for (int i = 2; i < lAndExp.eqExps.size(); i++) {
                value1 = res;
                value2 = visitEqExp(lAndExp.eqExps.get(i));
                op = new Op(Op.Op2Type(lAndExp.unaryOps.get(i - 1).token));
                res = new User(VirtualRegister.getRegister(), ValueType.i1);
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
            User res = new User(VirtualRegister.getRegister(), ValueType.i1);
            currentBasicBlock.appendInst(new IcmpInstruction(res, value1, value2, op));
            for (int i = 2; i < lOrExp.lAndExps.size(); i++) {
                value1 = res;
                value2 = visitLAndExp(lOrExp.lAndExps.get(i));
                op = new Op(Op.Op2Type(lOrExp.unaryOps.get(i - 1).token));
                res = new User(VirtualRegister.getRegister(), ValueType.i1);
                currentBasicBlock.appendInst(new IcmpInstruction(res, value1, value2, op));
            }
            return res;
        }
    }

    public Value visitCond(Cond cond) {
        Value value = visitLOrExp(cond.lOrExp);
        if (value.getType() == ValueType.i32) {
            User res = new User(VirtualRegister.getRegister(), ValueType.i1);
            currentBasicBlock.appendInst(new IcmpInstruction(res, value, new Constant("0"), new Op(Op.Type.ne)));
        }
        return value;
    }
}