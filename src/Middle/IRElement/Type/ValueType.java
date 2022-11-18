package Middle.IRElement.Type;

import java.util.ArrayList;

public class ValueType {

    public static Type i32 = new Type(DataType.i32);
    public static Type i1 = new Type(DataType.i1);

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

        public Type getType() {
            return this;
        }

        public ArrayList<Integer> getDim(){
            if(this instanceof ArrayType)
                return this.getDim();
            else throw new RuntimeException("[getDim] NOT ARRAY");
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

        public Type getType() {
            return type;
        }

    }

    public static class ArrayType extends Type {
        Type type;
        int size;

        public ArrayType(int size, Type type) {
            this.size = size;
            this.type = type;
        }

        public int size(){
            return size;
        }

        public ArrayList<Integer> getDim() {
            Type temp = this;
            ArrayList<Integer> res = new ArrayList<>();
            while(temp instanceof ArrayType){
                res.add(((ArrayType) temp).size);
                temp = ((ArrayType) temp).type;
            }
            return res;
        }

        public int getTotalSize(){
            Type temp = this;
            int size = 1;
            while(temp instanceof ArrayType){
                size *= ((ArrayType) temp).size;
                temp = ((ArrayType) temp).type;
            }
            return size;
        }

        @Override
        public String toString() {
            return String.format("[%d x %s]", size, type);
        }

        public Type getType() {
            return type;
        }

    }
}
