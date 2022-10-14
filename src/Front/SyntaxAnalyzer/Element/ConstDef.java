package Front.SyntaxAnalyzer.Element;

import java.util.ArrayList;

public class ConstDef extends SyntaxNode {
    public Ident ident;
    public ArrayList<ConstExp> constExps;
    public ConstInitVal constInitVal;

    public ConstDef(Ident ident, ArrayList<ConstExp> constExps, ConstInitVal constInitVal) {
        this.ident = ident;
        this.constExps = constExps;
        this.constInitVal = constInitVal;
        childrenNode.add(ident);
        childrenNode.addAll(constExps);
        childrenNode.add(constInitVal);
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        res.append(ident.toString());
        for (ConstExp constExp : constExps) {
            res.append("LBRACK [\n");
            res.append(constExp.toString());
            res.append("RBRACK ]\n");
        }
        res.append("ASSIGN =\n");
        res.append(constInitVal.toString());
        res.append("<ConstDef>\n");
        return res.toString();
    }
}
