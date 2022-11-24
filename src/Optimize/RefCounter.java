package Optimize;

import Middle.IRElement.Basic.BasicBlock;
import Middle.IRElement.Basic.Constant;
import Middle.IRElement.Basic.Function;
import Middle.IRElement.Instructions.*;

import java.util.HashMap;
import java.util.TreeMap;

public class RefCounter {

    public TreeMap<String, Integer> refCount;
    public HashMap<String, String> useMap;
    public Function function;

    final public int LOOP_FACTOR = 30;

    public RefCounter(Function function) {
        this.function = function;
        refCount = new TreeMap<>();
        useMap = new HashMap<>();
    }

    // 由于llvm ir 中的load指令，我们只对使用内存的变量做引用计数
    // 例如 a = load b , 实际上是对于b的引用计数
    // 这个函数用于映射use信息
    public void mapUse() {
        for (BasicBlock basicBlock : function.basicBlocks) {
            for (BaseInstruction instruction : basicBlock.instructions) {
                if (instruction instanceof LoadInstruction) {
                        useMap.put(instruction.result.getName(), instruction.value1.getName());
                }
                if(instruction instanceof StoreInstruction){
                    if (!(instruction.value1 instanceof Constant)){
                        useMap.put(instruction.value1.getName(), instruction.value2.getName());
                    }
                }
            }
        }
    }

    // 计算引用计数
    public void count() {
        int weight = 1;
        // 构建映射关系
        mapUse();
        // 打印useMap
        System.out.println("useMap:");
        for (String key : useMap.keySet()) {
            System.out.println(key + " -> " + useMap.get(key));
        }
        // 计算引用计数
        for (BasicBlock basicBlock : function.basicBlocks) {
            if (basicBlock.enterLoop) weight *= LOOP_FACTOR;
            if (basicBlock.exitLoop) weight /= LOOP_FACTOR;
            for (BaseInstruction instruction : basicBlock.instructions) {
                boolean isRef = !(instruction instanceof LoadInstruction);
                isRef &= !(instruction instanceof StoreInstruction);
                isRef &= !(instruction instanceof AllocateInstruction);
                isRef &= !(instruction.result instanceof Constant);
                isRef &= !(instruction instanceof BranchInstruction);
                isRef &= !(instruction instanceof IcmpInstruction); // 比较的这个指令用临时寄存器就够了
                isRef &= !(instruction instanceof RetInstruction);
//                if (isRef) {
//                    for(Value value : instruction.getUse()) {
//                        if(value instanceof Constant) continue;
//                        if(value instanceof GlobalVariable) continue;
//                        if(value instanceof Function) continue;
//                        String name = useMap.get(value.getName());
//                        if (refCount.containsKey(name)) {
//                            refCount.put(name, refCount.get(name) + weight);
//                        } else {
//                            refCount.put(name, weight);
//                        }
//                    }
//                }
            }
        }
    }

    // 打印出引用计数的键和值
    public void print() {
        for (String key : refCount.keySet()) {
            System.out.println(key + " " + refCount.get(key));
        }
    }
}
