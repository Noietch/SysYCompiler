package Front.SyntaxAnalyzer.Element;

public class FuncDef extends SyntaxNode {
    public final FuncType funcType;
    public final Ident ident;
    public final FuncFParams funcFParams;
    public final Block block;
    public int endLine;

    public FuncDef(FuncType funcType, Ident ident, FuncFParams funcFParams, Block block, int endLine) {
        this.funcType = funcType;
        this.ident = ident;
        this.funcFParams = funcFParams;
        this.block = block;
        this.endLine = endLine;
        if (funcType != null) childrenNode.add(funcType);
        if (ident != null) childrenNode.add(ident);
        if (funcFParams != null) childrenNode.add(funcFParams);
        if (block != null) childrenNode.add(block);
    }

    @Override
    public String toString() {
        StringBuilder res;
        res = new StringBuilder();
        res.append(funcType.toString());
        res.append(ident.toString());
        res.append("LPARENT (\n");
        if (funcFParams != null) res.append(funcFParams);
        res.append("RPARENT )\n");
        res.append(block.toString());
        res.append("<FuncDef>\n");
        return res.toString();
    }

    public int getNumOfParams() {
        if (funcFParams == null) return 0;
        else return funcFParams.funcFParams.size();
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
