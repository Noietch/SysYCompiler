package Backend;

import Backend.Mem.MemElem;
import Backend.Mem.RealRegister;
import Backend.Mem.Stack;
import Backend.Mem.VirtualRegister;

import java.util.ArrayList;
import java.util.HashMap;

public class RegAllocator {
    public int stackPointer = 0;
    public HashMap<VirtualRegister, MemElem> virtual2Stack = new HashMap<>();
    public HashMap<VirtualRegister, RealRegister> virtual2Temp = new HashMap<>();
    public ArrayList<RealRegister> tempRegPool = new ArrayList<>();
    public int[] temRegUseMap;

    public void initTempRegPool() {
        for (int i = 0; i < 4; i++)
            tempRegPool.add(new RealRegister("$t" + i));
        temRegUseMap = new int[tempRegPool.size()];
    }

    public RegAllocator() {
        initTempRegPool();
    }

    public String getCurStack() {
        return Integer.toString(stackPointer * 4);
    }

    public void getStackReg() {
        stackPointer++;
    }

    public void repeatReg(String virtualNum, int memSize) {
        virtual2Stack.put(new VirtualRegister(virtualNum), new Stack(stackPointer - memSize));
    }

    public void getStackReg(String virtualNum, int memSize) {
        virtual2Stack.put(new VirtualRegister(virtualNum), new Stack(stackPointer));
        stackPointer += memSize;
    }

    public void getStackReg(String virtualNum) {
        virtual2Stack.put(new VirtualRegister(virtualNum), new Stack(stackPointer++));
    }

    public void freeTempReg(RealRegister tempReg) {
        int num = tempReg.toString().charAt(2) - '0';
        temRegUseMap[num] = 0;
    }

    public RealRegister getTempReg(String virtualNum) {
        for (int i = 0; i < temRegUseMap.length; i++) {
            if (temRegUseMap[i] == 0) {
                virtual2Temp.put(new VirtualRegister(virtualNum), tempRegPool.get(i));
                temRegUseMap[i] = 1;
                return tempRegPool.get(i);
            }
        }
        throw new RuntimeException();
    }

    public void clear() {
        stackPointer = 0;
        virtual2Stack.clear();
        virtual2Temp.clear();
    }

    public MemElem lookUpStackInv(String virtualNum) {
        if (!virtual2Stack.containsKey(new VirtualRegister(virtualNum)))
            throw new RuntimeException("no the virtual num");
        return virtual2Stack.get(new VirtualRegister(virtualNum));
    }

    public MemElem lookUpStack(String virtualNum) {
        if (!virtual2Stack.containsKey(new VirtualRegister(virtualNum))) return null;
        MemElem memElem = virtual2Stack.get(new VirtualRegister(virtualNum));
        if (memElem instanceof Stack) {
            Stack stack = (Stack) memElem;
            return new Stack(stackPointer - stack.stackPos - 1);
        } else return memElem;
    }

    public RealRegister lookUpTemp(String virtualNum) {
        if (!virtual2Temp.containsKey(new VirtualRegister(virtualNum))) return null;
        RealRegister realRegister = virtual2Temp.get(new VirtualRegister(virtualNum));
        // 暂时的方案
        if (virtualNum.charAt(1) == 't' && temRegUseMap[realRegister.getNum()] == 0) return null;
        return realRegister;
    }
}
