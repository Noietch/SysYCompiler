package Front.SyntaxAnalyzer.Element;

public class BlockItem extends SyntaxNode {
    public Decl decl;

    public Stmt stmt;

    public BlockItem(Decl decl) {
        this.decl = decl;
        childrenNode.add(decl);
    }

    public BlockItem(Stmt stmt) {
        this.stmt = stmt;
        childrenNode.add(stmt);
    }

    public Decl getDecl() {
        return decl;
    }

    public Stmt getStmt() {
        return stmt;
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        if (decl == null) {
            res.append(stmt.toString());
        } else res.append(decl);
        return res.toString();
    }
}
