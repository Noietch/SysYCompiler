package Backend;

import Backend.Mem.RealRegister;
import Backend.Mem.VirtualRegister;
import Middle.IRElement.Basic.*;
import Middle.IRElement.Basic.Module;
import Middle.IRElement.Instructions.*;
import Middle.IRElement.Type.ValueType;
import Middle.IRElement.Value;

import java.util.ArrayList;
import java.util.Collections;

public class CodeGen {
    public Module irModule;

    public Function curFunc;

    public IcmpInstruction curIcmp;
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
            } else genDefine(function);
        }
    }

    private void genDefine(Function function) {
        mipsCode.add(new MipsInstruction(function.name));
        if (function.name.equals("putch"))
            mipsCode.add(new MipsInstruction("addiu", "$v0", "$zero", "11"));
        if (function.name.equals("putint"))
            mipsCode.add(new MipsInstruction("addiu", "$v0", "$zero", "1"));
        if (function.name.equals("getint"))
            mipsCode.add(new MipsInstruction("addiu", "$v0", "$zero", "5"));
        mipsCode.add(new MipsInstruction("syscall", ""));
        mipsCode.add(new MipsInstruction("jr", "$ra"));
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
        regAllocator.getStackReg("$ra");
        // 参数的个数——size
        int size = functionParams.size();
        for (int i = 0; i < size && i < 4; i++)
            regAllocator.virtual2Temp.put(new VirtualRegister("%" + i), new RealRegister("$a" + i));
        for (int i = 4; i < size; i++)
            regAllocator.virtual2Stack.put(new VirtualRegister("%" + i), -(size - i));
    }

    private void genGlobalVariable(GlobalVariable globalVariable) {
        throw new RuntimeException(globalVariable.toString());
    }

    private void genBlock(BasicBlock block, boolean firstBlock) {
        if (!firstBlock)
            mipsCode.add(new MipsInstruction(curFunc.name + "_label_" + block.name));
        for (BaseInstruction instruction : block.instructions) {
            genInstruction(instruction);
        }
    }

    private void genAllocInstr(AllocateInstruction allocateInstruction) {
        String virtualNum = allocateInstruction.value1.getName();
        ValueType.Type type = allocateInstruction.value1.getInnerType();
        // 如果是数组，则计算空间后分配
        if (allocateInstruction.value1.getInnerType() instanceof ValueType.ArrayType) {
            ArrayList<Integer> dim = type.getDim();
            int memSize = 1;
            for (int i = 0; i < dim.size(); i++) memSize *= dim.get(0);
            for (int i = 0; i < memSize; i++) regAllocator.getStackReg(virtualNum, memSize);
            mipsCode.add(new MipsInstruction("addiu", "$sp", "$sp", "-" + 4 * memSize));
        } else {
            // 如果是整数,或者是一个地址，则直接分配空间
            this.regAllocator.getStackReg(virtualNum); // 分配栈空间
            mipsCode.add(new MipsInstruction("addiu", "$sp", "$sp", "-4"));
        }
    }

    private RealRegister lookup(Value virtual) {
        RealRegister tempReg;
        if (virtual instanceof Constant) {
            String constNum = virtual.getName();
            tempReg = regAllocator.getTempReg("const");
            mipsCode.add(new MipsInstruction("addiu", tempReg.toString(), "$zero", constNum));
        } else {
            tempReg = regAllocator.lookUpTemp(virtual.getName());
            if (tempReg == null) {
                String stackReg = regAllocator.lookUpStack(virtual.getName());
                tempReg = regAllocator.getTempReg(virtual.getName());
                mipsCode.add(new MipsInstruction(true, "lw", tempReg.toString(), "$sp", stackReg));
            }
        }
        return tempReg;
    }

    private void genStoreInstr(StoreInstruction storeInstruction) {
        RealRegister tempReg = lookup(storeInstruction.value1);
        String stackReg = regAllocator.lookUpStack(storeInstruction.value2.getName());
        mipsCode.add(new MipsInstruction(true, "sw", tempReg.toString(), "$sp", stackReg));
        regAllocator.freeTempReg(storeInstruction.value1.getName(), tempReg);
    }

    private void genLoadInstr(LoadInstruction loadInstruction) {
        String stackReg = regAllocator.lookUpStack(loadInstruction.value1.getName());
        RealRegister tempReg = regAllocator.getTempReg(loadInstruction.result.getName());
        mipsCode.add(new MipsInstruction(true, "lw", tempReg.toString(), "$sp", stackReg));
    }

    private void genReturn(RetInstruction retInstruction) {
        // 清空栈，然后还原栈指针
        if (!curFunc.name.equals("main")) {
            // 这里需要重新加载ra
            String stack = regAllocator.lookUpStack("$ra");
            mipsCode.add(new MipsInstruction(true, "lw", "$ra", "$sp", stack));
            // 把返回值赋值到v0中
            Value ret = retInstruction.value1;
            RealRegister reg = lookup(ret);
            if (ret != null) mipsCode.add(new MipsInstruction("add", "$v0", "$zero", reg.toString()));
            // 执行return
            // 返回栈指针
            mipsCode.add(new MipsInstruction("addiu", "$sp", "$sp", regAllocator.getCurStack()));
            mipsCode.add(new MipsInstruction("jr", "$ra"));
        } else {
            mipsCode.add(new MipsInstruction("addiu", "$v0", "$zero", "10"));
            mipsCode.add(new MipsInstruction("syscall", ""));
        }
        regAllocator.clearStack();
    }

    private void genBinaryInstr(BinaryInstruction binaryInstruction) {
        String Operator = "";
        if (binaryInstruction.op.type.equals(Op.Type.add)) Operator = "add";
        if (binaryInstruction.op.type.equals(Op.Type.sub)) Operator = "sub";
        if (binaryInstruction.op.type.equals(Op.Type.mul)) Operator = "mul";
        if (binaryInstruction.op.type.equals(Op.Type.sdiv)) Operator = "div";
        if (binaryInstruction.op.type.equals(Op.Type.srem)) Operator = "rem";
        // 做二元算式
        RealRegister tempReg1 = lookup(binaryInstruction.value1);
        RealRegister tempReg2 = lookup(binaryInstruction.value2);
        RealRegister tempRegRes = regAllocator.getTempReg(binaryInstruction.result.getName());
        // 如果是加减法
        if (Operator.equals("add") || Operator.equals("sub") || Operator.equals("mul"))
            mipsCode.add(new MipsInstruction(Operator, tempRegRes.toString(), tempReg1.toString(), tempReg2.toString()));
        // 如果是除法和模
        if (Operator.equals("div") || Operator.equals("rem")) {
            // 先做除法
            mipsCode.add(new MipsInstruction("div", tempReg1.toString(), tempReg2.toString()));
            // 如果是除法取低位
            if (Operator.equals("div"))
                mipsCode.add(new MipsInstruction("mflo", tempRegRes.toString()));
            // 如果是模取高位
            if (Operator.equals("rem"))
                mipsCode.add(new MipsInstruction("mfhi", tempRegRes.toString()));
        }
        // 将计算结果保存到$sp中 , 首先申请内存, 然后把东西存进去
        mipsCode.add(new MipsInstruction("addiu", "$sp", "$sp", "-4"));
        regAllocator.getStackReg(binaryInstruction.result.getName());
        mipsCode.add(new MipsInstruction(true, "sw", tempRegRes.toString(), "$sp", "0"));
        // 释放编译器
        regAllocator.freeTempReg(binaryInstruction.value1.getName(), tempReg1);
        regAllocator.freeTempReg(binaryInstruction.value2.getName(), tempReg2);
        regAllocator.freeTempReg(binaryInstruction.result.getName(), tempRegRes);
    }

    private void genCallInstr(CallInstruction callInstruction) {
        // 首先把函数的参数加载到对应的寄存器中
        int size = callInstruction.funcRParams.size();
        for (int i = 0; i < size && i < 4; i++) {
            Value param = callInstruction.funcRParams.get(i);
            RealRegister tempReg = lookup(param);
            mipsCode.add(new MipsInstruction("add", "$a" + i, "$zero", tempReg.toString()));
            regAllocator.freeTempReg(param.getName(), tempReg);
        }
        for (int i = 4; i < size; i++) {
            // 大于4个参数，需要申请空间
            Value param = callInstruction.funcRParams.get(i);
            String regName = param.getName();
            RealRegister tempReg = lookup(param);
            mipsCode.add(new MipsInstruction("addiu", "$sp", "$sp", "-4"));
            regAllocator.getStackReg();
            mipsCode.add(new MipsInstruction(true, "sw", tempReg.toString(), "$sp", "0"));
            regAllocator.freeTempReg(regName, tempReg);
        }
        // 跳转到函数名
        String funcName = callInstruction.value1.name;
        mipsCode.add(new MipsInstruction("jal", funcName));
        // 如果有返回值，需要先申请一个，把$v0寄存器给拿到对应的地方
        if (callInstruction.value2 != null) {
            String virtualNum = callInstruction.value2.getName();
            this.regAllocator.getStackReg(virtualNum); // 分配寄存器
            mipsCode.add(new MipsInstruction("addiu", "$sp", "$sp", "-4"));
            String stack = regAllocator.lookUpStack(callInstruction.value2.getName());
            mipsCode.add(new MipsInstruction(true, "sw", "$v0", "$sp", stack));
        }
    }

    // mips里的比较分支是一条语句, 我们需要把ir的两条语句合并成一条
    // ir里的这个比较和分支是连在一起的, 所以我们可以设个全局变量, 一起处理。
    private void genIcmpInstr(IcmpInstruction icmpInstruction) {
        curIcmp = icmpInstruction;
    }

    private void genBranchInstruction(BranchInstruction branchInstruction) {
        String Operator = "";
        if (curIcmp.op.type.equals(Op.Type.eq)) Operator = "beq";
        if (curIcmp.op.type.equals(Op.Type.ne)) Operator = "bne";
        if (curIcmp.op.type.equals(Op.Type.sge)) Operator = "bge";
        if (curIcmp.op.type.equals(Op.Type.sgt)) Operator = "bgt";
        if (curIcmp.op.type.equals(Op.Type.sle)) Operator = "ble";
        if (curIcmp.op.type.equals(Op.Type.slt)) Operator = "blt";
        RealRegister tempReg1 = lookup(curIcmp.value1);
        RealRegister tempReg2 = lookup(curIcmp.value2);
        String label_true = curFunc.name + "_label_" + branchInstruction.value1.name;
        if (branchInstruction.value2 != null) {
            String label_false = curFunc.name + "_label_" + branchInstruction.value2.name;
            mipsCode.add(new MipsInstruction(Operator, tempReg1.toString(), tempReg2.toString(), label_true));
            mipsCode.add(new MipsInstruction("j", label_false));
        } else mipsCode.add(new MipsInstruction("j", label_true));
    }

    // 对于数组的访问, 由于llvmir 的特殊结构, 我们需要直接进行一个,对于getElementPtr的单独处理
    private void genGetElementPtr(GetElementPtr getElementPtr) {

    }


    private void genInstruction(BaseInstruction instruction) {
        if (instruction instanceof AllocateInstruction) genAllocInstr((AllocateInstruction) instruction);
        if (instruction instanceof LoadInstruction) genLoadInstr((LoadInstruction) instruction);
        if (instruction instanceof StoreInstruction) genStoreInstr((StoreInstruction) instruction);
        if (instruction instanceof BinaryInstruction) genBinaryInstr((BinaryInstruction) instruction);
        if (instruction instanceof RetInstruction) genReturn((RetInstruction) instruction);
        if (instruction instanceof CallInstruction) genCallInstr((CallInstruction) instruction);
        if (instruction instanceof IcmpInstruction) genIcmpInstr((IcmpInstruction) instruction);
        if (instruction instanceof BranchInstruction) genBranchInstruction((BranchInstruction) instruction);
        if (instruction instanceof GetElementPtr) genGetElementPtr((GetElementPtr) instruction);
    }
}