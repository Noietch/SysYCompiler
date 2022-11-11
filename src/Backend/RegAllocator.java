package Backend;

import Backend.Mem.RealRegister;
import Backend.Mem.Stack;
import Backend.Mem.VirtualRegister;
import Utils.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;


public class RegAllocator {
    static public String[] RegName = {
            "$zero", "$at", "$v0", "$v1", "$a0", "$a1", "$a2", "$a3",
            "$t0", "$t1", "$t2", "$t3", "$t4", "$t5", "$t6", "$t7", "$s0",
            "$s1", "$s2", "$s3", "$s4", "$s5", "$s6", "$s7", "$t8", "$t9",
            "$k0", "$k1", "$gp", "$sp", "$fp", "$ra"
    };
    public int prevPointer = 0;
    public int stackPointer = 0;
    public HashMap<String, String> virtual2Global = new HashMap<>();
    public HashMap<VirtualRegister, Stack> virtual2Stack = new HashMap<>();
    public ArrayList<Pair> Recorder = new ArrayList<>();
    public ArrayList<RealRegister> tempRegPool = new ArrayList<>();
    public VirtualRegister[] temRegUseMap = new VirtualRegister[RegName.length];
    public int tempNum = 1;

    public void savePointer() {
        prevPointer = stackPointer;
        stackPointer = 0;
    }

    public void resolvePointer() {
        stackPointer = prevPointer;
    }

    public void record(RealRegister realRegister, Stack stack) {
        Recorder.add(new Pair(realRegister, stack));
    }

    public void recordClear() {
        Recorder.clear();
    }

    public void initTempRegPool() {
        for (int i = 0; i < RegName.length; i++) {
            tempRegPool.add(new RealRegister(RegName[i]));
            temRegUseMap[i] = VirtualRegister.None;
        }
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
        temRegUseMap[tempReg.getNum()] = VirtualRegister.None;
    }

    public String getTempNum() {
        return "temp" + tempNum++;
    }

    public RealRegister getTempReg(String virtualNum) {
        // TODO 寄存器分配
        for (int i = 8; i < 28; i++) {
            if (temRegUseMap[i] == VirtualRegister.None) {
                VirtualRegister virtualRegister = new VirtualRegister(virtualNum);
                temRegUseMap[i] = virtualRegister;
                return tempRegPool.get(i);
            }
        }
        throw new RuntimeException();
    }

    public void clear() {
        stackPointer = 0;
        virtual2Stack.clear();
        Arrays.fill(temRegUseMap, VirtualRegister.None);
    }

    public Stack lookUpStack(String virtualNum) {
        return virtual2Stack.getOrDefault(new VirtualRegister(virtualNum), null);
    }

    public boolean lookUpGlobal(String global) {
        return virtual2Global.containsKey(global);
    }

    public RealRegister lookUpTemp(String virtualNum) {
        RealRegister res = null;
        for (int i = 0; i < temRegUseMap.length; i++) {
            if (temRegUseMap[i].name.equals(virtualNum)) {
                res = tempRegPool.get(i);
            }
        }
        return res;
    }

    public VirtualRegister temp2Virtual(int num) {
        return temRegUseMap[num];
    }
}
