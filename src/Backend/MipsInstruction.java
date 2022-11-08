package Backend;

public class MipsInstruction {
    public boolean isMem;
    public String Label;
    public String instr;

    public String reg1;
    public String reg2;
    public String reg3;
    public String offset;

    public MipsInstruction(String label) {
        this.Label = label;
    }

    public MipsInstruction(boolean isMem, String instr, String reg1, String reg2, String offset) {
        this.isMem = isMem;
        this.instr = instr;
        this.reg1 = reg1;
        this.reg2 = reg2;
        this.offset = offset;
    }

    public MipsInstruction(String instr, String reg1, String reg2, String reg3) {
        this.instr = instr;
        this.reg1 = reg1;
        this.reg2 = reg2;
        this.reg3 = reg3;
    }

    public MipsInstruction(String instr, String reg1, String reg2) {
        this.instr = instr;
        this.reg1 = reg1;
        this.reg2 = reg2;
    }

    public MipsInstruction(String instr, String reg1) {
        this.instr = instr;
        this.reg1 = reg1;
    }

    @Override
    public String toString() {
        // 如果是个标志
        if (Label != null) return Label + ":";
        // 如果操作内存
        if (isMem) return "    " + instr + " " + reg1 + " ," + offset + "(" + reg2 + ")";
        // 三个操作数
        if (reg3 != null) return "    " + instr + " " + reg1 + " ," + reg2 + " ," + reg3;
        // 两个操作数
        if (reg2 != null) return "    " + instr + " " + reg1 + " ," + reg2;
        // 一个操作数
        return "    " + instr + " " + reg1;
    }
}