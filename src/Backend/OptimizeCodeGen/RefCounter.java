package Backend.OptimizeCodeGen;

import Middle.IRElement.Basic.BasicBlock;
import Middle.IRElement.Basic.Function;
import Middle.IRElement.Instructions.*;
import Middle.IRElement.Type.ValueType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeMap;

public class RefCounter {

    public TreeMap<String, Integer> refCount;
    public Function function;
    public HashSet<String> ArraySet;

    final public int LOOP_FACTOR = 30;

    public RefCounter(Function function) {
        this.function = function;
        refCount = new TreeMap<>();
        ArraySet = new HashSet<>();
    }

    // 由于llvm的指令的特殊性，它使用的是虚拟寄存器，所有的虚拟寄存器都不会跨块引用
    // 全局寄存器的引用计数只是统计占用内存的寄存器
    // 那其实就是对load和store的引用计数
    public void count() {
        int weight = 1;
        for (BasicBlock basicBlock : function.basicBlocks) {
            if(basicBlock.enterLoop) weight *= LOOP_FACTOR;
            if(basicBlock.exitLoop) weight /= LOOP_FACTOR;
            for (BaseInstruction instruction : basicBlock.instructions) {
                if(instruction instanceof AllocateInstruction){
                    ValueType.Type type = instruction.result.getInnerType();
                    // 如果是数组，则计算空间后分配
                    if (type instanceof ValueType.ArrayType) {
                        ArraySet.add(instruction.result.getName());
                    }
                }
                if(instruction instanceof LoadInstruction) {
                    String name = instruction.value1.getName();
                    if(ArraySet.contains(name)) continue;
                    if (refCount.containsKey(name)) {
                        refCount.put(name, refCount.get(name) + weight);
                    } else {
                        refCount.put(name, weight);
                    }
                }
                if(instruction instanceof StoreInstruction){
                    String name = instruction.value2.getName();
                    if(ArraySet.contains(name)) continue;
                    if (refCount.containsKey(name)) {
                        refCount.put(name, refCount.get(name) + weight);
                    } else {
                        refCount.put(name, weight);
                    }
                }
            }
        }
    }

    // 把refCount的值作为关键字排序， 返回值类型为Arraylist
    public ArrayList<String> getSortedRef() {
        // 先引用计数
        count();
        // 再排序
        ArrayList<String> sortedRef = new ArrayList<>();
        refCount.entrySet().stream().sorted((o1, o2) -> o2.getValue() - o1.getValue()).
                forEachOrdered(x -> sortedRef.add(x.getKey()));
        return sortedRef;
    }


    // 打印出引用计数的键和值
    public void print() {
        for (String key : refCount.keySet()) {
            System.out.println(key + " " + refCount.get(key));
        }
    }
}
