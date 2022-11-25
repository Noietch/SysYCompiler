package Backend.OptimizeCodeGen;

import Backend.Mem.RealRegister;
import Backend.Mem.Stack;
import Backend.Mem.VirtualRegister;
import Backend.MipsInstruction;
import Middle.IRElement.Basic.*;
import Middle.IRElement.Instructions.*;
import Middle.IRElement.Type.ValueType;
import Middle.IRElement.Value;
import Utils.Triple;

import java.util.ArrayList;
import java.util.Collections;

public class OptimizeCodeGen {
    public Module irModule;
    public Function curFunc;

    public BasicBlock currentBB;
    public OpMemManager memManager;
    public ArrayList<MipsInstruction> mipsCode;
    public int maxParamStack;
    final public boolean isComment = true;

    public OptimizeCodeGen(Module irModule) {
        this.mipsCode = new ArrayList<>();
        this.memManager = new OpMemManager();
        this.irModule = irModule;
        this.maxParamStack = findMaxParam();
    }

    // 获取函数掉用的参数最大个数，用于分配栈空间
    public int findMaxParam() {
        int maxParam = 0;
        for (Function func : irModule.functions) {
            int paramNum = func.funcFParams.size();
            if (paramNum > maxParam) {
                maxParam = paramNum;
            }
        }
        // 如果最大参数个数大于4，需要分配栈空间，不然就是0
        return maxParam > 4 ? maxParam - 4 : 0;
    }

    // 全局寄存器分配
    // 临时寄存器给6个, 全局寄存器给
    public void regAllocate(ArrayList<String> ref) {
        memManager.setupGlobalReg(ref);
    }

    /*
     * preProcess 计算需要使用的内存 , 并且处理一下调用函数时的load语句不释放寄存器的情况
     *  * 对于alloc指令，分两种情况，第一种是整数或者是地址也就是函数参数中的数组地址 --> 分配四个
     *                            第二种是数组 --> 分配数组总长度乘以四
     *  * 对于call指令，就是每个函数预留所用寄存器的位置的内存，保存现场
     *
     * */
    public int preProcess(Function function) {
        // 保存 $ra 和 $sp
        int memSize = (1 + 1) * 4;
        // 保存现场，所有寄存器
        memSize += 4 * (memManager.GLOBAL_REG_UPPERBOUND - memManager.TEMP_REG_LOWER_BOUND);
        // 对于每一个函数调用，都添加最大的函数调用栈
        memSize += maxParamStack * 4;
        for (BasicBlock basicBlock : function.basicBlocks) {
            for (BaseInstruction instruction : basicBlock.instructions) {
                if (instruction instanceof AllocateInstruction) {
                    AllocateInstruction alloc = (AllocateInstruction) instruction;
                    ValueType.Type type = alloc.result.getInnerType();
                    if (type instanceof ValueType.ArrayType) memSize += ((ValueType.ArrayType) type).getTotalSize() * 4;
                    else memSize += 4;
                } // 如果是二元表达式，如果临时寄存器不够用了，则需要存一部分
                else if (instruction instanceof BinaryInstruction) memSize += 4;
                else if (instruction instanceof GetElementPtr) {
                    // 每一个数组加载出来的地址需要存到相应的地方
                    memSize += 4;
                    // 如果是数组加载出来的结果就需要添加到数组的集合中
                    GetElementPtr getElementPtr = (GetElementPtr) instruction;
                    memManager.addToArrayVirtualReg(getElementPtr.result.getName());
                } else if (instruction instanceof CallInstruction) {
                    // 如果是callInstruction有返回值也需要加内存
                    if (instruction.result != null) memSize += 4;
                    CallInstruction callInstruction = (CallInstruction) instruction;
                    // 加载参数 ,对于函数加载参数之前的load语句不释放寄存器的情况需要解决
                    int size = callInstruction.funcRParams.size();
                    // 如果是参数，加载出来的结果就需要放到参数的集合中
                    ArrayList<Value> params = callInstruction.funcRParams;
                    for (int i = 0; i < size; i++) {
                        memManager.addToParam(params.get(i).getName());
                    }
                }
            }
        }
        return memSize;
    }

