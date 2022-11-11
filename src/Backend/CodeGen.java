package Backend;

import Backend.Mem.RealRegister;
import Backend.Mem.Stack;
import Backend.Mem.VirtualRegister;
import Middle.IRElement.Basic.*;
import Middle.IRElement.Basic.Module;
import Middle.IRElement.Instructions.*;
import Middle.IRElement.Type.ValueType;
import Middle.IRElement.Value;
import Utils.Pair;

import java.util.ArrayList;
import java.util.Collections;

public class CodeGen {
    public Module irModule;
    public Function curFunc;

    private boolean firstFlag; // 记录目前的是不是第一个块儿
    public int allocStage; // 目前初始化的哪个阶段，2就是二维数组，1就是一维数组
    public int curMemSize;  // 目前正在初始化数组的大小
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

        for (MipsInstruction mipsInstruction : mipsCode) {
            res.append(mipsInstruction).append("\n");
        }
        return res.toString();
    }

    private void genModule() {
        mipsCode.add(new MipsInstruction(".data"));
        for (GlobalVariable globalVariable : irModule.globalVariables) {
            genGlobalVariable(globalVariable);
        }
        mipsCode.add(new MipsInstruction(".text"));
        mipsCode.add(new MipsInstruction(".globl main"));
        Collections.reverse(irModule.functions);
        for (Function function : irModule.functions) {
            if (function.define) {
                curFunc = function;
                genFunction(function);
            } else genDefine(function);
        }
    }

    private void genDefine(Function function) {
        mipsCode.add(new MipsInstruction(function.name + ":"));
        if (function.name.equals("putch"))
            mipsCode.add(new MipsInstruction("addiu", "$v0", "$zero", "11"));
        if (function.name.equals("putint"))
            mipsCode.add(new MipsInstruction("addiu", "$v0", "$zero", "1"));
        if (function.name.equals("getint"))
            mipsCode.add(new MipsInstruction("addiu", "$v0", "$zero", "5"));
        mipsCode.add(new MipsInstruction("syscall", ""));
        mipsCode.add(new MipsInstruction("jr", "$ra"));
    }

    private void saveStackTop() {
        regAllocator.savePointer();
        // 先把$fp存起来,存到寄存器，然后把$fp覆盖, 申请空间之后, 再把$fp存到内存里
        RealRegister tempReg = regAllocator.getTempReg(regAllocator.getTempNum());
        mipsCode.add(new MipsInstruction("add", tempReg.toString(), "$fp", "$zero"));
        mipsCode.add(new MipsInstruction("add", "$fp", "$sp", "$zero"));
        mipsCode.add(new MipsInstruction("addiu", "$sp", "$sp", "-4"));
        Stack stack = regAllocator.getStackReg("$fp");
        mipsCode.add(new MipsInstruction(true, "sw", tempReg.toString(), "$fp", stack.toString()));
        // 释放寄存器
        regAllocator.freeTempReg(tempReg);
        // 把$ra存起来
        mipsCode.add(new MipsInstruction("addiu", "$sp", "$sp", "-4"));
        stack = regAllocator.getStackReg("$ra");
        mipsCode.add(new MipsInstruction(true, "sw", "$ra", "$fp", stack.toString()));
    }

    private void fixStackTop() {
        // 把$sp还原
        mipsCode.add(new MipsInstruction("add", "$sp", "$fp", "$zero"));
        // 把$ra还原
        Stack stack = regAllocator.lookUpStack("$ra");
        mipsCode.add(new MipsInstruction(true, "lw", "$ra", "$fp", stack.toString()));
        // 把$fp还原
        stack = regAllocator.lookUpStack("$fp");
        mipsCode.add(new MipsInstruction(true, "lw", "$fp", "$fp", stack.toString()));
        regAllocator.resolvePointer();
    }

    private void genFunction(Function function) {
        mipsCode.add(new MipsInstruction(function.name + ":"));
        if (!curFunc.isMain()) genFunctionParam(function.funcFParams);
        firstFlag = true;
        for (BasicBlock basicBlock : function.basicBlocks) {
            genBlock(basicBlock);
            firstFlag = false;
        }
        regAllocator.clear();
    }

    // 这个函数其实是需要调整符号表
    private void genFunctionParam(ArrayList<Value> functionParams) {
        // 参数的个数——size
        int size = functionParams.size();
        // 4号寄存器是a0
        for (int i = 0; i < size && i < 4; i++)
            regAllocator.temRegUseMap[i + 4] = new VirtualRegister("%" + i);
        for (int i = 4; i < size; i++)
            regAllocator.virtual2Stack.put(new VirtualRegister("%" + i), new Stack(-(size - i)));
    }


    private void genGlobalVariable(GlobalVariable globalVariable) {
        // 如果没有初始化, 那么就直接赋0值
        int size = 4;
        if (globalVariable.init == null) {
            if (globalVariable.type.getType() == ValueType.i32)
                mipsCode.add(new MipsInstruction(globalVariable.name, ":    .word 0"));
            else {
                Integer memSize = 1;
                ArrayList<Integer> dim = globalVariable.getInnerType().getDim();
                for (Integer integer : dim) memSize *= integer;
                size = memSize * 4;
                mipsCode.add(new MipsInstruction(globalVariable.name, ":    .space " + memSize * 4));
            }
        } else {
            if (globalVariable.type.getType() == ValueType.i32)
                mipsCode.add(new MipsInstruction(globalVariable.name, ":    .word " + globalVariable.init.get(0)));
            else {
                ValueType.ArrayType arrayType = (ValueType.ArrayType) globalVariable.type.getType();
                ArrayList<Object> initials = globalVariable.flattenInitials(arrayType, globalVariable.init);
                Collections.reverse(initials);
                size = initials.size() * 4;
                String init = initials.stream().map(Object::toString).reduce((x, y) -> x + ", " + y).orElse("");
                mipsCode.add(new MipsInstruction(globalVariable.name, ":    .word " + init));
            }
        }
        // 添加符号表
        regAllocator.virtual2Global.put(globalVariable.name, Integer.toString(size - 4));
    }

    private void genBlock(BasicBlock block) {
        if (!firstFlag)
            mipsCode.add(new MipsInstruction(curFunc.name + "_label_" + block.name + ":"));
        saveStackTop();
        for (BaseInstruction instruction : block.instructions) {
            genInstruction(instruction);
        }
    }




    private int genAllocInstr(AllocateInstruction allocateInstruction) {
        String virtualNum = allocateInstruction.value1.getName();
        ValueType.Type type = allocateInstruction.value1.getInnerType();
        int size = 4;
        // 如果是数组，则计算空间后分配
        if (allocateInstruction.value1.getInnerType() instanceof ValueType.ArrayType) {
            ArrayList<Integer> dim = type.getDim();
            // 后面的初始化交给GetElem 二维数组后面就需要两个这个GetElem, 一维数组后面就需要一个这个GetElem
            allocStage = dim.size();
            // 算存储大小
            curMemSize = 1;
            for (Integer integer : dim) curMemSize *= integer;
            size = 4 * curMemSize;
            regAllocator.getStackReg(allocateInstruction.value1.getName(), curMemSize);
        } else this.regAllocator.getStackReg(virtualNum); // 如果是整数或者是一个地址，则直接分配空间
        return size;
    }

    private RealRegister lookup(Value virtual) {
        RealRegister tempReg;
        if (virtual instanceof Constant) {
            String constNum = virtual.getName();
            tempReg = regAllocator.getTempReg(regAllocator.getTempNum());
            mipsCode.add(new MipsInstruction("addiu", tempReg.toString(), "$zero", constNum));
        } else {
            tempReg = regAllocator.lookUpTemp(virtual.getName());
            if (tempReg == null) {
                Stack stackReg = regAllocator.lookUpStack(virtual.getName());
                tempReg = regAllocator.getTempReg(virtual.getName());
                mipsCode.add(new MipsInstruction(true, "lw", tempReg.toString(), "$fp", stackReg.toString()));
            }
        }
        return tempReg;
    }

    private void genStoreInstr(StoreInstruction storeInstruction) {
        RealRegister tempReg = lookup(storeInstruction.value1);
        RealRegister realRegister = regAllocator.lookUpTemp(storeInstruction.value2.getName());
        if (realRegister != null) {
            mipsCode.add(new MipsInstruction(true, "sw", tempReg.toString(), realRegister.toString(), "0"));
            regAllocator.freeTempReg(realRegister);
        } else {
            Stack stackReg = regAllocator.lookUpStack(storeInstruction.value2.getName());
            if (stackReg != null)
                mipsCode.add(new MipsInstruction(true, "sw", tempReg.toString(), "$fp", stackReg.toString()));
            else if (regAllocator.lookUpGlobal(storeInstruction.value2.name)) {
                mipsCode.add(new MipsInstruction("sw", tempReg.toString(), storeInstruction.value2.name));
            }
        }
        regAllocator.freeTempReg(tempReg);
    }

    private void genLoadInstr(LoadInstruction loadInstruction) {
        RealRegister tempReg = regAllocator.getTempReg(loadInstruction.result.getName());
        RealRegister realRegister = regAllocator.lookUpTemp(loadInstruction.value1.getName());
        if (realRegister != null) {
            mipsCode.add(new MipsInstruction(true, "lw", tempReg.toString(), realRegister.toString(), "0"));
            // 每次访问数组都是重新从内存里拿，所以这个寄存器是可以free掉的
            regAllocator.freeTempReg(realRegister);
        } else {
            Stack stackReg = regAllocator.lookUpStack(loadInstruction.value1.getName());
            if (stackReg != null)
                mipsCode.add(new MipsInstruction(true, "lw", tempReg.toString(), "$fp", stackReg.toString()));
            else if (regAllocator.lookUpGlobal(loadInstruction.value1.name)) {
                mipsCode.add(new MipsInstruction("lw", tempReg.toString(), loadInstruction.value1.name));
            }
        }
    }

    private void genReturn(RetInstruction retInstruction) {
        // 清空栈，然后还原栈指针
        if (!curFunc.name.equals("main")) {
            // 把ra和fp还原
            fixStackTop();
            // 把返回值赋值到v0中
            Value ret = retInstruction.value1;
            if (ret != null) {
                RealRegister reg = lookup(ret);
                mipsCode.add(new MipsInstruction("add", "$v0", "$zero", reg.toString()));
                regAllocator.freeTempReg(reg);
            }
            // 返回栈指针
            mipsCode.add(new MipsInstruction("jr", "$ra"));
        } else {
            mipsCode.add(new MipsInstruction("addiu", "$v0", "$zero", "10"));
            mipsCode.add(new MipsInstruction("syscall", ""));
        }
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
        Stack stack = regAllocator.getStackReg(binaryInstruction.result.getName());
        mipsCode.add(new MipsInstruction(true, "sw", tempRegRes.toString(), "$fp", stack.toString()));
        // 释放编译器
        regAllocator.freeTempReg(tempReg1);
        regAllocator.freeTempReg(tempReg2);
        regAllocator.freeTempReg(tempRegRes);
    }

    private void genCallInstr(CallInstruction callInstruction) {
        // 首先把函数的参数加载到对应的寄存器中
        int size = callInstruction.funcRParams.size();
        for (int i = 0; i < size && i < 4; i++) {
            Value param = callInstruction.funcRParams.get(i);
            RealRegister tempReg = lookup(param);
            mipsCode.add(new MipsInstruction("add", "$a" + i, "$zero", tempReg.toString()));
            regAllocator.freeTempReg(tempReg);
        }
        for (int i = 4; i < size; i++) {
            // 大于4个参数，需要申请空间
            Value param = callInstruction.funcRParams.get(i);
            RealRegister tempReg = lookup(param);
            mipsCode.add(new MipsInstruction("addiu", "$sp", "$sp", "-4"));
            regAllocator.getStackReg();
            mipsCode.add(new MipsInstruction(true, "sw", tempReg.toString(), "$sp", "0"));
            regAllocator.freeTempReg(tempReg);
        }
        // 保存现场, TODO 寄存器分配
        for (int i = 8; i < 28; i++) {
            if (regAllocator.temRegUseMap[i] != VirtualRegister.None) {
                mipsCode.add(new MipsInstruction("addiu", "$sp", "$sp", "-4"));
                Stack stack = regAllocator.getStackReg(regAllocator.temp2Virtual(i).toString());
                RealRegister tempReg = regAllocator.tempRegPool.get(i);
                mipsCode.add(new MipsInstruction(true, "sw", tempReg.toString(), "$fp", stack.toString()));
                regAllocator.freeTempReg(tempReg);
                regAllocator.record(tempReg, stack);
            }
        }
        // 跳转到函数名
        String funcName = callInstruction.value1.name;
        mipsCode.add(new MipsInstruction("jal", funcName));
        // 恢复现场
        for (Pair pair : regAllocator.Recorder)
            mipsCode.add(new MipsInstruction(true, "lw", pair.getFirst().toString(), "$fp", pair.getSecond().toString()));
        regAllocator.recordClear();
        // 如果有返回值，需要先申请一个，把$v0寄存器给拿到对应的地方
        if (callInstruction.value2 != null) {
            String virtualNum = callInstruction.value2.getName();
            this.regAllocator.getStackReg(virtualNum); // 分配寄存器
            mipsCode.add(new MipsInstruction("addiu", "$sp", "$sp", "-4"));
            Stack stack = regAllocator.lookUpStack(callInstruction.value2.getName());
            mipsCode.add(new MipsInstruction(true, "sw", "$v0", "$fp", stack.toString()));
        }
    }

    // mips里的比较分支是一条语句, 我们需要把ir的两条语句合并成一条
    // ir里的这个比较和分支是连在一起的, 所以我们可以设个全局变量, 一起处理。
    private void genIcmpInstr(IcmpInstruction icmpInstruction) {
        RealRegister tempReg1 = lookup(icmpInstruction.value1);
        RealRegister tempReg2 = lookup(icmpInstruction.value2);
        RealRegister tempRegRes = regAllocator.getTempReg(icmpInstruction.result.getName());
        String Operator = "";
        if (icmpInstruction.op.type.equals(Op.Type.eq)) Operator = "seq";
        else if (icmpInstruction.op.type.equals(Op.Type.ne)) Operator = "sne";
        else if (icmpInstruction.op.type.equals(Op.Type.sge)) Operator = "sge";
        else if (icmpInstruction.op.type.equals(Op.Type.sgt)) Operator = "sgt";
        else if (icmpInstruction.op.type.equals(Op.Type.sle)) Operator = "sle";
        else if (icmpInstruction.op.type.equals(Op.Type.slt)) Operator = "slt";
        mipsCode.add(new MipsInstruction(Operator, tempRegRes.toString(), tempReg1.toString(), tempReg2.toString()));
        regAllocator.freeTempReg(tempReg1);
        regAllocator.freeTempReg(tempReg2);
    }

    private void genBranchInstruction(BranchInstruction branchInstruction) {
        fixStackTop();
        if (branchInstruction.value2 != null) {
            RealRegister tempReg = regAllocator.lookUpTemp(branchInstruction.cond.getName());
            String label_true = curFunc.name + "_label_" + branchInstruction.value1.name;
            String label_false = curFunc.name + "_label_" + branchInstruction.value2.name;
            mipsCode.add(new MipsInstruction("bne", tempReg.toString(), "$zero", label_true));
            mipsCode.add(new MipsInstruction("j", label_false));
            regAllocator.freeTempReg(tempReg);
        } else {
            String label_true = curFunc.name + "_label_" + branchInstruction.value1.name;
            mipsCode.add(new MipsInstruction("j", label_true));
        }
    }

    private void genZext(ZextInstruction zextInstruction) {
        RealRegister tempReg = regAllocator.lookUpTemp(zextInstruction.value1.getName());
        regAllocator.temRegUseMap[tempReg.getNum()] = new VirtualRegister(zextInstruction.res.getName());
    }

    // 对于数组的访问, 由于llvmir 的特殊结构, 我们需要直接进行一个,对于getElementPtr的单独处理
    // 伏笔一：在llvm中我每次初始化数组之后会直接给他分配一个指针，这个指针是首地址，后面我们需要用的时候用的是这个指针
    // 这导致我们在alloc阶段的符号表是有问题的，我们需要记录一个地址两次
    private void genGetElementPtr(GetElementPtr getElementPtr) {
        // TODO 这里需要改
        if (getElementPtr.mipsHelper.isInit()) {
            if (allocStage > 1) allocStage--;
            else if (allocStage == 1)
                regAllocator.repeatReg(getElementPtr.value1.getName(), curMemSize);
        }
        // 这就说明用的是首地址，那么就是初始化里的东西,这里需要的就是修改字典
        else if (getElementPtr.bound2 == null) {
            RealRegister temReg = regAllocator.lookUpTemp(getElementPtr.value2.getName());
            if (temReg == null) {
                // 如果数组不是参数，而且这种情况只会出现在初始化
                int offset = Integer.parseInt(getElementPtr.bound1.name);
                Stack stack = regAllocator.lookUpStack(getElementPtr.value2.getName());
                regAllocator.virtual2Stack.put(new VirtualRegister(getElementPtr.value1.getName()), stack.offset(offset));
            } else {
                // 如果数组是个参数，那么就直接是存在寄存器里的，但是这里需要把数组地址算出来
                // 如果是一维数组,直接偏移，二维数组还需要偏移x列数
                RealRegister arrOffset = lookup(getElementPtr.bound1);
                RealRegister array = lookup(getElementPtr.value2);
                ValueType.Type type = getElementPtr.value2.getInnerType();
                if (type != ValueType.i32) {
                    String col = Integer.toString(((ValueType.ArrayType) type).size());
                    mipsCode.add(new MipsInstruction("mul", arrOffset.toString(), arrOffset.toString(), col));
                }
                mipsCode.add(new MipsInstruction("mul", arrOffset.toString(), arrOffset.toString(), "4"));
                mipsCode.add(new MipsInstruction("sub", array.toString(), array.toString(), arrOffset.toString()));
                regAllocator.freeTempReg(arrOffset);
                regAllocator.temRegUseMap[array.getNum()] = new VirtualRegister(getElementPtr.value1.getName());
            }
        } else {
            // 一般使用数组的方法，就是使用一开始申请的那个虚拟寄存器
            ValueType.Type type = getElementPtr.value2.getInnerType();
            // 如果是已经降维到了一维数组那么就直接算offset
            if (type.getType() == ValueType.i32) {
                // 首先直接把数组的地址加载进来
                String array = getElementPtr.value2.getName();
                Stack stack = regAllocator.lookUpStack(array);
                RealRegister curOffset = lookup(getElementPtr.bound2);
                mipsCode.add(new MipsInstruction("mul", curOffset.toString(), curOffset.toString(), "4"));
                // 如果一开始数组是存在内存里的，那就是一维数组
                if (stack != null) {
                    RealRegister tempReg = regAllocator.getTempReg(regAllocator.getTempNum());
                    mipsCode.add(new MipsInstruction("add", tempReg.toString(), "$fp", stack.toString()));
                    mipsCode.add(new MipsInstruction("sub", curOffset.toString(), tempReg.toString(), curOffset.toString()));
                    regAllocator.freeTempReg(tempReg);
                } else if (regAllocator.lookUpGlobal(getElementPtr.value2.name)) {
                    String size = regAllocator.virtual2Global.get(getElementPtr.value2.name);
                    RealRegister tempReg = regAllocator.getTempReg(regAllocator.getTempNum());
                    mipsCode.add(new MipsInstruction("la", tempReg.toString(), getElementPtr.value2.name));
                    mipsCode.add(new MipsInstruction("add", tempReg.toString(), tempReg.toString(), size));
                    mipsCode.add(new MipsInstruction("sub", curOffset.toString(), tempReg.toString(), curOffset.toString()));
                    regAllocator.freeTempReg(tempReg);
                } else {
                    RealRegister realRegister = regAllocator.lookUpTemp(array);
                    mipsCode.add(new MipsInstruction("sub", curOffset.toString(), realRegister.toString(), curOffset.toString()));
                    regAllocator.freeTempReg(realRegister);
                }
                regAllocator.temRegUseMap[curOffset.getNum()] = new VirtualRegister(getElementPtr.value1.getName());
            } else {
                // 首先计算偏移量
                RealRegister curOffset = lookup(getElementPtr.bound2);
                ValueType.ArrayType arrayType = (ValueType.ArrayType) type;
                int secDim = arrayType.getDim().get(1);
                mipsCode.add(new MipsInstruction("mul", curOffset.toString(), curOffset.toString(), Integer.toString(secDim)));
                //地址对齐
                mipsCode.add(new MipsInstruction("mul", curOffset.toString(), curOffset.toString(), "4"));
                // 首先找到目前的数组的地址
                RealRegister tempReg = regAllocator.getTempReg(regAllocator.getTempNum());
                if (regAllocator.lookUpGlobal(getElementPtr.value2.name)) {
                    String size = regAllocator.virtual2Global.get(getElementPtr.value2.name);
                    mipsCode.add(new MipsInstruction("la", tempReg.toString(), getElementPtr.value2.name));
                    mipsCode.add(new MipsInstruction("add", tempReg.toString(), tempReg.toString(), size));
                    mipsCode.add(new MipsInstruction("sub", curOffset.toString(), tempReg.toString(), curOffset.toString()));
                } else {
                    Stack stack = regAllocator.lookUpStack(getElementPtr.value2.getName());
                    mipsCode.add(new MipsInstruction("add", tempReg.toString(), "$fp", stack.toString()));
                    mipsCode.add(new MipsInstruction("sub", curOffset.toString(), tempReg.toString(), curOffset.toString()));
                }
                regAllocator.temRegUseMap[curOffset.getNum()] = new VirtualRegister(getElementPtr.value1.getName());
                // 把寄存器free掉
                regAllocator.freeTempReg(tempReg);
            }
        }
    }

    private void genInstruction(BaseInstruction instruction) {
        if (instruction instanceof AllocateInstruction) genAllocInstr((AllocateInstruction) instruction);
        else if (instruction instanceof LoadInstruction) genLoadInstr((LoadInstruction) instruction);
        else if (instruction instanceof StoreInstruction) genStoreInstr((StoreInstruction) instruction);
        else if (instruction instanceof BinaryInstruction) genBinaryInstr((BinaryInstruction) instruction);
        else if (instruction instanceof RetInstruction) genReturn((RetInstruction) instruction);
        else if (instruction instanceof CallInstruction) genCallInstr((CallInstruction) instruction);
        else if (instruction instanceof IcmpInstruction) genIcmpInstr((IcmpInstruction) instruction);
        else if (instruction instanceof BranchInstruction) genBranchInstruction((BranchInstruction) instruction);
        else if (instruction instanceof GetElementPtr) genGetElementPtr((GetElementPtr) instruction);
        else if (instruction instanceof ZextInstruction) genZext((ZextInstruction) instruction);
    }
}