package Backend;


import Middle.IRElement.Basic.BasicBlock;
import Middle.IRElement.Basic.Function;
import Middle.IRElement.Instructions.AllocateInstruction;
import Middle.IRElement.Instructions.BaseInstruction;
import Middle.IRElement.Instructions.BinaryInstruction;

// �����������ͳ�ƣ�һ�������ڣ��ڴ�ʹ������
public class MemAllocator {

    public Function curFunction;


    public void AllocParam(){
//        // ����alloc��instructionͳ������ͳһ����
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
