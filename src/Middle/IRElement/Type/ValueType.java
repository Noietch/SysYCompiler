package Middle.IRElement.Type;

public class ValueType {
    public static class Type {
        public DataType dataType;

        public Type() {

        }

        public Type(DataType dataType) {
            this.dataType = dataType;
        }

        @Override
        public String toString() {
            return dataType.toString();
        }

        public String getType() {
            return dataType.toString();
        }
    }

    public static class Pointer extends Type {
        Type type;

        public Pointer(Type type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return type + "*";
        }

        public String getType() {
            return type.getType();
        }
    }

    public static class ArrayType extends Type {
        Type type;
        int size;

        public ArrayType(Type type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return String.format("[%d x %s]", size, type);
        }

        public String getType() {
            return type.getType();
        }
    }
}
