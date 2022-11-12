package Middle.IRElement;

public class MipsHelper {
    public enum Type {
        normal,
        init,
        loadParam
    }

    public int loadPos = -1;
    public Type type;

    public MipsHelper(Type type) {
        this.type = type;
    }

    public void setInit() {
        this.type = Type.init;
    }

    public void setLoadParam(int loadPos) {
        this.loadPos = loadPos;
        this.type = Type.loadParam;
    }

    public boolean isInit() {
        return type == Type.init;
    }

    public boolean isLoadParam() {
        return type == Type.loadParam;
    }
}
