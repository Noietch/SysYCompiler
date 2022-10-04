package SyntaxAnalyzer.element;

import java.util.ArrayList;

public class VarDef extends SyntaxNode{
    private final Ident ident;
    private final ArrayList<ConstExp> constExps;
    private InitVal initVal;

    public VarDef(Ident ident, ArrayList<ConstExp> constExps, InitVal initVal) {
        this.ident = ident;
        this.constExps = constExps;
        this.initVal = initVal;
        childrenNode.add(ident);
        childrenNode.addAll(constExps);
        childrenNode.add(initVal);
    }

    public VarDef(Ident ident, ArrayList<ConstExp> constExps) {
        this.ident = ident;
        this.constExps = constExps;
        childrenNode.add(ident);
        childrenNode.addAll(constExps);
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        res.append(ident.toString());
        for(ConstExp constExp:constExps){
            res.append("LBRACK [\n");
            res.append(constExp.toString());
            res.append("RBRACK ]\n");
        }
        if(initVal != null){
            res.append("ASSIGN =\n");
            res.append(initVal);
        }
        res.append("<VarDef>\n");
        return res.toString();
    }
}
