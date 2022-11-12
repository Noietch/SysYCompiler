package Backend.Allocator;

import Middle.IRElement.Basic.BasicBlock;
import Middle.IRElement.Basic.Function;
import Middle.IRElement.Instructions.*;
import Middle.IRElement.Type.ValueType;

import java.util.HashMap;

/* 寄存器分配是对于每一个函数来说的，并且对于每一个虚拟寄存器，我们都需要有位置
 * 对于所有的储存设备来说，不管是寄存器还是栈都是用来储存东西的地方，本质上没有区别只有速度上的区别
 * 寄存器我们称为 register 栈我们就叫做 effective memory
 * 这个类和他的名字一样，整个分配我只是使用内存为虚拟寄存器分配位置，真实寄存器不会作为虚拟寄存器存储的地方
 * */

public class OnlyMemAllocator {
    public Function curFunc;
    public Integer StackTop;
    public HashMap<String, Integer> Virtual2Stack = new HashMap<>();

    public OnlyMemAllocator(Function curFunc) {
        this.curFunc = curFunc;
        StackTop = 0;
    }

    /*
     * 寄存器分配器主函数
     * */
    public void calAllocMem() {
        for (BasicBlock basicBlock : curFunc.basicBlocks) {
            for (BaseInstruction instruction : basicBlock.instructions) {
                if (instruction instanceof AllocateInstruction) {

                }
                if (instruction instanceof StoreInstruction) calStore((StoreInstruction) instruction);
                if (instruction instanceof LoadInstruction) calLoad((LoadInstruction) instruction);
                if (instruction instanceof BinaryInstruction) calBinary((BinaryInstruction) instruction);
            }
        }
    }

    // AllocateInstruction 这个就是需要计算所占空间，算完了加上去就结束
    public void calAlloc(AllocateInstruction alloc) {
        ValueType.Type type = alloc.value1.getInnerType();
        this.Virtual2Stack.put(alloc.value1.getName(), StackTop);
        if (type instanceof ValueType.ArrayType)
            StackTop += ((ValueType.ArrayType) type).getTotalSize() * 4;
        else StackTop += 4;
    }

    /*
     * StoreInstruction LoadInstruction 这两个指令是把一个东西存到另外一个东西的位置上
     * 这个store实际上就是把两个虚拟地址映射到同一个地方，不产生新内存
     * 在mips里面没有一位数这种东西，所以calZext其实是做了一个映射
     * 首先查一下地址,然后赋值,不过这两个指令都是临时用一下寄存器
     * store 会销毁寄存器
     * load 会增加寄存器
     */
    public void calStore(StoreInstruction store) {
        int stack = this.Virtual2Stack.get(store.value2.getName());
        this.Virtual2Stack.put(store.value1.getName(), stack);
    }

    public void calLoad(LoadInstruction load) {
        int stack = this.Virtual2Stack.get(load.value1.getName());
        this.Virtual2Stack.put(load.result.getName(), stack);
    }

    public void calZext(ZextInstruction zext){
        int stack = this.Virtual2Stack.get(zext.value1.getName());
        this.Virtual2Stack.put(zext.res.getName(), stack);
    }

    /*
     * 二元表达式实际上用了两个寄存器，所以只会产生一个内存，用来储存计算完成的结果
     * */
    public void calBinary(BinaryInstruction binary) {
        StackTop += 4;
        this.Virtual2Stack.put(binary.result.getName(), StackTop);
    }

    public void calIcmpInstr(IcmpInstruction icmpInstruction) {
        StackTop += 4;
        this.Virtual2Stack.put(icmpInstruction.result.getName(), StackTop);
    }

    /*
     * CallInstruction
     * 需要保存现场，这里需要申请空间
     * 有返回值也需要申请空间
     * */
    public void calCallInstr(CallInstruction callInstruction) {
        // TODO 首先是要保存现场，用了几个寄存器就保存几个,这里先用8个
        StackTop += 4 * 8;
        // 如果有返回值, 就需要申请一个空间放
        if (callInstruction.value2 != null) {
            StackTop += 4;
            this.Virtual2Stack.put(callInstruction.value2.getName(), StackTop);
        }
    }

    /*
    *  GetElementPtr 这个东西不产生新的东西，也就是做映射
    * */
    public void calElementPtr(GetElementPtr getElementPtr){

    }
}
