package SyntaxAnalyzer.element;

import java.util.ArrayList;

public class VarDecl extends SyntaxNode {
    private final BType bType;
    private final ArrayList<VarDef> varDefs;

    public VarDecl(BType bType, ArrayList<VarDef> varDefs) {
        this.bType = bType;
        this.varDefs = varDefs;
        childrenNode.add(bType);
        childrenNode.addAll(varDefs);
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        res.append(bType.toString());
        for (int i = 0; i < varDefs.size(); i++) {
            res.append(varDefs.get(i).toString());
            if (i < varDefs.size() - 1) res.append("COMMA ,\n");
        }
        res.append("SEMICN ;\n");
        res.append("<VarDecl>\n");
        return res.toString();
    }
}
