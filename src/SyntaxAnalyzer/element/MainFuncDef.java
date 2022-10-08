package SyntaxAnalyzer.element;

public class MainFuncDef extends SyntaxNode{
    public Ident ident;
    public final Block block;
    public int endLine;
    public MainFuncDef(Ident ident,Block block,int endLine) {
        this.ident = ident;
        this.block = block;
        this.endLine = endLine;
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

    public boolean isReturn() {
        int size = block.blockItems.size();
        if(size >= 1){
            BlockItem blockItem = block.blockItems.get(size - 1);
            Stmt stmt = blockItem.getStmt();
            if (stmt != null)
                return stmt.getType() == Stmt.Type.Return;
        }
        return false;
    }
}
