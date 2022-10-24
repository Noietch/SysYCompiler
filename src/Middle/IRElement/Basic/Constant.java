package Middle.IRElement.Basic;

import Middle.IRElement.Type.ValueType;
import Middle.IRElement.Value;

import java.util.ArrayList;

public class Constant extends Value {

    public ArrayList<Integer> dim = new ArrayList<>();
    public ArrayList<Constant> array = new ArrayList<>();

    public Constant(String name) {
        this.type = ValueType.i32;
        this.name = name;
    }

    public void addDim(int dimValue){
        dim.add(dimValue);
    }

    public void addValue(String num){
        array.add(new Constant(num));
    }

    public int getValue() {
        return Integer.parseInt(name);
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
