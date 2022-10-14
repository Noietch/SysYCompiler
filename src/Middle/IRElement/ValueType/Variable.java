package Middle.IRElement.ValueType;

import Front.SyntaxAnalyzer.Element.ConstInitVal;
import Front.SyntaxAnalyzer.Element.InitVal;
import Middle.IRElement.Value;

public class Variable extends Value {
    public boolean isConst;
    public ConstInitVal constInitVal;
    public InitVal initVal;

    public Variable(String name) {
        this.name = name;
    }
}
