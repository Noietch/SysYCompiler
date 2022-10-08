package SyntaxAnalyzer.element;

import SyntaxAnalyzer.SymbolTable.Symbol;
import SyntaxAnalyzer.SymbolTable.SymbolTable;

import java.util.ArrayList;

public class AddExp extends SyntaxNode {
    public final ArrayList<MulExp> mulExps;
    public final ArrayList<Ident> unaryOps;

    public AddExp(ArrayList<MulExp> mulExps, ArrayList<Ident> unaryOps) {
        this.mulExps = mulExps;
        this.unaryOps = unaryOps;
        childrenNode.addAll(mulExps);
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        int idx = 0;
        for (MulExp mulExp : mulExps) {
            res.append(mulExp);
            res.append("<AddExp>\n");
            if (idx < unaryOps.size()) res.append(unaryOps.get(idx));
            idx++;
        }
        return res.toString();
    }

    public Symbol.Type getType(SymbolTable symbolTable) {
        return mulExps.get(0).getType(symbolTable);
    }
}