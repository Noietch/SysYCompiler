package SyntaxAnalyzer.element;

public class BlockItem extends SyntaxNode{
    private Decl decl;
    private Stmt stmt;
    public BlockItem(Decl decl) {
        this.decl = decl;
        childrenNode.add(decl);
    }
    public BlockItem(Stmt stmt) {
        this.stmt = stmt;
        childrenNode.add(stmt);
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        if(decl == null){
            res.append(stmt.toString());
        }
        else res.append(decl);
//        res.append("<BlockItem>\n");
        return res.toString();
    }
}
