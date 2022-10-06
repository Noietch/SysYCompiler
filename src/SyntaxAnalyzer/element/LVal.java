package SyntaxAnalyzer.element;

import java.util.ArrayList;

public class LVal extends SyntaxNode{
    public Ident ident;
    public VarType type;
    public ArrayList<Exp> exps;

    public LVal(Ident ident, VarType type) {
        this.ident = ident;
        this.type = type;
        childrenNode.add(ident);
    }

    public LVal(Ident ident, VarType type, ArrayList<Exp> exps) {
        this.ident = ident;
        this.type = type;
        this.exps = exps;
        childrenNode.add(ident);
        childrenNode.addAll(exps);
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        res.append(ident.toString());
        for(Exp exp:exps){
            res.append("LBRACK [\n");
            res.append(exp.toString());
            res.append("RBRACK ]\n");
        }
        res.append("<LVal>\n");
        return res.toString();
    }

    public String getName(){
        return ident.getValue();
    }
}
