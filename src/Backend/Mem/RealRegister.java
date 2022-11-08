package Backend.Mem;

public class RealRegister {
    String name;

    public RealRegister(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
