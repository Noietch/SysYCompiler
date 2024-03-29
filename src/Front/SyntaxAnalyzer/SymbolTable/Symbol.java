package Front.SyntaxAnalyzer.SymbolTable;

import Front.SyntaxAnalyzer.Element.ConstInitVal;
import Front.SyntaxAnalyzer.Element.FuncFParams;
import Front.SyntaxAnalyzer.Element.FuncType;
import Front.SyntaxAnalyzer.Element.InitVal;

public class Symbol {
    public enum Type {
        void_fuc, int_func, var, oneDimArray, twoDimArray,
    }

    public final String name;
    public ConstInitVal constInitVal;
    public InitVal initVal;
    public boolean isConst;
    public final int stateLine;
    public int paramsNum;

    public Type type;
    public FuncType returnType;
    public FuncFParams funcFParams;

    @Override
    public String toString() {
        return "[Symbol] " + "name: " + name + ", isConst: " + isConst + ", stateLine: " + stateLine + ", type=" + type + "\n";
    }

    public Symbol(String name, int stateLine) {
        this.name = name;
        this.stateLine = stateLine;
        this.isConst = false;
    }

    public void setConst(boolean isConst) {
        this.isConst = isConst;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setInitVal(InitVal initVal) {
        this.initVal = initVal;
    }

    public void setConstInitVal(ConstInitVal constInitVal) {
        this.constInitVal = constInitVal;
    }

    public void setReturnType(FuncType returnType) {
        this.returnType = returnType;
    }

    public void setParamsNum(int paramsNum) {
        this.paramsNum = paramsNum;
    }

    public void setFuncFParams(FuncFParams funcFParams) {
        this.funcFParams = funcFParams;
    }
}