package Front.SyntaxAnalyzer.Element;

import Front.SyntaxAnalyzer.SymbolTable.Symbol;
import Front.SyntaxAnalyzer.SymbolTable.SymbolTable;

public class Exp extends SyntaxNode {
    public final AddExp addExp;

    public Exp(AddExp addExp) {
        this.addExp = addExp;
        childrenNode.add(addExp);
    }

    @Override
    public String toString() {
        StringBuilder res;
        res = new StringBuilder();
        res.append(addExp.toString());
        res.append("<Exp>\n");
        return res.toString();
    }

    public Symbol.Type getType(SymbolTable symbolTable) {
        return addExp.getType(symbolTable);
    }

    public int eval(){
        return addExp.eval();
    }
}
