package Backend;

import Backend.Mem.RealRegister;
import Backend.Mem.VirtualRegister;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RegAllocator {
    public int stackPointer = 0;
    public HashMap<VirtualRegister, Integer> virtual2Stack = new HashMap<>();
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

    public void getStackReg(String virtualNum) {
        virtual2Stack.put(new VirtualRegister(virtualNum), stackPointer++);
    }

    public void freeTempReg(String virtual, RealRegister tempReg) {
        virtual2Temp.remove(new VirtualRegister(virtual));
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

    public void clearStack() {
        stackPointer = 0;
        virtual2Stack.clear();
    }

    public String lookUpStack(String virtualNum) {
        if (!virtual2Stack.containsKey(new VirtualRegister(virtualNum))) return null;
        int stackPos = virtual2Stack.get(new VirtualRegister(virtualNum));
        return Integer.toString((stackPointer - stackPos - 1) * 4);
    }

    public RealRegister lookUpTemp(String virtualNum) {
        if (!virtual2Temp.containsKey(new VirtualRegister(virtualNum))) return null;
        return virtual2Temp.get(new VirtualRegister(virtualNum));
    }
}
