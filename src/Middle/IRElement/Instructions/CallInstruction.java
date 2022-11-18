package Middle.IRElement.Instructions;

import Middle.IRElement.Basic.Function;
import Middle.IRElement.Value;

import java.util.ArrayList;

public class CallInstruction extends BaseInstruction {

    public ArrayList<Value> funcRParams;

    public CallInstruction(Function function, Value res) {
        this.result = res;
        this.value1 = function;
        funcRParams = new ArrayList<>();
    }

    public void addParam(Value value) {
        funcRParams.add(value);
    }


    public void setRes(Value res) {
        this.result = res;
    }

    @Override
    public String toString() {
        Function function = (Function) this.value1;
        StringBuilder res = new StringBuilder();
        if (result != null)
            res.append(this.result.getName()).append(" = call ").append(function.returnType);
        else
            res.append("call ").append(function.returnType);
        res.append(" ").append(function.getName()).append("(");
        for (int i = 0; i < funcRParams.size(); i++) {
            res.append(funcRParams.get(i).getDescriptor());
            if (i != funcRParams.size() - 1) res.append(", ");
        }
        res.append(")");
        return res.toString();
    }
}
