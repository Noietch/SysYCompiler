package Middle.IRElement;

import Middle.IRElement.ValueType.Function;
import Middle.IRElement.ValueType.GlobalVariable;
import Utils.LinkedList;

public class Module {
    public LinkedList<Function> functions;
    public LinkedList<GlobalVariable> globalVariables;
    public Module() {
        this.functions = new LinkedList<>();
        this.globalVariables = new LinkedList<>();
    }

    public void addFunctions(Function function){
        functions.append(function);
    }

    public void addGlobalVariable(GlobalVariable globalVariable){
        globalVariables.append(globalVariable);
    }


    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        for(Function function:functions){
            res.append(function);
        }
        return res.toString();
    }
}

