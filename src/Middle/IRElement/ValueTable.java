package Middle.IRElement;
import java.util.ArrayList;
import java.util.HashMap;

public class ValueTable {
    public HashMap<String,Value> valueHashMap;
    public ValueTable father;
    public ArrayList<ValueTable> children;


    public ValueTable(ValueTable father) {
        this.valueHashMap = new HashMap<>();
        this.children = new ArrayList<>();
        this.father = father;
    }

    public ValueTable newSon() {
        ValueTable son = new ValueTable(this);
        this.children.add(son);
        return son;
    }

    public ValueTable back(){
        return this.father;
    }


    public void addValue(String name,Value value){
        valueHashMap.put(name,value);
    }


    public Value getRegister(String name){
        ValueTable cur = this;
        while(cur != null){
            if(cur.valueHashMap.containsKey(name))
                return cur.valueHashMap.get(name);
            cur = cur.father;
        }
        throw new RuntimeException(String.format("%s NOT FOUND", name));
    }
}
