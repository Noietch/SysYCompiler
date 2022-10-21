package Middle.IRElement.Basic;

import Middle.IRElement.Type.ValueType;
import Middle.IRElement.Value;

import java.util.ArrayList;

public class GlobalVariable extends Value {
    public boolean isConst;
    public ArrayList<Object> init;

    public GlobalVariable(String name, boolean isConst, ArrayList<Object> init) {
        this.name = name;
        this.isConst = isConst;
        this.init = init;
        this.isGlobal = true;
    }

    public String getName() {
        return "@" + name;
    }

    @SuppressWarnings("unchecked")
    public String getInitials(ValueType.ArrayType arrayType, ArrayList<Object> init) {
        StringBuilder res = new StringBuilder();
        res.append("[");
        for (int i = 0; i < arrayType.size(); i++) {
            res.append(arrayType.getType()).append(" ");
            Object temp = init.get(i);
            if (((ArrayList<Object>) temp).get(0) instanceof Integer)
                res.append(((ArrayList<Object>) temp).get(0));
            else
                res.append(getInitials((ValueType.ArrayType) arrayType.getType(), (ArrayList<Object>) temp));
            if (i != arrayType.size() - 1) res.append(", ");
        }
        res.append("]");
        return res.toString();
    }

    @Override
    public String toString() {
        if (isConst) {
            if (init.get(0) instanceof Integer) {
                return String.format("@%s = dso_local constant %s %s", name, type.getType(), init.get(0));
            } else {
                ValueType.ArrayType arrayType = (ValueType.ArrayType) type.getType();
                StringBuilder res;
                res = new StringBuilder();
                res.append(String.format("@%s = dso_local constant %s ", name, arrayType));
                res.append(getInitials(arrayType, init));
                return res.toString();
            }
        } else {
            if (init == null) {
                if (type.getType() == ValueType.i32)
                    return String.format("@%s = dso_local global %s %s", name, type.getType(), "0");
                else {
                    ValueType.ArrayType arrayType = (ValueType.ArrayType) type.getType();
                    return String.format("@%s = dso_local global %s %s", name, arrayType, "zeroinitializer");
                }
            } else if (init.get(0) instanceof Integer) {
                return String.format("@%s = dso_local global %s %s", name, type, init.get(0));
            } else {
                ValueType.ArrayType arrayType = (ValueType.ArrayType) type.getType();
                StringBuilder res;
                res = new StringBuilder();
                res.append(String.format("@%s = dso_local global %s ", name, arrayType));
                res.append(getInitials(arrayType, init));
                return res.toString();
            }
        }
    }
}
