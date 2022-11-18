package Backend;

import Backend.Mem.RealRegister;
import Backend.Mem.Stack;
import Backend.Mem.VirtualRegister;
import Middle.IRElement.Basic.*;
import Middle.IRElement.Basic.Module;
import Middle.IRElement.Instructions.*;
import Middle.IRElement.Type.ValueType;
import Middle.IRElement.Value;
import Utils.Triple;

import java.util.ArrayList;
import java.util.Collections;

public class CodeGen {
    public Module irModule;
    public Function curFunc;
    public RegAllocator regAllocator;
    public ArrayList<MipsInstruction> mipsCode;

    public int maxParamStack;

    public CodeGen(Module irModule) {
        this.mipsCode = new ArrayList<>();
        this.regAllocator = new RegAllocator();
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
        // 保存现场，用了几个寄存器就保存几个,这里先用8个,这8个就是一直在的
        memSize += 4 * 10;

        for (BasicBlock basicBlock : function.basicBlocks) {
            for (BaseInstruction instruction : basicBlock.instructions) {
                if (instruction instanceof AllocateInstruction) {
                    AllocateInstruction alloc = (AllocateInstruction) instruction;
                    ValueType.Type type = alloc.result.getInnerType();
                    if (type instanceof ValueType.ArrayType) memSize += ((ValueType.ArrayType) type).getTotalSize() * 4;
                    else memSize += 4;
                } // 如果是二元表达式，则需要存
                else if (instruction instanceof BinaryInstruction) memSize += 4;
                else if (instruction instanceof GetElementPtr) {
                    // 每一个数组加载出来的地址需要存到相应的地方
                    memSize += 4;
                    // 如果是数组加载出来的结果就需要添加到数组的集合中
                    GetElementPtr getElementPtr = (GetElementPtr) instruction;
                    regAllocator.addToArrayVirtualReg(getElementPtr.result.getName());
                } else if (instruction instanceof CallInstruction) {
                    // 如果是callInstruction有返回值也需要加内存
                    if (instruction.result != null) memSize += 4;
                    CallInstruction callInstruction = (CallInstruction) instruction;
                    // 加载参数 ,对于函数加载参数之前的load语句不释放寄存器的情况需要解决
                    int size = callInstruction.funcRParams.size();
                    // 如果是参数，加载出来的结果就需要放到参数的集合中
                    ArrayList<Value> params = callInstruction.funcRParams;
                    for (int i = 0; i < size; i++) {
                        regAllocator.addToParam(params.get(i).getName());
                    }
                    // 对于每一个函数调用，都添加最大的函数调用栈
                    memSize += maxParamStack * 4;
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
            } else genDefine(function);
        }
    }

    private void genDefine(Function function) {
        mipsCode.add(new MipsInstruction(function.name + ":"));
        if (function.name.equals("putch")) mipsCode.add(new MipsInstruction("addiu", "$v0", "$zero", "11"));
        if (function.name.equals("putint")) mipsCode.add(new MipsInstruction("addiu", "$v0", "$zero", "1"));
        if (function.name.equals("getint")) mipsCode.add(new MipsInstruction("addiu", "$v0", "$zero", "5"));
        mipsCode.add(new MipsInstruction("syscall", ""));
        mipsCode.add(new MipsInstruction("jr", "$ra"));
    }

    private void saveStackTop(int memSize) {
        // 先把$sp存起来,存到寄存器，然后把$sp覆盖, 申请空间之后, 再把$sp存到内存里
        mipsCode.add(new MipsInstruction("addu", "$fp", "$sp", "$zero"));
        mipsCode.add(new MipsInstruction("addiu", "$sp", "$sp", "-" + memSize));
        // 把原来的$sp存起来
        Stack stack = regAllocator.getStackReg("$sp");
        mipsCode.add(new MipsInstruction(true, "sw", "$fp", "$sp", stack.toString()));
        // 把$ra存起来
        stack = regAllocator.getStackReg("$ra");
        mipsCode.add(new MipsInstruction(true, "sw", "$ra", "$sp", stack.toString()));
    }

    private void fixStackTop() {
        // 把$ra还原
        Stack stack = regAllocator.lookUpStack("$ra");
        mipsCode.add(new MipsInstruction(true, "lw", "$ra", "$sp", stack.toString()));
        // 把$sp还原
        stack = regAllocator.lookUpStack("$sp");
        mipsCode.add(new MipsInstruction(true, "lw", "$sp", "$sp", stack.toString()));
    }

    private void genFunction(Function function) {
        mipsCode.add(new MipsInstruction(function.name + ":"));
        // 加载函数所需内存
        int memSize = preProcess(function);
        // 把函数调用栈的字典建立起来
        for (int i = 0; i < maxParamStack; i++) {
            regAllocator.virtual2Stack.put(new VirtualRegister("param" + (4 + i)), new Stack(i));
        }
        // 把寄存器的保存现场用的字典建起来
        for (int i = 8; i < 18; i++) {
            regAllocator.getStackReg(regAllocator.tempRegPool.get(i).toString());
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
        regAllocator.clear();
    }

    // 这个函数其实是需要调整符号表
    private void genFunctionParam(ArrayList<Value> functionParams, int memSize) {
        // 参数的个数——size
        int size = functionParams.size();
        // 4号寄存器是a0
        for (int i = 0; i < size && i < 4; i++)
            regAllocator.temRegUseMap[i + 4] = new VirtualRegister("%" + i);
        for (int i = 4; i < size; i++)
            regAllocator.virtual2Stack.put(new VirtualRegister("%" + i), new Stack(memSize / 4 + i - 4));
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
        regAllocator.globalSet.add(globalVariable.getName());
    }

    private void genBlock(BasicBlock block, boolean isFirst) {
        if (!isFirst) mipsCode.add(new MipsInstruction(curFunc.name + "_label_" + block.name + ":"));
        for (BaseInstruction instruction : block.instructions) {
            genInstruction(instruction);
        }
    }

    private void genAllocInstr(AllocateInstruction allocateInstruction) {
        String virtualNum = allocateInstruction.result.getName();
        ValueType.Type type = allocateInstruction.result.getInnerType();
        // 如果是数组，则计算空间后分配
        if (type instanceof ValueType.ArrayType) {
            int curMemSize = ((ValueType.ArrayType) type).getTotalSize();
            regAllocator.getStackReg(allocateInstruction.result.getName(), curMemSize);
        } else this.regAllocator.getStackReg(virtualNum); // 如果是整数或者是一个地址，则直接分配空间
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
                mipsCode.add(new MipsInstruction(true, "lw", tempReg.toString(), "$sp", stackReg.toString()));
            }
        }
        return tempReg;
    }

