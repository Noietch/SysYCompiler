package Front.SyntaxAnalyzer.Element;

import java.util.ArrayList;

public class EqExp extends SyntaxNode{
    public final ArrayList<RelExp> relExps;
    public final ArrayList<Ident> unaryOps;

    public EqExp(ArrayList<RelExp> relExps, ArrayList<Ident> unaryOps) {
        this.relExps = relExps;
        this.unaryOps = unaryOps;
        childrenNode.addAll(relExps);
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        int idx = 0;
        for(RelExp relExp:relExps){
            res.append(relExp);
            res.append("<EqExp>\n");
            if(idx<unaryOps.size()) res.append(unaryOps.get(idx));
            idx++;
        }
        return res.toString();
    }
}
