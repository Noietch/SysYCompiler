package Backend.Allocator;

import Middle.IRElement.Basic.BasicBlock;
import Middle.IRElement.Basic.Function;
import Middle.IRElement.Instructions.*;
import Middle.IRElement.Type.ValueType;

import java.util.HashMap;

/* �Ĵ��������Ƕ���ÿһ��������˵�ģ����Ҷ���ÿһ������Ĵ��������Ƕ���Ҫ��λ��
 * �������еĴ����豸��˵�������ǼĴ�������ջ�����������涫���ĵط���������û������ֻ���ٶ��ϵ�����
 * �Ĵ������ǳ�Ϊ register ջ���Ǿͽ��� effective memory
 * ��������������һ��������������ֻ��ʹ���ڴ�Ϊ����Ĵ�������λ�ã���ʵ�Ĵ���������Ϊ����Ĵ����洢�ĵط�
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
     * �Ĵ���������������
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

    // AllocateInstruction ���������Ҫ������ռ�ռ䣬�����˼���ȥ�ͽ���
    public void calAlloc(AllocateInstruction alloc) {
        ValueType.Type type = alloc.value1.getInnerType();
        this.Virtual2Stack.put(alloc.value1.getName(), StackTop);
        if (type instanceof ValueType.ArrayType)
            StackTop += ((ValueType.ArrayType) type).getTotalSize() * 4;
        else StackTop += 4;
    }

    /*
     * StoreInstruction LoadInstruction ������ָ���ǰ�һ�������浽����һ��������λ����
     * ���storeʵ���Ͼ��ǰ����������ַӳ�䵽ͬһ���ط������������ڴ�
     * ��mips����û��һλ�����ֶ���������calZext��ʵ������һ��ӳ��
     * ���Ȳ�һ�µ�ַ,Ȼ��ֵ,����������ָ�����ʱ��һ�¼Ĵ���
     * store �����ټĴ���
     * load �����ӼĴ���
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
     * ��Ԫ���ʽʵ�������������Ĵ���������ֻ�����һ���ڴ棬�������������ɵĽ��
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
     * ��Ҫ�����ֳ���������Ҫ����ռ�
     * �з���ֵҲ��Ҫ����ռ�
     * */
    public void calCallInstr(CallInstruction callInstruction) {
        // TODO ������Ҫ�����ֳ������˼����Ĵ����ͱ��漸��,��������8��
        StackTop += 4 * 8;
        // ����з���ֵ, ����Ҫ����һ���ռ��
        if (callInstruction.value2 != null) {
            StackTop += 4;
            this.Virtual2Stack.put(callInstruction.value2.getName(), StackTop);
        }
    }

    /*
    *  GetElementPtr ��������������µĶ�����Ҳ������ӳ��
    * */
    public void calElementPtr(GetElementPtr getElementPtr){

    }
}
