package Front.SyntaxAnalyzer.Element;

import java.util.ArrayList;

public class FuncFParams extends SyntaxNode {
    public ArrayList<FuncFParam> funcFParams;

    public FuncFParams(ArrayList<FuncFParam> funcFParams) {
        this.funcFParams = funcFParams;
        childrenNode.addAll(funcFParams);
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < funcFParams.size(); i++) {
            res.append(funcFParams.get(i).toString());
            if (i < funcFParams.size() - 1) res.append("COMMA ,\n");
        }
        res.append("<FuncFParams>\n");
        return res.toString();
    }
}
