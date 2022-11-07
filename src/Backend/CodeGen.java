package Backend;

import Middle.IRElement.Basic.*;
import Middle.IRElement.Basic.Module;
import Middle.IRElement.Instructions.*;
import Middle.IRElement.Value;

import java.util.ArrayList;
import java.util.Collections;

public class CodeGen {
    public Module irModule;

    public Function curFunc;
    public RegAllocator regAllocator;
    public ArrayList<MipsInstruction> mipsCode;

    public CodeGen(Module irModule) {
        this.mipsCode = new ArrayList<>();
        this.regAllocator = new RegAllocator();
        this.irModule = irModule;
    }

    public String genMips() {
        genModule();
        StringBuilder res = new StringBuilder();
        res.append(".globl main").append("\n");
        for (MipsInstruction mipsInstruction : mipsCode) {
            res.append(mipsInstruction).append("\n");
        }
        return res.toString();
    }

    private void genModule() {
        for (GlobalVariable globalVariable : irModule.globalVariables) {
            genGlobalVariable(globalVariable);
        }
        Collections.reverse(irModule.functions);
        for (Function function : irModule.functions) {
            if (function.define) {
                curFunc = function;
                genFunction(function);
            }
        }
    }

    private void genFunction(Function function) {
        mipsCode.add(new MipsInstruction(function.name));
        if (!curFunc.name.equals("main")) genFunctionParam(function.funcFParams);
        boolean firstFlag = true;
        for (BasicBlock basicBlock : function.basicBlocks) {
            genBlock(basicBlock, firstFlag);
            firstFlag = false;
        }
    }

    // 这个函数其实是需要调整符号表
    private void genFunctionParam(ArrayList<Value> functionParams) {
        // 首先是把$ra存起来
        // 分配内存
        mipsCode.add(new MipsInstruction("addiu", "$sp", "$sp", "-4"));
        mipsCode.add(new MipsInstruction(true, "sw", "$ra", "$sp", "0"));
        regAllocator.getStackReg();
        // 参数的个数——size
        int size = functionParams.size();
        if (size >= 1) regAllocator.virtual2Temp.put("0", "$a0");
        if (size >= 2) regAllocator.virtual2Temp.put("1", "$a1");
        if (size >= 3) regAllocator.virtual2Temp.put("2", "$a2");
        if (size >= 4) regAllocator.virtual2Temp.put("3", "$a3");
        for (int i = 4; i < size; i++)
            regAllocator.virtual2Stack.put(Integer.toString(i), -(size - i));
    }

    private void genGlobalVariable(GlobalVariable globalVariable) {
        throw new RuntimeException(globalVariable.toString());
    }

    private void genBlock(BasicBlock block, boolean firstBlock) {
        if (!firstBlock)
            mipsCode.add(new MipsInstruction(block.parent.name + "_label_" + block.name));
        for (BaseInstruction instruction : block.instructions) {
            genInstruction(instruction);
        }
    }

    private void genAllocInstr(AllocateInstruction allocateInstruction) {
        String virtualNum = allocateInstruction.value1.name;
        this.regAllocator.getStackReg(virtualNum); // 分配寄存器
        mipsCode.add(new MipsInstruction("addiu", "$sp", "$sp", "-4"));
    }

    private void genStoreInstr(StoreInstruction storeInstruction) {
        String tempReg;
        String virtualName = "null";
        if (storeInstruction.value1 instanceof Constant) {
            String constNum = storeInstruction.value1.name;
            tempReg = regAllocator.getTempReg("const");
            mipsCode.add(new MipsInstruction("addiu", tempReg, "$zero", constNum));
        } else {
            virtualName = storeInstruction.value1.name;
            tempReg = lookup(virtualName);
        }
        String stackReg = regAllocator.lookUpStack(storeInstruction.value2.name);
        mipsCode.add(new MipsInstruction(true, "sw", tempReg, "$sp", stackReg));
        regAllocator.freeTempReg(virtualName, tempReg);
    }

