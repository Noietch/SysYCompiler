package Middle;

import Middle.IRElement.Basic.BasicBlock;
import Middle.IRElement.Basic.Function;
import Middle.IRElement.Basic.Module;
import Middle.IRElement.Instructions.*;
import Middle.IRElement.Value;
import Utils.LinkedListNode;

public class IRCodeSwaper {
    public static void swapPrint(Module irModule){
        for (Function function:irModule.functions){
            for (BasicBlock basicBlock : function.basicBlocks) {
                for (BaseInstruction instruction : basicBlock.instructions) {
                    if(instruction instanceof CallInstruction){
                        CallInstruction callInstruction = (CallInstruction) instruction;
                        if(callInstruction.value1.name.equals("putint")){
                            LinkedListNode curInstr = callInstruction.getPrev();
                            Value param = callInstruction.funcRParams.get(0);
                            while (curInstr instanceof BaseInstruction){
                                boolean flag = false;
                                if(curInstr instanceof LoadInstruction){
                                    LoadInstruction loadInstruction = (LoadInstruction) curInstr;
                                    if(loadInstruction.result.getName().equals(param.getName()))
                                        flag = true;
                                }
                                else if(curInstr instanceof CallInstruction){
                                    CallInstruction callInstruction_1 = (CallInstruction) curInstr;
                                    if(callInstruction_1.value2 != null){
                                        if(callInstruction_1.value2.getName().equals(param.getName()))
                                            flag = true;
                                    }
                                }
                                else if(curInstr instanceof BinaryInstruction){
                                    BinaryInstruction binaryInstruction = (BinaryInstruction) curInstr;
                                    if(binaryInstruction.result.getName().equals(param.getName()))
                                        flag = true;
                                }
                                else if(curInstr instanceof GetElementPtr){
                                    GetElementPtr getElementPtr = (GetElementPtr) curInstr;
                                    if(getElementPtr.value1.getName().equals(param.getName()))
                                        flag = true;
                                }
                                if(flag){
                                    basicBlock.instructions.delete((BaseInstruction) curInstr);
                                    basicBlock.instructions.insertBefore(callInstruction, (BaseInstruction) curInstr);
                                    break;
                                }
                                curInstr = curInstr.getPrev();
                            }
                        }
                    }
                }
            }
        }
    }
}
