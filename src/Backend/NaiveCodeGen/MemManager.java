package Backend.NaiveCodeGen;

import Backend.Mem.RealRegister;
import Backend.Mem.Stack;
import Backend.Mem.VirtualRegister;
import Utils.Triple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;


public class MemManager {
    static public String[] RegName = {
            "$zero", "$at", "$v0", "$v1", "$a0", "$a1", "$a2", "$a3",
            "$t0", "$t1", "$t2", "$t3", "$t4", "$t5", "$t6", "$t7", "$s0",
            "$s1", "$s2", "$s3", "$s4", "$s5", "$s6", "$s7", "$t8", "$t9",
            "$k0", "$k1", "$gp", "$sp", "$fp", "$ra"
    };
    public int tempNum = 1;
    public int stackPointer = 0;
    public HashSet<String> globalSet = new HashSet<>();
    public HashMap<String, String> virtual2Global = new HashMap<>();
    public HashMap<VirtualRegister, Stack> virtual2Stack = new HashMap<>();
    public ArrayList<Triple> Recorder = new ArrayList<>();
    public ArrayList<RealRegister> tempRegPool = new ArrayList<>();
    public VirtualRegister[] temRegUseMap = new VirtualRegister[RegName.length];
    public HashSet<String> arrayVirtualReg = new HashSet<>();
    public HashSet<String> paramVirtualReg = new HashSet<>();

    public boolean isParam(String name) {
        return paramVirtualReg.contains(name);
    }

    public void addToParam(String name) {
        paramVirtualReg.add(name);
    }

    public boolean isArrayVirtualReg(String name) {
        return arrayVirtualReg.contains(name);
    }

    public void addToArrayVirtualReg(String name) {
        arrayVirtualReg.add(name);
    }

    public void addToGlobal(String virtualRegister, String name) {
        virtual2Global.put(virtualRegister, name);
    }

    public String lookupGlobal(String virtualRegister) {
        if (virtual2Global.containsKey(virtualRegister)) {
            return virtual2Global.get(virtualRegister).substring(1);
        } else return null;
    }

    public void record(RealRegister realRegister, Stack stack, VirtualRegister virtualRegister) {
        Recorder.add(new Triple(realRegister, stack, virtualRegister));
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

    public MemManager() {
        initTempRegPool();
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


    public int getUseTempReg() {
        int res = 0;
        for (int i = 8; i < 18; i++) {
            if (temRegUseMap[i] != VirtualRegister.None) {
                res++;
            }
        }
        return res;
    }

    public RealRegister getTempReg(String virtualNum) {
        // TODO 寄存器分配
        for (int i = 8; i < 18; i++) {
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
        virtual2Global.clear();
        arrayVirtualReg.clear();
        paramVirtualReg.clear();
        Arrays.fill(temRegUseMap, VirtualRegister.None);
    }

    public Stack lookUpStack(String virtualNum) {
        return virtual2Stack.getOrDefault(new VirtualRegister(virtualNum), null);
    }

    public boolean isGlobal(String global) {
        return globalSet.contains(global);
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
}