package SyntaxAnalyzer.element;

import java.util.ArrayList;

public class FuncDef extends SyntaxNode {
    private final FuncType funcType;
    private final Ident ident;
    private final ArrayList<FuncFParams> funcFParams;
    private final Block block;

    public FuncDef(FuncType funcType, Ident ident, ArrayList<FuncFParams> funcFParams, Block block) {
        this.funcType = funcType;
        this.ident = ident;
        this.funcFParams = funcFParams;
        this.block = block;
        childrenNode.add(funcType);
        childrenNode.add(ident);
        childrenNode.addAll(funcFParams);
        childrenNode.add(block);
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        res.append(funcType.toString());
        res.append(ident.toString());
        res.append("LPARENT (\n");
        if (funcFParams.size() > 0)
            res.append(funcFParams.get(0).toString());
        res.append("RPARENT )\n");
        res.append(block.toString());
        res.append("<FuncDef>\n");
        return res.toString();
    }
}
