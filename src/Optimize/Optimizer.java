package Optimize;

import Middle.IRElement.Basic.BasicBlock;
import Middle.IRElement.Basic.Function;
import Middle.IRElement.Basic.Module;
import Middle.IRElement.Instructions.BaseInstruction;
import Middle.IRElement.Instructions.CallInstruction;
import Middle.IRElement.Value;
import Utils.LinkedListNode;

import java.util.ArrayList;
import java.util.Collections;

public class Optimizer {
    Module irModule;

    public Optimizer(Module irModule) {
        this.irModule = irModule;
    }

    public void ReorganizePrintParam(){
        for (Function function:irModule.functions) {
            for (BasicBlock basicBlock : function.basicBlocks) {
                for (BaseInstruction instruction : basicBlock.instructions) {
                    if (instruction instanceof CallInstruction) {
                        CallInstruction callInstruction = (CallInstruction) instruction;
                        if (callInstruction.value1.name.equals("putint")) {
                            optimizeCallInstruction(callInstruction.funcRParams.get(0),callInstruction,basicBlock);
                        }
                    }
                }
            }
        }
    }

    public void optimizeCallInstruction(Value param, BaseInstruction callInstruction, BasicBlock basicBlock){
        LinkedListNode listNode = callInstruction.getPrev();
        while (listNode instanceof BaseInstruction){
            BaseInstruction curInstr = (BaseInstruction) listNode;
            if(curInstr instanceof CallInstruction){
                if(curInstr.result != null && curInstr.result.getName().equals(param.getName())){
                    ArrayList<Value> params = ((CallInstruction) curInstr).funcRParams;
                    Collections.reverse(params);
                    for(Value value:params){
                        optimizeCallInstruction(value,callInstruction,basicBlock);
                    }
                }
            }
            if(curInstr.result != null && curInstr.result.getName().equals(param.getName())){
                basicBlock.instructions.delete(curInstr);
                basicBlock.instructions.insertBefore(callInstruction,curInstr);
                break;
            }
            listNode = listNode.getPrev();
        }
    }
}
