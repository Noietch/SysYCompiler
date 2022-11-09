package Backend;

import Backend.Mem.RealRegister;
import Backend.Mem.Stack;
import Backend.Mem.VirtualRegister;

import java.util.ArrayList;
import java.util.HashMap;

public class RegAllocator {

    public int stackPointer = 0;
    public HashMap<VirtualRegister, Stack> virtual2Stack = new HashMap<>();
    public HashMap<VirtualRegister, RealRegister> virtual2Temp = new HashMap<>();
    public ArrayList<RealRegister> tempRegPool = new ArrayList<>();
    public int[] temRegUseMap;

    public void initTempRegPool() {
        for (int i = 0; i < 5; i++)
            tempRegPool.add(new RealRegister("$t" + i));
        temRegUseMap = new int[tempRegPool.size()];
    }

    public RegAllocator() {
        initTempRegPool();
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

    public Stack getStackReg(String virtualNum) {
        Stack res = new Stack(stackPointer);
        virtual2Stack.put(new VirtualRegister(virtualNum), new Stack(stackPointer++));
        return res;
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


    public Stack lookUpStack(String virtualNum) {
        return virtual2Stack.getOrDefault(new VirtualRegister(virtualNum), null);
    }

    public RealRegister lookUpTemp(String virtualNum) {
        if (!virtual2Temp.containsKey(new VirtualRegister(virtualNum))) return null;
        RealRegister realRegister = virtual2Temp.get(new VirtualRegister(virtualNum));
        // 暂时的方案
        if (realRegister.toString().charAt(1) == 't' && temRegUseMap[realRegister.getNum()] == 0) return null;
        return realRegister;
    }
}
