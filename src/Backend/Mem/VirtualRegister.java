package Backend.Mem;

public class VirtualRegister {
    public static VirtualRegister None = new VirtualRegister("None");
    public String name;

    public VirtualRegister(String name) {
        this.name = name;
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
        return obj instanceof VirtualRegister &&
                this.name.equals(((VirtualRegister) obj).name);
    }
}
