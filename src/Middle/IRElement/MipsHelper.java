package Middle.IRElement;

public class MipsHelper {
    public enum Type {
        normal,
        init,
    }
    public Type type;

    public MipsHelper(Type type) {
        this.type = type;
    }

    public void setInit() {
        this.type = Type.init;
    }

    public boolean isInit() {
        return type == Type.init;
    }
}
