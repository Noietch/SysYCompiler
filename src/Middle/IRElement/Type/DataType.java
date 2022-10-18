package Middle.IRElement.Type;

public enum DataType{
    i32("i32"), Void("void"), i1("i1");
    public final String name;
    DataType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
