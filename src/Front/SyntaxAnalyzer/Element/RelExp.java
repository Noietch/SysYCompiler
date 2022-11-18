package Front.SyntaxAnalyzer.Element;

import java.util.ArrayList;

public class RelExp extends SyntaxNode{
    public ArrayList<AddExp> addExps;
    public ArrayList<Ident> unaryOps;
    public RelExp(ArrayList<AddExp> addExps, ArrayList<Ident> unaryOps) {
        this.addExps = addExps;
        this.unaryOps = unaryOps;
        childrenNode.addAll(addExps);
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        int idx = 0;
        for(AddExp addExp:addExps){
            res.append(addExp);
            res.append("<RelExp>\n");
            if(idx<unaryOps.size()) res.append(unaryOps.get(idx));
            idx++;
        }
        return res.toString();
    }
}
