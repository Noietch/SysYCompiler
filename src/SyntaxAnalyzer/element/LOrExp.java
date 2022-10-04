package SyntaxAnalyzer.element;

import java.util.ArrayList;

public class LOrExp extends SyntaxNode{
    private final ArrayList<LAndExp> lAndExps;
    private final ArrayList<Ident> unaryOps;
    public LOrExp(ArrayList<LAndExp> lAndExps, ArrayList<Ident> unaryOps) {
        this.lAndExps = lAndExps;
        this.unaryOps = unaryOps;
        childrenNode.addAll(lAndExps);
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        int idx = 0;
        for(LAndExp lAndExp:lAndExps){
            res.append(lAndExp);
            res.append("<LOrExp>\n");
            if(idx<unaryOps.size()) res.append(unaryOps.get(idx));
            idx++;
        }
        return res.toString();
    }
}
