package SyntaxAnalyzer.element;

public class MainFuncDef extends SyntaxNode{
    public Ident ident;
    public final Block block;

    public MainFuncDef(Ident ident,Block block) {
        this.ident = ident;
        this.block = block;
        childrenNode.add(block);
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        res.append("INTTK int\n");
        res.append("MAINTK main\n");
        res.append("LPARENT (\n");
        res.append("RPARENT )\n");
        res.append(block.toString());
        res.append("<MainFuncDef>\n");
        return res.toString();
    }
}
