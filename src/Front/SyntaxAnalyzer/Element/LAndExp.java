package Front.SyntaxAnalyzer.Element;

import java.util.ArrayList;

public class LAndExp extends SyntaxNode{
    public final ArrayList<EqExp> eqExps;
    public final ArrayList<Ident> unaryOps;

    public LAndExp(ArrayList<EqExp> eqExps, ArrayList<Ident> unaryOps) {
        this.eqExps = eqExps;
        this.unaryOps = unaryOps;
        childrenNode.addAll(eqExps);
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        int idx = 0;
        for(EqExp eqExp:eqExps){
            res.append(eqExp);
            res.append("<LAndExp>\n");
            if(idx<unaryOps.size()) res.append(unaryOps.get(idx));
            idx++;
        }
        return res.toString();
    }
}
