package Middle.IRElement.ValueType;

import Front.SyntaxAnalyzer.Element.ConstInitVal;
import Front.SyntaxAnalyzer.Element.InitVal;
import Middle.IRElement.Value;

public class GlobalVariable extends Value {
    public boolean isConst;
    public ConstInitVal constInitVal;
    public InitVal initVal;

    public GlobalVariable(String name, boolean isConst, ConstInitVal constInitVal, InitVal initVal) {
        this.name = name;
        this.isConst = isConst;
        this.constInitVal = constInitVal;
        this.initVal = initVal;
    }
}
