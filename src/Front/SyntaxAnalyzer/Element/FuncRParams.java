package Front.SyntaxAnalyzer.Element;

import Front.SyntaxAnalyzer.SymbolTable.Symbol;
import Front.SyntaxAnalyzer.SymbolTable.SymbolTable;

import java.util.ArrayList;

public class FuncRParams extends SyntaxNode {
    public final ArrayList<Exp> exps;

    public FuncRParams(ArrayList<Exp> exps) {
        this.exps = exps;
        childrenNode.addAll(exps);
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < exps.size(); i++) {
            res.append(exps.get(i).toString());
            if (i < exps.size() - 1) res.append("COMMA ,\n");
        }
        res.append("<FuncRParams>\n");
        return res.toString();
    }

    public int getNumOfParam() {
        return exps.size();
    }

    public ArrayList<Symbol.Type> getParamType(SymbolTable symbolTable){
        ArrayList<Symbol.Type> res = new ArrayList<>();
        for(Exp exp:exps){
            res.add(exp.getType(symbolTable));
        }
        return res;
    }
}