    private void genStoreInstr(StoreInstruction storeInstruction) {
        RealRegister tempReg = lookup(storeInstruction.value1);
        Stack stackReg = regAllocator.lookUpStack(storeInstruction.value2.getName());
        if (stackReg != null) {
            // 如果是数组就需要先load出地址
            if (regAllocator.isArrayVirtualReg(storeInstruction.value2.getName())) {
                // 申请临时寄存器把数组地址load出来
                RealRegister arrayAddr = regAllocator.getTempReg(regAllocator.getTempNum());
                mipsCode.add(new MipsInstruction(true, "lw", arrayAddr.toString(), "$sp", stackReg.toString()));
                mipsCode.add(new MipsInstruction(true, "sw", tempReg.toString(), arrayAddr.toString(), "0"));
                // 释放零时寄存器
                regAllocator.freeTempReg(arrayAddr);
            } else mipsCode.add(new MipsInstruction(true, "sw", tempReg.toString(), "$sp", stackReg.toString()));
        } else if (regAllocator.isGlobal(storeInstruction.value2.getName())) {
            mipsCode.add(new MipsInstruction("sw", tempReg.toString(), storeInstruction.value2.name));
        }
        regAllocator.freeTempReg(tempReg);
    }

    private void genLoadInstr(LoadInstruction loadInstruction) {
        if (regAllocator.isParam(loadInstruction.result.getName())) {
            // 如果是参数，就直接做个对应，不做翻译工作，但是需要注意的是数组对应的是地址的地址
            Stack stack = regAllocator.lookUpStack(loadInstruction.value1.getName());
            regAllocator.virtual2Stack.put(new VirtualRegister(loadInstruction.result.getName()), stack);
            // 如果是数组也把他加进数组的hashset里
            if (regAllocator.isArrayVirtualReg(loadInstruction.value1.getName())) {
                regAllocator.addToArrayVirtualReg(loadInstruction.result.getName());
            }
            // 如果是全局变量则添加字典
            if (regAllocator.isGlobal(loadInstruction.value1.getName()))
                regAllocator.addToGlobal(loadInstruction.result.getName(), loadInstruction.value1.getName());
        } else {
            // 对于不同的类别需要不同的寄存器
            RealRegister resultReg = regAllocator.getTempReg(loadInstruction.result.getName());
            Stack stackReg = regAllocator.lookUpStack(loadInstruction.value1.getName());
            // 这是一般的情况, 从栈上加载数据到寄存器里
            if (stackReg != null)
                // 如果是数组就需要先load出地址
                if (regAllocator.isArrayVirtualReg(loadInstruction.value1.getName())) {
                    // 申请临时寄存器把数组地址load出来
                    RealRegister arrayAddr = regAllocator.getTempReg(regAllocator.getTempNum());
                    mipsCode.add(new MipsInstruction(true, "lw", arrayAddr.toString(), "$sp", stackReg.toString()));
                    mipsCode.add(new MipsInstruction(true, "lw", resultReg.toString(), arrayAddr.toString(), "0"));
                    // 释放零时寄存器
                    regAllocator.freeTempReg(arrayAddr);
                } else mipsCode.add(new MipsInstruction(true, "lw", resultReg.toString(), "$sp", stackReg.toString()));
            else if (regAllocator.isGlobal(loadInstruction.value1.getName())) {
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
                regAllocator.freeTempReg(reg);
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

    private void genBinaryInstr(BinaryInstruction binaryInstruction) {
        String Operator = "";
        if (binaryInstruction.op.type.equals(Op.Type.add)) Operator = "addu";
        if (binaryInstruction.op.type.equals(Op.Type.sub)) Operator = "sub";
        if (binaryInstruction.op.type.equals(Op.Type.mul)) Operator = "mul";
        if (binaryInstruction.op.type.equals(Op.Type.sdiv)) Operator = "div";
        if (binaryInstruction.op.type.equals(Op.Type.srem)) Operator = "rem";
        // 做二元算式
        RealRegister tempReg1 = lookup(binaryInstruction.value1);
        RealRegister tempReg2 = lookup(binaryInstruction.value2);
        RealRegister tempRegRes = regAllocator.getTempReg(binaryInstruction.result.getName());
        // 如果是加减法
        if (Operator.equals("addu") || Operator.equals("sub") || Operator.equals("mul"))
            mipsCode.add(new MipsInstruction(Operator, tempRegRes.toString(), tempReg1.toString(), tempReg2.toString()));
        // 如果是除法和模
        if (Operator.equals("div") || Operator.equals("rem")) {
            // 先做除法
            mipsCode.add(new MipsInstruction("div", tempReg1.toString(), tempReg2.toString()));
            // 如果是除法取低位
            if (Operator.equals("div")) mipsCode.add(new MipsInstruction("mflo", tempRegRes.toString()));
            // 如果是模取高位
            if (Operator.equals("rem")) mipsCode.add(new MipsInstruction("mfhi", tempRegRes.toString()));
        }
        // 把东西存到栈里
        Stack stack = regAllocator.getStackReg(binaryInstruction.result.getName());
        mipsCode.add(new MipsInstruction(true, "sw", tempRegRes.toString(), "$sp", stack.toString()));
        // 释放寄存器
        regAllocator.freeTempReg(tempReg1);
        regAllocator.freeTempReg(tempReg2);
        regAllocator.freeTempReg(tempRegRes);
    }

    private void loadParam(ArrayList<Value> params) {
        for (int i = 0; i < params.size(); i++) {
            Value param = params.get(i);
            if (i < 4) {
                // 如果是常数
                if (param instanceof Constant) {
                    mipsCode.add(new MipsInstruction("li", "$a" + i, param.toString()));
                } else {
                    // 如果是栈上的值，就先把栈上的值加载到寄存器里
                    Stack stack = regAllocator.lookUpStack(param.getName());
                    String global = regAllocator.lookupGlobal(param.getName());
                    if (stack != null) {
                        // 如果参数的类型是i32但是虚拟寄存器是数组
                        if (regAllocator.isArrayVirtualReg(param.getName()) && param.getType() == ValueType.i32) {
                            //那就先把数组的地址load出来,然后再把值load到ax寄存器里
                            RealRegister arrayAddr = regAllocator.getTempReg(param.getName());
                            mipsCode.add(new MipsInstruction(true, "lw", arrayAddr.toString(), "$sp", stack.toString()));
                            mipsCode.add(new MipsInstruction(true, "lw", "$a" + i, arrayAddr.toString(), "0"));
                            regAllocator.freeTempReg(arrayAddr);
                        } else mipsCode.add(new MipsInstruction(true, "lw", "$a" + i, "$sp", stack.toString()));
                    } else if (global != null) {
                        mipsCode.add(new MipsInstruction("lw", "$a" + i, global));
                    } else throw new RuntimeException();
                }
            } else {
                // 如果参数大于4个，就要把参数放到栈上
                // 处理常数情况
                if (param instanceof Constant) {
                    RealRegister tempReg = regAllocator.getTempReg(regAllocator.getTempNum());
                    mipsCode.add(new MipsInstruction("li", tempReg.toString(), param.toString()));
                    Stack stack = regAllocator.lookUpStack("param" + i);
                    mipsCode.add(new MipsInstruction(true, "sw", tempReg.toString(), "$sp", stack.toString()));
                    regAllocator.freeTempReg(tempReg);
                } else {
                    // 如果是栈上的值，就先把栈上的值加载到寄存器里
                    Stack stack = regAllocator.lookUpStack(param.getName());
                    String global = regAllocator.lookupGlobal(param.getName());
                    if (stack != null) {
                        // 如果参数的类型是i32但是虚拟寄存器是数组
                        if (regAllocator.isArrayVirtualReg(param.getName()) && param.getType() == ValueType.i32) {
                            //那就先把数组的地址load出来
                            RealRegister arrayAddr = regAllocator.getTempReg(param.getName());
                            mipsCode.add(new MipsInstruction(true, "lw", arrayAddr.toString(), "$sp", stack.toString()));
                            mipsCode.add(new MipsInstruction(true, "lw", arrayAddr.toString(), arrayAddr.toString(), "0"));
                            Stack stackParam = regAllocator.lookUpStack("param" + i);
                            mipsCode.add(new MipsInstruction(true, "sw", arrayAddr.toString(), "$sp", stackParam.toString()));
                            regAllocator.freeTempReg(arrayAddr);
                        } else {
                            RealRegister tempReg = regAllocator.getTempReg(regAllocator.getTempNum());
                            mipsCode.add(new MipsInstruction(true, "lw", tempReg.toString(), "$sp", stack.toString()));
                            Stack stackParam = regAllocator.lookUpStack("param" + i);
                            mipsCode.add(new MipsInstruction(true, "sw", tempReg.toString(), "$sp", stackParam.toString()));
                            regAllocator.freeTempReg(tempReg);
                        }
                    } else if (global != null) {
                        RealRegister tempReg = regAllocator.getTempReg(regAllocator.getTempNum());
                        mipsCode.add(new MipsInstruction("lw", tempReg.toString(), global));
                        Stack stackParam = regAllocator.lookUpStack("param" + i);
                        mipsCode.add(new MipsInstruction(true, "sw", tempReg.toString(), "$sp", stackParam.toString()));
                        regAllocator.freeTempReg(tempReg);
                    } else throw new RuntimeException();
                }
            }
        }
    }


    private void genCallInstr(CallInstruction callInstruction) {
        // 首先把函数的参数加载到对应的寄存器中
        int size = callInstruction.funcRParams.size();
        loadParam(callInstruction.funcRParams);
        // 释放有关寄存器
        for (int i = 0; i < size; i++) {
            Value param = callInstruction.funcRParams.get(i);
            RealRegister realRegister = regAllocator.lookUpTemp(param.getName());
            if (realRegister != null) regAllocator.freeTempReg(realRegister);
        }

        // 保存现场
        for (int i = 8; i < 18; i++) {
            if (regAllocator.temRegUseMap[i] != VirtualRegister.None) {
                Stack stack = regAllocator.lookUpStack(regAllocator.tempRegPool.get(i).toString());
                RealRegister tempReg = regAllocator.tempRegPool.get(i);
                mipsCode.add(new MipsInstruction(true, "sw", tempReg.toString(), "$sp", stack.toString()));
                regAllocator.record(tempReg, stack, regAllocator.temRegUseMap[i]);
                regAllocator.freeTempReg(tempReg);
            }
        }
        // 跳转到函数名
        String funcName = callInstruction.value1.name;
        mipsCode.add(new MipsInstruction("jal", funcName));
        // 恢复现场, 这里不需要再次申请寄存器
        for (Triple triple : regAllocator.Recorder) {
            mipsCode.add(new MipsInstruction(true, "lw", triple.getFirst().toString(), "$sp", triple.getSecond().toString()));
            regAllocator.temRegUseMap[((RealRegister) triple.getFirst()).getNum()] = (VirtualRegister) triple.getThird();
        }
        regAllocator.recordClear();
        // 如果有返回值，把$v0的赋值到另外一个寄存器里
        if (callInstruction.result != null) {
            RealRegister tempReg = this.regAllocator.getTempReg(callInstruction.result.getName());
            mipsCode.add(new MipsInstruction("addu", tempReg.toString(), "$zero", "$v0"));
            //把东西存到栈里
            Stack stack = regAllocator.getStackReg(callInstruction.result.getName());
            mipsCode.add(new MipsInstruction(true, "sw", tempReg.toString(), "$sp", stack.toString()));
            //释放寄存器
            regAllocator.freeTempReg(tempReg);
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
        regAllocator.temRegUseMap[tempReg.getNum()] = new VirtualRegister(zextInstruction.result.getName());
    }

    // 对于数组的访问, 由于llvm ir 的特殊结构, 取二维数组要取两次这个就比较麻烦
    private void genGetElementPtr(GetElementPtr getElementPtr) {
        if (getElementPtr.bound2 == null) {
            // 如果是一维数组,直接偏移，二维数组还需要偏移x列数
            RealRegister arrOffset = lookup(getElementPtr.bound1);
            RealRegister array = lookup(getElementPtr.value1);
            ValueType.Type type = getElementPtr.value1.getInnerType();
            if (type != ValueType.i32) {
                String col = Integer.toString(((ValueType.ArrayType) type).size());
                mipsCode.add(new MipsInstruction("mul", arrOffset.toString(), arrOffset.toString(), col));
            }
            mipsCode.add(new MipsInstruction("mul", arrOffset.toString(), arrOffset.toString(), "4"));
            mipsCode.add(new MipsInstruction("addu", array.toString(), array.toString(), arrOffset.toString()));
            // 申请空间把array存起来
            Stack curOffsetStack = regAllocator.getStackReg(getElementPtr.result.getName());
            mipsCode.add(new MipsInstruction(true, "sw", array.toString(), "$sp", curOffsetStack.toString()));
            // 释放寄存器
            regAllocator.freeTempReg(array);
            regAllocator.freeTempReg(arrOffset);
        } else {
            // 一般使用数组的方法，就是使用一开始申请的那个虚拟寄存器
            ValueType.Type type = getElementPtr.value1.getInnerType();
            // 如果是已经降维到了一维数组那么就直接算offset
            if (type.getType() == ValueType.i32) {
                // 首先直接把数组的地址加载进来
                String array = getElementPtr.value1.getName();
                Stack stack = regAllocator.lookUpStack(array);
                // 计算偏移量
                RealRegister curOffset = lookup(getElementPtr.bound2);
                mipsCode.add(new MipsInstruction("mul", curOffset.toString(), curOffset.toString(), "4"));
                if (stack != null) {
                    // 如果是二维数组这个里面装的是地址，所以需要选load出数组地址
                    RealRegister arrayAddr;
                    if (regAllocator.isArrayVirtualReg(array)) {
                        // 首先加载数组地址
                        arrayAddr = regAllocator.getTempReg(array);
                        mipsCode.add(new MipsInstruction(true, "lw", arrayAddr.toString(), "$sp", stack.toString()));
                    } else { // 如果是一维数组地址计算用sp计算就可以
                        arrayAddr = regAllocator.getTempReg(regAllocator.getTempNum());
                        mipsCode.add(new MipsInstruction("addu", arrayAddr.toString(), "$sp", stack.toString()));
                    }
                    // 然后计算偏移量
                    mipsCode.add(new MipsInstruction("addu", curOffset.toString(), arrayAddr.toString(), curOffset.toString()));
                    regAllocator.freeTempReg(arrayAddr);
                } else if (regAllocator.isGlobal(getElementPtr.value1.getName())) {
                    RealRegister tempReg = regAllocator.getTempReg(regAllocator.getTempNum());
                    mipsCode.add(new MipsInstruction("la", tempReg.toString(), getElementPtr.value1.name));
                    mipsCode.add(new MipsInstruction("addu", curOffset.toString(), tempReg.toString(), curOffset.toString()));
                    regAllocator.freeTempReg(tempReg);
                }
                // 申请空间把currentOffset存起来
                Stack curOffsetStack = regAllocator.getStackReg(getElementPtr.result.getName());
                mipsCode.add(new MipsInstruction(true, "sw", curOffset.toString(), "$sp", curOffsetStack.toString()));
                // 释放寄存器
                regAllocator.freeTempReg(curOffset);
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
                if (regAllocator.isGlobal(getElementPtr.value1.getName())) {
                    mipsCode.add(new MipsInstruction("la", tempReg.toString(), getElementPtr.value1.name));
                    mipsCode.add(new MipsInstruction("addu", curOffset.toString(), tempReg.toString(), curOffset.toString()));
                } else {
                    Stack stack = regAllocator.lookUpStack(getElementPtr.value1.getName());
                    mipsCode.add(new MipsInstruction("addu", tempReg.toString(), "$sp", stack.toString()));
                    mipsCode.add(new MipsInstruction("addu", curOffset.toString(), tempReg.toString(), curOffset.toString()));
                }
                // 申请空间把currentOffset存起来
                Stack stack = regAllocator.getStackReg(getElementPtr.result.getName());
                mipsCode.add(new MipsInstruction(true, "sw", curOffset.toString(), "$sp", stack.toString()));
                // 把寄存器free掉
                regAllocator.freeTempReg(tempReg);
                regAllocator.freeTempReg(curOffset);
            }
        }
    }

    private void genInstruction(BaseInstruction instruction) {
        if (!(instruction instanceof AllocateInstruction)) {
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