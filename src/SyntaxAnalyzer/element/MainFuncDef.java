package SyntaxAnalyzer.element;

public class MainFuncDef extends SyntaxNode{
    private final Block block;

    public MainFuncDef(Block block) {
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
