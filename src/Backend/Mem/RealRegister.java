package Backend.Mem;

public class RealRegister extends MemElem{
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