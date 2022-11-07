package Backend;

import java.util.HashMap;

public class RegAllocator {
    public int stackPointer = 0;
    public HashMap<String, Integer> virtual2Stack = new HashMap<>();
    public HashMap<String, String> virtual2Temp = new HashMap<>();

    public String[] tempRegPool = {"$t0", "$t1", "$t2", "$t3"};

    public int[] temRegUseMap = {0, 0, 0, 0};

    public RegAllocator() {

    }

    public int getStackReg(){
        return stackPointer++;
    }
    public void getStackReg(String virtualNum) {
        virtual2Stack.put(virtualNum, stackPointer++);
    }

    public void freeTempReg(String virtual, String tempReg) {
        virtual2Temp.remove(virtual);
        int num = tempReg.charAt(2) - '0';
        temRegUseMap[num] = 0;
    }

    public String getTempReg(String virtualNum) {
        for (int i = 0; i < tempRegPool.length; i++) {
            if (temRegUseMap[i] == 0) {
                virtual2Temp.put(virtualNum, tempRegPool[i]);
                temRegUseMap[i] = 1;
                return tempRegPool[i];
            }
        }
        throw new RuntimeException();
    }

    public String clearStack() {
        int res = stackPointer;
        stackPointer = 0;
        virtual2Stack.clear();
        return Integer.toString(res * 4);
    }

    public String lookUpStack(String virtualNum) {
        if (!virtual2Stack.containsKey(virtualNum)) return null;
        int stackPos = virtual2Stack.get(virtualNum);
        return Integer.toString((stackPointer - stackPos - 1) * 4);
    }

    public String lookUpTemp(String virtualNum) {
        if (!virtual2Temp.containsKey(virtualNum)) return null;
        return virtual2Temp.get(virtualNum);
    }
}
