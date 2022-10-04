package SyntaxAnalyzer.element;

import java.util.ArrayList;

public class ConstDecl extends SyntaxNode {
    private final BType bType;
    private final ArrayList<ConstDef> constDefs;

    public ConstDecl(BType bType, ArrayList<ConstDef> constDefs) {
        this.bType = bType;
        this.constDefs = constDefs;
        childrenNode.add(bType);
        childrenNode.addAll(constDefs);
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        res.append("CONSTTK const\n");
        res.append(bType.toString());
        for (int i = 0; i < constDefs.size(); i++) {
            res.append(constDefs.get(i).toString());
            if (i < constDefs.size() - 1) res.append("COMMA ,\n");
        }
        res.append("SEMICN ;\n");
        res.append("<ConstDecl>\n");
        return res.toString();
    }
}
