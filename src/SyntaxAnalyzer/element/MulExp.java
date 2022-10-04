package SyntaxAnalyzer.element;

import java.util.ArrayList;

public class MulExp extends SyntaxNode{
    private final ArrayList<UnaryExp> unaryExps;
    private final ArrayList<Ident> unaryOps;

    public MulExp(ArrayList<UnaryExp> unaryExps, ArrayList<Ident> unaryOps) {
        this.unaryExps = unaryExps;
        this.unaryOps = unaryOps;
        childrenNode.addAll(unaryExps);
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        int idx = 0;
        for(UnaryExp unaryExp:unaryExps){
            res.append(unaryExp);
            res.append("<MulExp>\n");
            if(idx<unaryOps.size()) res.append(unaryOps.get(idx));
            idx++;
        }
        return res.toString();
    }
}