    public String genMips() {
        genModule();
        StringBuilder res = new StringBuilder();
        for (MipsInstruction mipsInstruction : mipsCode)
            res.append(mipsInstruction).append("\n");
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
            }
        }
    }

    private void saveStackTop(int memSize) {
        // 先把$sp存起来,存到寄存器，然后把$sp覆盖, 申请空间之后, 再把$sp存到内存里
        mipsCode.add(new MipsInstruction("addu", "$fp", "$sp", "$zero"));
        mipsCode.add(new MipsInstruction("addiu", "$sp", "$sp", "-" + memSize));
        // 把原来的$sp存起来
        Stack stack = memManager.getStackReg("$sp");
        mipsCode.add(new MipsInstruction(true, "sw", "$fp", "$sp", stack.toString()));
        // 把$ra存起来
        stack = memManager.getStackReg("$ra");
        mipsCode.add(new MipsInstruction(true, "sw", "$ra", "$sp", stack.toString()));
    }

    private void fixStackTop() {
        // 把$ra还原
        Stack stack = memManager.lookUpStack("$ra");
        mipsCode.add(new MipsInstruction(true, "lw", "$ra", "$sp", stack.toString()));
        // 把$sp还原
        stack = memManager.lookUpStack("$sp");
        mipsCode.add(new MipsInstruction(true, "lw", "$sp", "$sp", stack.toString()));
    }

    private void genFunction(Function function) {
        mipsCode.add(new MipsInstruction(function.name + ":"));
        // 引用计数
        RefCounter refCounter = new RefCounter(function);
        ArrayList<String> ref = refCounter.getSortedRef();
        // regAllocate
        regAllocate(ref);
        // 加载函数所需内存
        int memSize = preProcess(function);
        // 把函数调用栈的字典建立起来
        for (int i = 0; i < maxParamStack; i++) {
            memManager.getStackReg("param" + (4 + i));
        }
        // 把寄存器的保存现场用的字典建起来
        for (int i = memManager.TEMP_REG_LOWER_BOUND; i < memManager.GLOBAL_REG_UPPERBOUND; i++) {
            memManager.getStackReg(memManager.tempRegPool.get(i).toString());
        }
        // 保存栈顶及 $ra
        saveStackTop(memSize);
        // 加载函数参数
        genFunctionParam(function.funcFParams, memSize);
        // 产生所有的块儿的内存
        boolean isFirst = true;
        for (BasicBlock basicBlock : function.basicBlocks) {
            genBlock(basicBlock, isFirst);
            isFirst = false;
        }
        memManager.clear();
    }

    // 这个函数其实是需要调整符号表
    private void genFunctionParam(ArrayList<Value> functionParams, int memSize) {
        // 参数的个数——size
        int size = functionParams.size();
        // 4号寄存器是a0
        for (int i = 0; i < size && i < 4; i++)
            memManager.temRegUseMap[i + 4] = new VirtualRegister("%" + i);
        for (int i = 4; i < size; i++)
            memManager.virtual2Stack.put(new VirtualRegister("%" + i), new Stack(memSize / 4 + i - 4));
    }


    private void genGlobalVariable(GlobalVariable globalVariable) {
        // 如果没有初始化, 那么就直接赋0值
        if (globalVariable.init == null) {
            if (globalVariable.type.getType() == ValueType.i32)
                mipsCode.add(new MipsInstruction(globalVariable.name, ":    .word 0"));
            else {
                Integer memSize = 1;
                ArrayList<Integer> dim = globalVariable.getInnerType().getDim();
                for (Integer integer : dim) memSize *= integer;
                mipsCode.add(new MipsInstruction(globalVariable.name, ":    .space " + memSize * 4));
            }
        } else {
            if (globalVariable.type.getType() == ValueType.i32)
                mipsCode.add(new MipsInstruction(globalVariable.name, ":    .word " + globalVariable.init.get(0)));
            else {
                ValueType.ArrayType arrayType = (ValueType.ArrayType) globalVariable.type.getType();
                ArrayList<Object> initials = globalVariable.flattenInitials(arrayType, globalVariable.init);
                String init = initials.stream().map(Object::toString).reduce((x, y) -> x + ", " + y).orElse("");
                mipsCode.add(new MipsInstruction(globalVariable.name, ":    .word " + init));
            }
        }
        // 添加符号表
        memManager.globalSet.add(globalVariable.getName());
    }

    private void genBlock(BasicBlock block, boolean isFirst) {
        currentBB = block;
        if (!isFirst) mipsCode.add(new MipsInstruction(curFunc.name + "_label_" + block.name + ":"));
        for (BaseInstruction instruction : block.instructions) {
            genInstruction(instruction);
        }
    }

    private void genAllocInstr(AllocateInstruction allocateInstruction) {
        String virtualNum = allocateInstruction.result.getName();
        ValueType.Type type = allocateInstruction.result.getInnerType();
        if (memManager.lookUpTemp(virtualNum) != null) {
            // 如果已经分配了寄存器, 那么就直接返回
            return;
        }
        // 如果是数组，则计算空间后分配
        if (type instanceof ValueType.ArrayType) {
            int curMemSize = ((ValueType.ArrayType) type).getTotalSize();
            memManager.getStackReg(allocateInstruction.result.getName(), curMemSize);
        } else this.memManager.getStackReg(virtualNum); // 如果是整数或者是一个地址，则直接分配空间
    }

    private RealRegister lookup(Value virtual) {
        RealRegister tempReg;
        if (virtual instanceof Constant) {
            String constNum = virtual.getName();
            tempReg = memManager.getTempReg(memManager.getTempNum());
            mipsCode.add(new MipsInstruction("addiu", tempReg.toString(), "$zero", constNum));
        } else {
            tempReg = memManager.lookUpTemp(virtual.getName());
            if (tempReg == null) {
                tempReg = memManager.virtual2Reg.get(virtual.getName());
                if (tempReg == null) {
                    Stack stackReg = memManager.lookUpStack(virtual.getName());
                    tempReg = memManager.getTempReg(virtual.getName());
                    mipsCode.add(new MipsInstruction(true, "lw", tempReg.toString(), "$sp", stackReg.toString()));
                }
            }
        }
        return tempReg;
    }

    private void genStoreInstr(StoreInstruction storeInstruction) {
        RealRegister tempReg = lookup(storeInstruction.value1);
        RealRegister target = memManager.lookUpTemp(storeInstruction.value2.getName());
        if (target != null) {
            mipsCode.add(new MipsInstruction("move", target.toString(), tempReg.toString()));
            memManager.freeTempReg(tempReg);
            return;
        }
        Stack stackReg = memManager.lookUpStack(storeInstruction.value2.getName());
        if (stackReg != null) {
            // 如果是数组就需要先load出地址
            if (memManager.isArrayVirtualReg(storeInstruction.value2.getName())) {
                // 申请临时寄存器把数组地址load出来
                RealRegister arrayAddr = memManager.getTempReg(memManager.getTempNum());
                mipsCode.add(new MipsInstruction(true, "lw", arrayAddr.toString(), "$sp", stackReg.toString()));
                mipsCode.add(new MipsInstruction(true, "sw", tempReg.toString(), arrayAddr.toString(), "0"));
                // 释放零时寄存器
                memManager.freeTempReg(arrayAddr);
            } else mipsCode.add(new MipsInstruction(true, "sw", tempReg.toString(), "$sp", stackReg.toString()));
        } else if (memManager.isGlobal(storeInstruction.value2.getName())) {
            mipsCode.add(new MipsInstruction("sw", tempReg.toString(), storeInstruction.value2.name));
        }
        memManager.freeTempReg(tempReg);
    }

    private void genLoadInstr(LoadInstruction loadInstruction) {
        // 如果是全局寄存器，就直接做个对应
        RealRegister target = memManager.lookUpTemp(loadInstruction.value1.getName());
        if (target != null) {
            memManager.virtual2Reg.put(loadInstruction.result.getName(), target);
            return;
        }

        if (memManager.isParam(loadInstruction.result.getName())) {
            // 如果是参数，就直接做个对应，不做翻译工作，但是需要注意的是数组对应的是地址的地址
            // 如果是栈上
            Stack stack = memManager.lookUpStack(loadInstruction.value1.getName());
            memManager.virtual2Stack.put(new VirtualRegister(loadInstruction.result.getName()), stack);
            // 如果是数组也把他加进数组的hashset里
            if (memManager.isArrayVirtualReg(loadInstruction.value1.getName())) {
                memManager.addToArrayVirtualReg(loadInstruction.result.getName());
            }
            // 如果是全局变量则添加字典
            if (memManager.isGlobal(loadInstruction.value1.getName()))
                memManager.addToGlobal(loadInstruction.result.getName(), loadInstruction.value1.getName());
        } else {
            // 对于不同的类别需要不同的寄存器
            RealRegister resultReg = memManager.getTempReg(loadInstruction.result.getName());
            Stack stackReg = memManager.lookUpStack(loadInstruction.value1.getName());
            // 这是一般的情况, 从栈上加载数据到寄存器里
            if (stackReg != null)
                // 如果是数组就需要先load出地址
                if (memManager.isArrayVirtualReg(loadInstruction.value1.getName())) {
                    // 申请临时寄存器把数组地址load出来
                    RealRegister arrayAddr = memManager.getTempReg(memManager.getTempNum());
                    mipsCode.add(new MipsInstruction(true, "lw", arrayAddr.toString(), "$sp", stackReg.toString()));
                    mipsCode.add(new MipsInstruction(true, "lw", resultReg.toString(), arrayAddr.toString(), "0"));
                    // 释放零时寄存器
                    memManager.freeTempReg(arrayAddr);
                } else mipsCode.add(new MipsInstruction(true, "lw", resultReg.toString(), "$sp", stackReg.toString()));
            else if (memManager.isGlobal(loadInstruction.value1.getName())) {
                // 如果还是找不到, 寻找全局变量
                mipsCode.add(new MipsInstruction("lw", resultReg.toString(), loadInstruction.value1.name));
            }
        }
    }

    private void genReturn(RetInstruction retInstruction) {
        // 清空栈，然后还原栈指针
        if (!curFunc.name.equals("main")) {
            // 把返回值赋值到v0中
            Value ret = retInstruction.value1;
            if (ret != null) {
                RealRegister reg = lookup(ret);
                mipsCode.add(new MipsInstruction("addu", "$v0", "$zero", reg.toString()));
                memManager.freeTempReg(reg);
            }
            // 把ra和fp还原
            fixStackTop();
            // 返回栈指针
            mipsCode.add(new MipsInstruction("jr", "$ra"));
        } else {
            mipsCode.add(new MipsInstruction("addiu", "$v0", "$zero", "10"));
            mipsCode.add(new MipsInstruction("syscall", ""));
        }
    }

    // 乘法优化
    private void mulOptimize(String res, String op1, String op2) {
        final int bitsOfInt = 32;
        int imm = Integer.parseInt(op2);
        int abs = (imm < 0) ? (-imm) : imm;
        if (abs == 0) {
            mipsCode.add(new MipsInstruction("addu", res, "$zero", "$zero"));
        } else if ((abs & (abs - 1)) == 0) {
            // imm 是 2 的幂
            int sh = bitsOfInt - 1 - Integer.numberOfLeadingZeros(abs);
            mipsCode.add(new MipsInstruction("sll", res, op1, Integer.toString(sh)));
            if (imm < 0) {
                mipsCode.add(new MipsInstruction("subu", res, "$zero", res));
            }
        } else if (Integer.bitCount(abs) == 2) {
            // 如果有两个1, 那就是两个shift操作
            // a * 10 => (a << 3) + (a << 1)
            int hi = bitsOfInt - 1 - Integer.numberOfLeadingZeros(abs);
            int lo = Integer.numberOfTrailingZeros(abs);
            RealRegister shiftHi = memManager.getTempReg(memManager.getTempNum());
            // 为了不改变原来的寄存器，所以需要一个临时寄存器
            RealRegister tempOp1 = memManager.getTempReg(memManager.getTempNum());
            mipsCode.add(new MipsInstruction("sll", shiftHi.toString(), op1, Integer.toString(hi)));
            mipsCode.add(new MipsInstruction("sll", tempOp1.toString(), op1, Integer.toString(lo)));
            mipsCode.add(new MipsInstruction("addu", res, shiftHi.toString(), tempOp1.toString()));
            if (imm < 0) {
                mipsCode.add(new MipsInstruction("subu", res, "$zero", res));
            }
            memManager.freeTempReg(shiftHi);
            memManager.freeTempReg(tempOp1);
        } else if (((abs + 1) & (abs)) == 0) {
            // 若乘数的绝对值为2的幂-1，可用一条移位指令和减法指令
            // a * (2^sh - 1) => (a << sh) - a
            int sh = bitsOfInt - 1 - Integer.numberOfLeadingZeros(abs + 1);
            RealRegister shift = memManager.getTempReg(memManager.getTempNum());
            mipsCode.add(new MipsInstruction("sll", shift.toString(), op1, Integer.toString(sh)));
            mipsCode.add(new MipsInstruction("subu", res, shift.toString(), op1));
            if (imm < 0) {
                mipsCode.add(new MipsInstruction("subu", res, "$zero", res));
            }
            memManager.freeTempReg(shift);
        } else {
            mipsCode.add(new MipsInstruction("mul", res, op1, op2));
        }
    }


    // choose_multiplier utils
    public static long[] choose_multiplier(int d, int prec) {
        int N = 32;
        long l = (long) Math.ceil((Math.log(d) / Math.log(2)));
        long sh_post = l;
        long m_low = (long) Math.floor(Math.pow(2, N + l) / d);
        long m_high = (long) Math.floor((Math.pow(2, N + l) + Math.pow(2, N + l - prec)) / d);
        while ((Math.floor(m_low >> 1) < Math.floor(m_high >> 1)) && sh_post > 0) {
            m_low = (long) Math.floor(m_low >> 1);
            m_high = (long) Math.floor(m_high >> 1);
            sh_post = sh_post - 1;
        }
        return new long[]{m_high, sh_post, l};
    }

    // 除法优化
    private void divOptimize(String res, String op1, String op2, String op) {
        final int bitsOfInt = 32;
        int imm = Integer.parseInt(op2);
        int abs = (imm < 0) ? (-imm) : imm;
        long[] multiplier = choose_multiplier(abs, bitsOfInt - 1);
        long m = multiplier[0];
        long sh_post = multiplier[1];
        long l = multiplier[2];
        RealRegister tempReg = memManager.getTempReg(memManager.getTempNum());
        if (imm == 1) {
            mipsCode.add(new MipsInstruction("addu", res, op1, "$zero"));
        } else if (imm == -1) {
            mipsCode.add(new MipsInstruction("subu", res, "$zero", op1));
        } else if (abs == 1 << l) {
            //q = SRA(op1 + SRL(SRA(op1, sh-1),bitsOfInt-sh),sh);
            int sh = bitsOfInt - 1 - Integer.numberOfLeadingZeros(abs);
            mipsCode.add(new MipsInstruction("sra", tempReg.toString(), op1, Integer.toString(sh - 1)));
            mipsCode.add(new MipsInstruction("srl", tempReg.toString(), tempReg.toString(), Integer.toString(bitsOfInt - sh)));
            mipsCode.add(new MipsInstruction("addu", tempReg.toString(), tempReg.toString(), op1));
            mipsCode.add(new MipsInstruction("sra", res, tempReg.toString(), Integer.toString(sh)));
            memManager.freeTempReg(tempReg);
        } else if (m < (1L << (bitsOfInt - 1))) {
            // q = SRA(MULSH(m, n), shpost) - XSIGN(n);
            mipsCode.add(new MipsInstruction("addiu", tempReg.toString(), "$zero", Long.toString(m)));
            mipsCode.add(new MipsInstruction("mult", tempReg.toString(), op1));
            mipsCode.add(new MipsInstruction("mfhi", tempReg.toString()));
            mipsCode.add(new MipsInstruction("sra", res, tempReg.toString(), Long.toString(sh_post)));
            mipsCode.add(new MipsInstruction("sra", tempReg.toString(), op1, Integer.toString(bitsOfInt - 1)));
            mipsCode.add(new MipsInstruction("subu", res, res, tempReg.toString()));
        } else {
            // q = SRA(n + MULSH(m - 2^N, n), shpost)- XSIGN(n);
            mipsCode.add(new MipsInstruction("addiu", tempReg.toString(), "$zero", Long.toString(m - (1L << bitsOfInt))));
            mipsCode.add(new MipsInstruction("mult", tempReg.toString(), op1));
            mipsCode.add(new MipsInstruction("mfhi", tempReg.toString()));
            mipsCode.add(new MipsInstruction("addu", tempReg.toString(), tempReg.toString(), op1));
            mipsCode.add(new MipsInstruction("sra", res, tempReg.toString(), Long.toString(sh_post)));
            mipsCode.add(new MipsInstruction("sra", tempReg.toString(), op1, Integer.toString(bitsOfInt - 1)));
            mipsCode.add(new MipsInstruction("subu", res, res, tempReg.toString()));
        }
        if (imm < 0) {
            mipsCode.add(new MipsInstruction("subu", res, "$zero", res));
        }
        if (op.equals("rem")) {
            mipsCode.add(new MipsInstruction("mul", tempReg.toString(), res, op2));
            mipsCode.add(new MipsInstruction("subu", res, op1, tempReg.toString()));
        }
        memManager.freeTempReg(tempReg);
    }

    // 二元运算
    private void genBinaryInstr(BinaryInstruction binaryInstruction) {
        String Operator = "";
        if (binaryInstruction.op.type.equals(Op.Type.add)) Operator = "addu";
        if (binaryInstruction.op.type.equals(Op.Type.sub)) Operator = "subu";
        if (binaryInstruction.op.type.equals(Op.Type.mul)) Operator = "mul";
        if (binaryInstruction.op.type.equals(Op.Type.sdiv)) Operator = "div";
        if (binaryInstruction.op.type.equals(Op.Type.srem)) Operator = "rem";

        // 得到第一个寄存器
        RealRegister tempReg1 = lookup(binaryInstruction.value1);
        String reg1 = tempReg1.toString();

        // 得到第二个寄存器
        // 如果是立即数，就直接用立即数
        String reg2;
        RealRegister tempReg2 = null;
        if (binaryInstruction.value2 instanceof Constant) {
            reg2 = binaryInstruction.value2.name;
        } else {
            tempReg2 = lookup(binaryInstruction.value2);
            reg2 = tempReg2.toString();
        }
        // 得到结果寄存器
        RealRegister tempRegRes = memManager.getTempReg(binaryInstruction.result.getName());
        String regRes = tempRegRes.toString();

        // 如果是加减法
        if (Operator.equals("addu") || Operator.equals("subu"))
            mipsCode.add(new MipsInstruction(Operator, regRes, reg1, reg2));

        // 如果是乘法
        if (Operator.equals("mul")) {
            if (binaryInstruction.value2 instanceof Constant) {
                mulOptimize(regRes, reg1, reg2);
            } else {
                mipsCode.add(new MipsInstruction(Operator, regRes, reg1, reg2));
            }
        }
        // 如果是除法和模
        if (Operator.equals("div") || Operator.equals("rem")) {
            if (binaryInstruction.value2 instanceof Constant) {
                divOptimize(regRes, reg1, reg2, Operator);
            } else {
                mipsCode.add(new MipsInstruction("div", reg1, reg2));
                if (Operator.equals("div"))
                    mipsCode.add(new MipsInstruction("mflo", regRes));
                else
                    mipsCode.add(new MipsInstruction("mfhi", regRes));
            }
        }
        // 释放寄存器
        memManager.freeTempReg(tempReg1);
        if (tempReg2 != null) memManager.freeTempReg(tempReg2);
        //假如目前的寄存器使用量大于6个，就把结果存在内存中
        if (memManager.getUseTempReg() > memManager.TEMP_REG_NUM - 4) {
            Stack stack = memManager.getStackReg(binaryInstruction.result.getName());
            mipsCode.add(new MipsInstruction(true, "sw", regRes, "$sp", stack.toString()));
            memManager.freeTempReg(tempRegRes);
        }
    }

    private void loadParam(ArrayList<Value> params) {
        for (int i = 0; i < params.size(); i++) {
            Value param = params.get(i);
            if (i < 4) {
                // 如果是常数
                if (param instanceof Constant) {
                    mipsCode.add(new MipsInstruction("li", "$a" + i, param.toString()));
                } else {
                    RealRegister realRegister = memManager.lookUpTemp(param.getName());
                    if (realRegister != null) {
                        mipsCode.add(new MipsInstruction("move", "$a" + i, realRegister.toString()));
                        memManager.freeTempReg(realRegister);
                        continue;
                    }
                    RealRegister globalRegister = memManager.lookUpGlobalReg(param.getName());
                    if (globalRegister != null) {
                        mipsCode.add(new MipsInstruction("move", "$a" + i, globalRegister.toString()));
                        continue;
                    }
                    // 如果是栈上的值，就先把栈上的值加载到寄存器里
                    Stack stack = memManager.lookUpStack(param.getName());
                    String global = memManager.lookupGlobal(param.getName());
                    if (stack != null) {
                        // 如果参数的类型是i32但是虚拟寄存器是数组
                        if (memManager.isArrayVirtualReg(param.getName()) && param.getType() == ValueType.i32) {
                            //那就先把数组的地址load出来,然后再把值load到ax寄存器里
                            RealRegister arrayAddr = memManager.getTempReg(param.getName());
                            mipsCode.add(new MipsInstruction(true, "lw", arrayAddr.toString(), "$sp", stack.toString()));
                            mipsCode.add(new MipsInstruction(true, "lw", "$a" + i, arrayAddr.toString(), "0"));
                            memManager.freeTempReg(arrayAddr);
                        } else mipsCode.add(new MipsInstruction(true, "lw", "$a" + i, "$sp", stack.toString()));
                    } else if (global != null) {
                        mipsCode.add(new MipsInstruction("lw", "$a" + i, global));
                    } else throw new RuntimeException();
                }
            } else {
                // 如果参数大于4个，就要把参数放到栈上
                RealRegister realRegister = memManager.lookUpTemp(param.getName());
                Stack stack_ = memManager.lookUpStack("param" + i);
                if (realRegister != null) {
                    mipsCode.add(new MipsInstruction(true, "sw", realRegister.toString(), "$sp", stack_.toString()));
                    memManager.freeTempReg(realRegister);
                    continue;
                }
                RealRegister globalRegister = memManager.lookUpGlobalReg(param.getName());
                if (globalRegister != null) {
                    mipsCode.add(new MipsInstruction(true, "sw", globalRegister.toString(), "$sp", stack_.toString()));
                    continue;
                }
                // 处理常数情况
                if (param instanceof Constant) {
                    RealRegister tempReg = memManager.getTempReg(memManager.getTempNum());
                    mipsCode.add(new MipsInstruction("li", tempReg.toString(), param.toString()));
                    Stack stack = memManager.lookUpStack("param" + i);
                    mipsCode.add(new MipsInstruction(true, "sw", tempReg.toString(), "$sp", stack.toString()));
                    memManager.freeTempReg(tempReg);
                } else {
                    // 如果是栈上的值，就先把栈上的值加载到寄存器里
                    Stack stack = memManager.lookUpStack(param.getName());
                    String global = memManager.lookupGlobal(param.getName());
                    if (stack != null) {
                        // 如果参数的类型是i32但是虚拟寄存器是数组
                        if (memManager.isArrayVirtualReg(param.getName()) && param.getType() == ValueType.i32) {
                            //那就先把数组的地址load出来
                            RealRegister arrayAddr = memManager.getTempReg(param.getName());
                            mipsCode.add(new MipsInstruction(true, "lw", arrayAddr.toString(), "$sp", stack.toString()));
                            mipsCode.add(new MipsInstruction(true, "lw", arrayAddr.toString(), arrayAddr.toString(), "0"));
                            Stack stackParam = memManager.lookUpStack("param" + i);
                            mipsCode.add(new MipsInstruction(true, "sw", arrayAddr.toString(), "$sp", stackParam.toString()));
                            memManager.freeTempReg(arrayAddr);
                        } else {
                            RealRegister tempReg = memManager.getTempReg(memManager.getTempNum());
                            mipsCode.add(new MipsInstruction(true, "lw", tempReg.toString(), "$sp", stack.toString()));
                            Stack stackParam = memManager.lookUpStack("param" + i);
                            mipsCode.add(new MipsInstruction(true, "sw", tempReg.toString(), "$sp", stackParam.toString()));
                            memManager.freeTempReg(tempReg);
                        }
                    } else if (global != null) {
                        RealRegister tempReg = memManager.getTempReg(memManager.getTempNum());
                        mipsCode.add(new MipsInstruction("lw", tempReg.toString(), global));
                        Stack stackParam = memManager.lookUpStack("param" + i);
                        mipsCode.add(new MipsInstruction(true, "sw", tempReg.toString(), "$sp", stackParam.toString()));
                        memManager.freeTempReg(tempReg);
                    } else throw new RuntimeException();
                }
            }
        }
    }

    // 判断是否是IO函数
    private boolean isIOCall(CallInstruction callInstruction) {
        String name = callInstruction.value1.name;
        return name.equals("putch") || name.equals("putint") || name.equals("getint");
    }

    // 内联IO函数
    private void inlineIO(CallInstruction callInstruction) {
        if (callInstruction.value1.name.equals("putch")) {
            mipsCode.add(new MipsInstruction("addiu", "$v0", "$zero", "11"));
            mipsCode.add(new MipsInstruction("syscall", ""));
        }
        if (callInstruction.value1.name.equals("putint")) {
            mipsCode.add(new MipsInstruction("addiu", "$v0", "$zero", "1"));
            mipsCode.add(new MipsInstruction("syscall", ""));
        }
        if (callInstruction.value1.name.equals("getint")) {
            mipsCode.add(new MipsInstruction("addiu", "$v0", "$zero", "5"));
            mipsCode.add(new MipsInstruction("syscall", ""));
            RealRegister tempReg = this.memManager.getTempReg(callInstruction.result.getName());
            mipsCode.add(new MipsInstruction("addu", tempReg.toString(), "$zero", "$v0"));
            //把东西存到栈里
            Stack stack = memManager.getStackReg(callInstruction.result.getName());
            mipsCode.add(new MipsInstruction(true, "sw", tempReg.toString(), "$sp", stack.toString()));
            //释放寄存器
            memManager.freeTempReg(tempReg);
        }
    }

    private void genCallInstr(CallInstruction callInstruction) {
        // 首先把函数的参数加载到对应的寄存器中
        int size = callInstruction.funcRParams.size();
        loadParam(callInstruction.funcRParams);
        // 如果是IO函数，就直接内联
        if (isIOCall(callInstruction)) {
            inlineIO(callInstruction);
            return;
        }
        // 释放有关寄存器
        for (int i = 0; i < size; i++) {
            Value param = callInstruction.funcRParams.get(i);
            RealRegister realRegister = memManager.lookUpTemp(param.getName());
            if (realRegister != null) memManager.freeTempReg(realRegister);
        }
        // 保存现场
        for (int i = memManager.TEMP_REG_LOWER_BOUND; i < memManager.GLOBAL_REG_UPPERBOUND; i++) {
            if (memManager.temRegUseMap[i] != VirtualRegister.None) {
                Stack stack = memManager.lookUpStack(memManager.tempRegPool.get(i).toString());
                RealRegister tempReg = memManager.tempRegPool.get(i);
                mipsCode.add(new MipsInstruction(true, "sw", tempReg.toString(), "$sp", stack.toString()));
                memManager.record(tempReg, stack, memManager.temRegUseMap[i]);
                memManager.freeTempReg(tempReg);
            }
        }
        // 跳转到函数名
        String funcName = callInstruction.value1.name;
        mipsCode.add(new MipsInstruction("jal", funcName));
        // 恢复现场, 这里不需要再次申请寄存器
        for (Triple triple : memManager.Recorder) {
            mipsCode.add(new MipsInstruction(true, "lw", triple.getFirst().toString(), "$sp", triple.getSecond().toString()));
            memManager.temRegUseMap[((RealRegister) triple.getFirst()).getNum()] = (VirtualRegister) triple.getThird();
        }
        memManager.recordClear();
        // 如果有返回值，把$v0的赋值到另外一个寄存器里
        if (callInstruction.result != null) {
            RealRegister tempReg = this.memManager.getTempReg(callInstruction.result.getName());
            mipsCode.add(new MipsInstruction("addu", tempReg.toString(), "$zero", "$v0"));
            //把东西存到栈里
            Stack stack = memManager.getStackReg(callInstruction.result.getName());
            mipsCode.add(new MipsInstruction(true, "sw", tempReg.toString(), "$sp", stack.toString()));
            //释放寄存器
            memManager.freeTempReg(tempReg);
        }
    }

    // mips里的比较分支是一条语句, 我们需要把ir的两条语句合并成一条
    // ir里的这个比较和分支是连在一起的, 所以我们可以设个全局变量, 一起处理。
    private void genIcmpInstr(IcmpInstruction icmpInstruction) {
        RealRegister tempReg1 = lookup(icmpInstruction.value1);
        RealRegister tempReg2 = lookup(icmpInstruction.value2);
        RealRegister tempRegRes = memManager.getTempReg(icmpInstruction.result.getName());
        String Operator = "";
        if (icmpInstruction.op.type.equals(Op.Type.eq)) Operator = "seq";
        else if (icmpInstruction.op.type.equals(Op.Type.ne)) Operator = "sne";
        else if (icmpInstruction.op.type.equals(Op.Type.sge)) Operator = "sge";
        else if (icmpInstruction.op.type.equals(Op.Type.sgt)) Operator = "sgt";
        else if (icmpInstruction.op.type.equals(Op.Type.sle)) Operator = "sle";
        else if (icmpInstruction.op.type.equals(Op.Type.slt)) Operator = "slt";
        mipsCode.add(new MipsInstruction(Operator, tempRegRes.toString(), tempReg1.toString(), tempReg2.toString()));
        memManager.freeTempReg(tempReg1);
        memManager.freeTempReg(tempReg2);
    }

    private void genBranchInstruction(BranchInstruction branchInstruction) {
        if (branchInstruction.value2 != null) {
            RealRegister tempReg = memManager.lookUpTemp(branchInstruction.cond.getName());
            String label_true = curFunc.name + "_label_" + branchInstruction.value1.name;
            String label_false = curFunc.name + "_label_" + branchInstruction.value2.name;
            mipsCode.add(new MipsInstruction("beq", tempReg.toString(), "$zero", label_false));
            if (!branchInstruction.value1.name.equals(((BasicBlock) currentBB.getNext()).name))
                mipsCode.add(new MipsInstruction("j", label_true));
            memManager.freeTempReg(tempReg);
        } else {
            String label_true = curFunc.name + "_label_" + branchInstruction.value1.name;
            if (!branchInstruction.value1.name.equals(((BasicBlock) currentBB.getNext()).name))
                mipsCode.add(new MipsInstruction("j", label_true));
        }

    }

    private void genZext(ZextInstruction zextInstruction) {
        RealRegister tempReg = memManager.lookUpTemp(zextInstruction.value1.getName());
        memManager.temRegUseMap[tempReg.getNum()] = new VirtualRegister(zextInstruction.result.getName());
    }

    // 对于数组的访问, 由于llvm ir 的特殊结构, 取二维数组要取两次这个就比较麻烦
    private void genGetElementPtr(GetElementPtr getElementPtr) {
        if (getElementPtr.bound2 == null) {
            // 如果是一维数组,直接偏移，二维数组还需要偏移x列数
            RealRegister arrOffset = lookup(getElementPtr.bound1);
            RealRegister array = lookup(getElementPtr.value1);
            ValueType.Type type = getElementPtr.value1.getInnerType();
            // 为了不改变原来的寄存器, 我们申请一个新的寄存器
            RealRegister tempReg = memManager.getTempReg(memManager.getTempNum());
            if (type != ValueType.i32) {
                String col = Integer.toString(((ValueType.ArrayType) type).size());
                mipsCode.add(new MipsInstruction("mul", tempReg.toString(), arrOffset.toString(), col));
                mipsCode.add(new MipsInstruction("sll", tempReg.toString(), tempReg.toString(), "2"));
            } else mipsCode.add(new MipsInstruction("sll", tempReg.toString(), arrOffset.toString(), "2"));
            mipsCode.add(new MipsInstruction("addu", array.toString(), array.toString(), tempReg.toString()));
            // 申请空间把array存起来
            Stack curOffsetStack = memManager.getStackReg(getElementPtr.result.getName());
            mipsCode.add(new MipsInstruction(true, "sw", array.toString(), "$sp", curOffsetStack.toString()));
            // 释放寄存器
            memManager.freeTempReg(array);
            memManager.freeTempReg(arrOffset);
            memManager.freeTempReg(tempReg);
        } else {
            // 一般使用数组的方法，就是使用一开始申请的那个虚拟寄存器
            ValueType.Type type = getElementPtr.value1.getInnerType();
            // 如果是已经降维到了一维数组那么就直接算offset
            if (type.getType() == ValueType.i32) {
                // 首先直接把数组的地址加载进来
                String array = getElementPtr.value1.getName();
                Stack stack = memManager.lookUpStack(array);
                // 计算偏移量
                RealRegister curOffset = lookup(getElementPtr.bound2);
                // 为了不改变原来的寄存器, 我们申请一个新的寄存器
                RealRegister tempReg_ = memManager.getTempReg(memManager.getTempNum());
                mipsCode.add(new MipsInstruction("sll", tempReg_.toString(), curOffset.toString(), "2"));
                if (stack != null) {
                    // 如果是二维数组这个里面装的是地址，所以需要选load出数组地址
                    RealRegister arrayAddr;
                    if (memManager.isArrayVirtualReg(array)) {
                        // 首先加载数组地址
                        arrayAddr = memManager.getTempReg(array);
                        mipsCode.add(new MipsInstruction(true, "lw", arrayAddr.toString(), "$sp", stack.toString()));
                    } else { // 如果是一维数组地址计算用sp计算就可以
                        arrayAddr = memManager.getTempReg(memManager.getTempNum());
                        mipsCode.add(new MipsInstruction("addu", arrayAddr.toString(), "$sp", stack.toString()));
                    }
                    // 然后计算偏移量
                    mipsCode.add(new MipsInstruction("addu", tempReg_.toString(), arrayAddr.toString(), tempReg_.toString()));
                    memManager.freeTempReg(arrayAddr);
                } else if (memManager.isGlobal(getElementPtr.value1.getName())) {
                    RealRegister tempReg = memManager.getTempReg(memManager.getTempNum());
                    mipsCode.add(new MipsInstruction("la", tempReg.toString(), getElementPtr.value1.name));
                    mipsCode.add(new MipsInstruction("addu", tempReg_.toString(), tempReg.toString(), tempReg_.toString()));
                    memManager.freeTempReg(tempReg);
                }
                // 申请空间把currentOffset存起来
                Stack curOffsetStack = memManager.getStackReg(getElementPtr.result.getName());
                mipsCode.add(new MipsInstruction(true, "sw", tempReg_.toString(), "$sp", curOffsetStack.toString()));
                // 释放寄存器
                memManager.freeTempReg(curOffset);
                memManager.freeTempReg(tempReg_);
            } else {
                // 首先计算偏移量
                RealRegister curOffset = lookup(getElementPtr.bound2);
                ValueType.ArrayType arrayType = (ValueType.ArrayType) type;
                int secDim = arrayType.getDim().get(1);
                // 为了不改变原来的寄存器, 我们申请一个新的寄存器
                RealRegister tempReg_ = memManager.getTempReg(memManager.getTempNum());
                mipsCode.add(new MipsInstruction("mul", tempReg_.toString(), curOffset.toString(), Integer.toString(secDim)));
                //地址对齐
                mipsCode.add(new MipsInstruction("sll", tempReg_.toString(), tempReg_.toString(), "2"));
                // 首先找到目前的数组的地址
                RealRegister tempReg = memManager.getTempReg(memManager.getTempNum());
                if (memManager.isGlobal(getElementPtr.value1.getName())) {
                    mipsCode.add(new MipsInstruction("la", tempReg.toString(), getElementPtr.value1.name));
                    mipsCode.add(new MipsInstruction("addu", tempReg_.toString(), tempReg.toString(), tempReg_.toString()));
                } else {
                    Stack stack = memManager.lookUpStack(getElementPtr.value1.getName());
                    mipsCode.add(new MipsInstruction("addu", tempReg.toString(), "$sp", stack.toString()));
                    mipsCode.add(new MipsInstruction("addu", tempReg_.toString(), tempReg.toString(), tempReg_.toString()));
                }
                // 申请空间把currentOffset存起来
                Stack stack = memManager.getStackReg(getElementPtr.result.getName());
                mipsCode.add(new MipsInstruction(true, "sw", tempReg_.toString(), "$sp", stack.toString()));
                // 把寄存器free掉
                memManager.freeTempReg(tempReg);
                memManager.freeTempReg(curOffset);
                memManager.freeTempReg(tempReg_);
            }
        }
    }

    private void genInstruction(BaseInstruction instruction) {
        if (!(instruction instanceof AllocateInstruction) && isComment) {
            MipsInstruction comment = new MipsInstruction();
            comment.addComment(instruction.toString());
            mipsCode.add(comment);
        }
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