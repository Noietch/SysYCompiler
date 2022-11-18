package Front.SyntaxAnalyzer.Element;

import java.util.ArrayList;

public class InitVal extends SyntaxNode {
    public ArrayList<SyntaxNode> syntaxNodes;
    public VarType initType;

    public InitVal(ArrayList<SyntaxNode> syntaxNodes, VarType initType) {
        this.syntaxNodes = syntaxNodes;
        this.initType = initType;
        childrenNode.addAll(syntaxNodes);
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        if (initType == VarType.Var) {
            res.append(syntaxNodes.get(0).toString());
        } else {
            res.append("LBRACE {\n");
            for (int i = 0; i < syntaxNodes.size(); i++) {
                res.append(syntaxNodes.get(i).toString());
                if (i < syntaxNodes.size() - 1) res.append("COMMA ,\n");
            }
            res.append("RBRACE }\n");
        }
        res.append("<InitVal>\n");
        return res.toString();
    }
}
