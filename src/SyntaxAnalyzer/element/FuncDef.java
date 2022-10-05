package SyntaxAnalyzer.element;

public class FuncDef extends SyntaxNode {
    private final FuncType funcType;
    private final Ident ident;
    private final FuncFParams funcFParams;
    private final Block block;

    public FuncDef(FuncType funcType, Ident ident, FuncFParams funcFParams, Block block) {
        this.funcType = funcType;
        this.ident = ident;
        this.funcFParams = funcFParams;
        this.block = block;
        childrenNode.add(funcType);
        childrenNode.add(ident);
        childrenNode.add(funcFParams);
        childrenNode.add(block);
    }

    @Override
    public String toString() {
        StringBuilder res;
        res = new StringBuilder();
        res.append(funcType.toString());
        res.append(ident.toString());
        res.append("LPARENT (\n");
        if(funcFParams!=null) res.append(funcFParams);
        res.append("RPARENT )\n");
        res.append(block.toString());
        res.append("<FuncDef>\n");
        return res.toString();
    }
}
