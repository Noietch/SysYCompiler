package Optimize;

import Middle.IRElement.Basic.*;
import Middle.IRElement.Instructions.BaseInstruction;
import Middle.IRElement.Instructions.BinaryInstruction;
import Middle.IRElement.Instructions.BranchInstruction;
import Middle.IRElement.Instructions.CallInstruction;
import Middle.IRElement.Value;

public class Optimizer {
    Module irModule;

    public Optimizer(Module irModule) {
        this.irModule = irModule;
    }

    // 单个指令优化
    public void instructionOptimize() {
        binaryConstantSwap();
        uselessCodeElimination();
    }


    // 如果二元运算且是加法或者是乘法有一个是常数，那么就把常数放在右边
    public void binaryConstantSwap() {
        for (Function function : irModule.functions) {
            if (function.define) {
                for (BasicBlock basicBlock : function.basicBlocks) {
                    for (BaseInstruction instruction : basicBlock.instructions) {
                        if (instruction instanceof BinaryInstruction) {
                            BinaryInstruction binaryInstruction = (BinaryInstruction) instruction;
                            if (binaryInstruction.op.type.equals(Op.Type.add) || binaryInstruction.op.type.equals(Op.Type.mul)) {
                                if (binaryInstruction.value1 instanceof Constant && !(binaryInstruction.value2 instanceof Constant)) {
                                    Value temp = binaryInstruction.value1;
                                    binaryInstruction.value1 = binaryInstruction.value2;
                                    binaryInstruction.value2 = temp;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // 跳转缩减,如果跳转的目标基本块中只有一条指令,并且是无条件跳转,则删除目标基本块,并且把跳转指令的目标改为目标基本块的目标
    // 这个优化有问题，问题在于有可能需要递归修改，但是在竞速样例里面没问题，所以先这样吧
    // 经过测试，本优化没有在测试样例里面起作用，所以先注释掉
    public void delBlock(BasicBlock basicBlock) {
        Function parentFunction = basicBlock.parent;
        parentFunction.basicBlocks.delete(basicBlock);
    }

    public void jumpReduce() {
        for (Function function : irModule.functions) {
            if (function.define) {
                for (BasicBlock basicBlock : function.basicBlocks) {
                    BaseInstruction terminator = basicBlock.instructions.getTail();
                    if (terminator instanceof BranchInstruction) {
                        BranchInstruction branchInstruction = (BranchInstruction) terminator;
                        BasicBlock trueBlock = (BasicBlock) branchInstruction.value1;
                        BaseInstruction trueTerminator = trueBlock.instructions.getHead();
                        if (trueTerminator instanceof BranchInstruction) {
                            BranchInstruction trueBranchInstruction = (BranchInstruction) trueTerminator;
                            if (trueBranchInstruction.cond == null) {
                                branchInstruction.value1 = trueBranchInstruction.value1;
                                delBlock(trueBlock);
                            }
                        }
                        if (branchInstruction.value2 != null) {
                            BasicBlock falseBlock = (BasicBlock) branchInstruction.value2;
                            BaseInstruction falseTerminator = falseBlock.instructions.getHead();
                            if (falseTerminator instanceof BranchInstruction) {
                                BranchInstruction falseBranchInstruction = (BranchInstruction) falseTerminator;
                                if (falseBranchInstruction.cond == null) {
                                    branchInstruction.value2 = falseBranchInstruction.value1;
                                    delBlock(falseBlock);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // 输入一条指令和一个函数，判断从这条指令开始，这个条指令的def集，后续是否被使用
    // 虽然llvm中除了站内存的虚拟寄存器之外都不会跨快使用，但是这里还是考虑了跨快使用的情况比如定义了，就没用过。
    private boolean isUseful(Function function, BaseInstruction startInstruction) {
        boolean isUseful = false;
        boolean start = false;
        if(startInstruction.getDef().size() == 0) return true;
        for (BasicBlock basicBlock : function.basicBlocks) {
            for (BaseInstruction instruction : basicBlock.instructions) {
                if(!start) start = instruction == startInstruction;
                if (start) {
                    Value def = startInstruction.getDef().get(0);
                    isUseful |= instruction.getUse().contains(def);
                }
            }
        }
        return isUseful;
    }

    // 检查整个函数是否还存在无用代码,如果存在则删除
    private int isUseful(Function function) {
        int uselessCount = 0;
        for (BasicBlock basicBlock : function.basicBlocks) {
            for (BaseInstruction instruction : basicBlock.instructions) {
                if (!isUseful(function, instruction)) {
                    uselessCount++;
                    // 如果是call指令，可能返回值没用，但是他本身是有用的，所以不能删除
                    if(instruction instanceof CallInstruction){
                        instruction.result = null;
                    }
                    else basicBlock.instructions.delete(instruction);
                }
            }
        }
        return uselessCount;
    }

    // 删除无用代码
    public void uselessCodeElimination() {
        while (true) {
            int uselessCount = 0;
            for (Function function : irModule.functions) {
                if (function.define) {
                    uselessCount += isUseful(function);
                }
            }
            if (uselessCount == 0) break;
        }
    }
}
