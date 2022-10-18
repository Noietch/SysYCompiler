package Middle.IRElement.Basic;

import Front.SyntaxAnalyzer.Element.*;
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

    @Override
    public String toString() {
        if (isConst) {
            if (constInitVal.initType == VarType.Var) {
                ConstExp constExp = (ConstExp) constInitVal.syntaxNodes.get(0);
                return "@" + name + " = dso_local constant i32 " + constExp.eval();
            }
        } else {
            if (initVal == null) {
                return "@" + name + " = common dso_local global i32 0";
            }
            if (initVal.initType == VarType.Var) {
                Exp exp = (Exp) initVal.syntaxNodes.get(0);
                return "@" + name + " = dso_local global i32 " + exp.eval();
            }
        }
        throw new RuntimeException("NOT IMPLEMENT");
    }
}
