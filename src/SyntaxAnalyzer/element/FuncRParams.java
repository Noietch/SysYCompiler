package SyntaxAnalyzer.element;

import java.util.ArrayList;

public class FuncRParams extends SyntaxNode{
    private ArrayList<Exp> exps;

    public FuncRParams(ArrayList<Exp> exps) {
        this.exps = exps;
        childrenNode.addAll(exps);
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < exps.size(); i++) {
            res.append(exps.get(i).toString());
            if (i < exps.size() - 1) res.append("COMMA ,\n");
        }
        res.append("<FuncRParams>\n");
        return res.toString();
    }
}
