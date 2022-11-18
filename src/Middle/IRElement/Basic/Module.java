package Middle.IRElement.Basic;

import Utils.LinkedList;

import java.util.ArrayList;

public class Module {
    public ArrayList<Function> functions;
    public LinkedList<GlobalVariable> globalVariables;

    public Module() {
        this.functions = new ArrayList<>();
        this.globalVariables = new LinkedList<>();
    }

    public void addFunctions(Function function) {
        functions.add(function);
    }

    public void addGlobalVariable(GlobalVariable globalVariable) {
        globalVariables.append(globalVariable);
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        for (GlobalVariable globalVariable : globalVariables) {
            res.append(globalVariable).append("\n");
        }
        for (Function function : functions) {
            res.append(function).append("\n");
        }
        return res.toString();
    }
}