    private void genLoadInstr(LoadInstruction loadInstruction) {
        String stackReg = regAllocator.lookUpStack(loadInstruction.value1.name);
        String tempReg = regAllocator.getTempReg(loadInstruction.result.name);
        mipsCode.add(new MipsInstruction(true, "lw", tempReg, "$sp", stackReg));
    }

    private void genReturn(RetInstruction retInstruction) {
        // 清空栈，然后还原栈指针
        String num = regAllocator.clearStack();
        if (!curFunc.name.equals("main")) {
            mipsCode.add(new MipsInstruction("addiu", "$sp", "$sp", num));
            // TODO: 这里需要重新加载ra
            // 执行return
            mipsCode.add(new MipsInstruction("jr", "$ra"));
        }
    }

    private String lookup(String virtualNum) {
        String tempReg = regAllocator.lookUpTemp(virtualNum);
        if (tempReg == null) {
            String stackReg = regAllocator.lookUpStack(virtualNum);
            tempReg = regAllocator.getTempReg(virtualNum);
            mipsCode.add(new MipsInstruction(true, "lw", tempReg, "$sp", stackReg));
        }
        return tempReg;
    }

    private void genBinaryInstr(BinaryInstruction binaryInstruction) {
        String Operator = "";
        if (binaryInstruction.op.type.equals(Op.Type.add)) Operator = "add";
        if (binaryInstruction.op.type.equals(Op.Type.sub)) Operator = "sub";
        if (binaryInstruction.op.type.equals(Op.Type.mul)) Operator = "mul";
        if (binaryInstruction.op.type.equals(Op.Type.sdiv)) Operator = "div";
        if (binaryInstruction.op.type.equals(Op.Type.srem)) Operator = "rem";
        // 做二元算式
        String tempReg1 = lookup(binaryInstruction.value1.name);
        String tempReg2 = binaryInstruction.value2.name;
        if (!(binaryInstruction.value2 instanceof Constant))
            tempReg2 = lookup(binaryInstruction.value2.name);
        String tempRegRes = regAllocator.getTempReg(binaryInstruction.result.name);
        // 如果是加减法
        if (Operator.equals("add") || Operator.equals("sub") || Operator.equals("mul"))
            mipsCode.add(new MipsInstruction(Operator, tempRegRes, tempReg1, tempReg2));
        // 如果是除法和模
        if (Operator.equals("div") || Operator.equals("rem")) {
            // 先做除法
            mipsCode.add(new MipsInstruction("div", tempReg1, tempReg2));
            // 如果是除法取低位
            if (Operator.equals("div"))
                mipsCode.add(new MipsInstruction("mflo", tempRegRes));
            // 如果是模取高位
            if (Operator.equals("rem"))
                mipsCode.add(new MipsInstruction("mfhi", tempRegRes));
        }
        // 将计算结果保存到$sp中 , 首先申请内存, 然后把东西存进去
        mipsCode.add(new MipsInstruction("addiu", "$sp", "$sp", "-4"));
        regAllocator.getStackReg(binaryInstruction.result.name);
        mipsCode.add(new MipsInstruction(true, "sw", tempRegRes, "$sp", "0"));
        // 释放编译器
        regAllocator.freeTempReg(binaryInstruction.value1.name, tempReg1);
        regAllocator.freeTempReg(binaryInstruction.value2.name, tempReg2);
        regAllocator.freeTempReg(binaryInstruction.result.name, tempRegRes);
    }

    private void genInstruction(BaseInstruction instruction) {
        if (instruction instanceof AllocateInstruction) genAllocInstr((AllocateInstruction) instruction);
        if (instruction instanceof LoadInstruction) genLoadInstr((LoadInstruction) instruction);
        if (instruction instanceof StoreInstruction) genStoreInstr((StoreInstruction) instruction);
        if (instruction instanceof BinaryInstruction) genBinaryInstr((BinaryInstruction) instruction);
        if (instruction instanceof RetInstruction) genReturn((RetInstruction) instruction);
    }
}