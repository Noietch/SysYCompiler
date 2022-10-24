package Middle.IRElement;

import Middle.IRElement.Basic.Constant;
import Middle.IRElement.Type.ValueType;
import Utils.LinkedList;
import Utils.LinkedListNode;

import java.util.ArrayList;

public class Value extends LinkedListNode {
    public String name;
    public LinkedList<Use> uses;

    public ValueType.Type type;

    public boolean isConst = false;
    public boolean isGlobal = false;

    // ====================== 对于局部常量数组或者是全局数组需要对索引进行数字化 ============================
    public ArrayList<Constant> array = new ArrayList<>();

    public Value() {
    }

    public Value(String name) {
        this.name = name;
        this.uses = new LinkedList<>();
    }

    public Value(String name, ValueType.Type type) {
        this.name = name;
        this.type = type;
        this.uses = new LinkedList<>();
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(ValueType.Type type) {
        this.type = type;
    }

    public ValueType.Type getInnerType() {
        return this.type.getType();
    }

    public ValueType.Type getType() {
        return this.type;
    }

    public String getName() {
        return "%" + name;
    }

    public String getDescriptor() {
        return String.format("%s %s", this.type, getName());
    }

    @Override
    public String toString() {
        return this.type.toString();
    }

    // ====================== 添加数组常量 ============================


    public void setConst() {
        isConst = true;
    }

    public void addValue(ArrayList<Constant> constants) {
        array.addAll(constants);
    }

    public void addValue(Constant constant) {
        array.add(constant);
    }

    public Constant getValue(int pos) {
        return array.get(pos);
    }

}