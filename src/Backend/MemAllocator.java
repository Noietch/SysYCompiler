package Backend;


import Middle.IRElement.Basic.BasicBlock;
import Middle.IRElement.Basic.Function;
import Middle.IRElement.Instructions.AllocateInstruction;
import Middle.IRElement.Instructions.BaseInstruction;
import Middle.IRElement.Instructions.BinaryInstruction;

// 这个类是用来统计，一个函数内，内存使用量的
public class MemAllocator {

    public Function curFunction;


    public void AllocParam(){
//        // 对于alloc的instruction统计完了统一分配
//        int size = 0;
//        for(BasicBlock basicBlock:function.basicBlocks){
//            for(BaseInstruction instruction:basicBlock.instructions){
//                if(instruction instanceof AllocateInstruction) size += genAllocInstr((AllocateInstruction) instruction);
//                if(instruction instanceof BinaryInstruction) size += 4;
//
//            }
//        }
    }

    public void calAlloc(AllocateInstruction alloc){

    }

}
