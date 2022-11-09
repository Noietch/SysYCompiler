package Backend.Mem;

public class RealRegister {
    String name;

    public RealRegister(String name) {
        this.name = name;
    }

    public int getNum(){
        return name.charAt(2) - '0';
    }

    @Override
    public String toString() {
        return name;
    }
}