package Front.SyntaxAnalyzer.Element;

import Front.SyntaxAnalyzer.SymbolTable.Symbol;
import Front.SyntaxAnalyzer.SymbolTable.SymbolTable;

public class UnaryExp extends SyntaxNode {
    public PrimaryExp primaryExp;
    public Ident ident;
    public FuncRParams funcRParams;
    public UnaryOp unaryOp;
    public UnaryExp unaryExp;

    public UnaryExp(PrimaryExp primaryExp) {
        this.primaryExp = primaryExp;
        childrenNode.add(primaryExp);
    }

    public UnaryExp(Ident ident) {
        this.ident = ident;
    }

    public UnaryExp(Ident ident, FuncRParams funcRParams) {
        this.ident = ident;
        this.funcRParams = funcRParams;
        childrenNode.add(funcRParams);
    }

    public UnaryExp(UnaryOp unaryOp, UnaryExp unaryExp) {
        this.unaryOp = unaryOp;
        this.unaryExp = unaryExp;
        childrenNode.add(unaryOp);
        childrenNode.add(unaryExp);
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        if (primaryExp != null) {
            res.append(primaryExp);
        } else if (ident != null) {
            res.append(ident);
            res.append("LPARENT (\n");
            if (funcRParams != null) {
                res.append(funcRParams);
            }
            res.append("RPARENT )\n");
        } else {
            res.append(unaryOp.toString());
            res.append(unaryExp.toString());
        }
        res.append("<UnaryExp>\n");
        return res.toString();
    }

    public int getNumOfParam() {
        if (funcRParams != null) {
            return funcRParams.getNumOfParam();
        } else return 0;
    }

    public Symbol.Type getType(SymbolTable symbolTable) {
        if (primaryExp != null) return primaryExp.getType(symbolTable);
        else if (ident != null) return symbolTable.getType(ident);
        else return unaryExp.getType(symbolTable);
    }

    public int eval(){
        if(primaryExp != null) return primaryExp.eval();
        else if(ident != null) throw new RuntimeException("func can not eval");
        else {
            if(unaryOp.opType == UnaryOp.Type.PLUS) return unaryExp.eval();
            else return - unaryExp.eval();
        }
    }
}
