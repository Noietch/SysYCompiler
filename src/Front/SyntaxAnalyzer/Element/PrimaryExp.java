package Front.SyntaxAnalyzer.Element;

import Front.SyntaxAnalyzer.SymbolTable.Symbol;
import Front.SyntaxAnalyzer.SymbolTable.SymbolTable;

public class PrimaryExp extends SyntaxNode {
    public Exp exp;
    public LVal lVal;
    public Number number;

    public boolean isExp;

    public PrimaryExp(Exp exp) {
        this.exp = exp;
        this.isExp = true;
        childrenNode.add(exp);
    }

    public PrimaryExp(Number number) {
        this.number = number;
        this.isExp = false;
        childrenNode.add(number);
    }

    public PrimaryExp(LVal lVal) {
        this.lVal = lVal;
        this.isExp = false;
        childrenNode.add(lVal);
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        if (exp != null) {
            res.append("LPARENT (\n");
            res.append(exp);
            res.append("RPARENT )\n");
        }
        if (lVal != null) {
            res.append(lVal);
        }
        if (number != null) {
            res.append(number);
        }
        res.append("<PrimaryExp>\n");
        return res.toString();
    }

    public Symbol.Type getType(SymbolTable symbolTable) {
        if (number != null) return Symbol.Type.var;
        else if (lVal != null) return lVal.getType(symbolTable);
        else return exp.getType(symbolTable);
    }

    public String getNumber(){
        return number.intConst.token.value;
    }
}