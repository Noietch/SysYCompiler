package Middle.IRElement.Basic;

import Front.SyntaxAnalyzer.Element.*;
import Middle.IRElement.Type.ValueType;
import Middle.IRElement.Value;

import java.util.ArrayList;

public class GlobalVariable extends Value {
    public boolean isConst;
    public ConstInitVal constInitVal;
    public InitVal initVal;

    public GlobalVariable(String name, boolean isConst, ConstInitVal constInitVal, InitVal initVal) {
        this.name = name;
        this.isConst = isConst;
        this.constInitVal = constInitVal;
        this.initVal = initVal;
        this.isGlobal = true;
    }

    public String getName() {
        return "@" + name;
    }

    public String getConstInitials(ValueType.ArrayType arrayType, ArrayList<SyntaxNode> syntaxNodes) {
        StringBuilder res = new StringBuilder();
        res.append("[");
        for (int i = 0; i < arrayType.size(); i++) {
            res.append(arrayType.getType()).append(" ");
            ConstInitVal temp = (ConstInitVal) syntaxNodes.get(i);
            if (temp.initType == VarType.Var) {
                ConstExp constExp = (ConstExp) temp.syntaxNodes.get(0);
                res.append(constExp.eval());
            } else {
                res.append(getConstInitials((ValueType.ArrayType) arrayType.getType(), temp.syntaxNodes));
            }
            if (i != arrayType.size() - 1) res.append(", ");
        }
        res.append("]");
        return res.toString();
    }

    public String getInitials(ValueType.ArrayType arrayType, ArrayList<SyntaxNode> syntaxNodes) {
        StringBuilder res = new StringBuilder();
        res.append("[");
        for (int i = 0; i < arrayType.size(); i++) {
            res.append(arrayType.getType()).append(" ");
            InitVal temp = (InitVal) syntaxNodes.get(i);
            if (temp.initType == VarType.Var) {
                Exp constExp = (Exp) temp.syntaxNodes.get(0);
                res.append(constExp.eval());
            } else {
                res.append(getInitials((ValueType.ArrayType) arrayType.getType(), temp.syntaxNodes));
            }
            if (i != arrayType.size() - 1) res.append(", ");
        }
        res.append("]");
        return res.toString();
    }

    @Override
    public String toString() {
        if (isConst) {
            if (constInitVal.initType == VarType.Var) {
                ConstExp constExp = (ConstExp) constInitVal.syntaxNodes.get(0);
                return String.format("@%s = dso_local constant %s %s", name, type, constExp.eval());
            } else {
                ValueType.ArrayType arrayType = (ValueType.ArrayType) type.getType();
                StringBuilder res;
                res = new StringBuilder();
                res.append(String.format("@%s = dso_local constant %s ", name, arrayType));
                res.append(getConstInitials(arrayType, constInitVal.syntaxNodes));
                return res.toString();
            }
        } else {
            if (initVal == null) {
                if (type == ValueType.i32)
                    return String.format("@%s = dso_local global %s %s", name, type, "0");
                else {
                    ValueType.ArrayType arrayType = (ValueType.ArrayType) type.getType();
                    return String.format("@%s = dso_local global %s %s", name, arrayType, "zeroinitializer");
                }
            } else if (initVal.initType == VarType.Var) {
                Exp exp = (Exp) initVal.syntaxNodes.get(0);
                return String.format("@%s = dso_local global %s %s", name, type, exp.eval());
            } else {
                ValueType.ArrayType arrayType = (ValueType.ArrayType) type.getType();
                StringBuilder res;
                res = new StringBuilder();
                res.append(String.format("@%s = dso_local global %s ", name, arrayType));
                res.append(getInitials(arrayType, initVal.syntaxNodes));
                return res.toString();
            }
        }
    }
}
