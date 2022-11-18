package Backend.Mem;

public class RealRegister {
    static public String[] RegName = {
            "$zero", "$at", "$v0", "$v1", "$a0", "$a1", "$a2", "$a3",
            "$t0", "$t1", "$t2", "$t3", "$t4", "$t5", "$t6", "$t7", "$s0",
            "$s1", "$s2", "$s3", "$s4", "$s5", "$s6", "$s7", "$t8", "$t9",
            "$k0", "$k1", "$gp", "$sp", "$fp", "$ra"
    };
    String name;

    public RealRegister(String name) {
        this.name = name;
    }

    public int getNum() {
        for (int i = 0; i < RegName.length; i++) {
            if (name.equals(RegName[i])) {
                return i;
            }
        }
        return -1;
    }


    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof RealRegister &&
                this.name.equals(((RealRegister) obj).name);
    }
}