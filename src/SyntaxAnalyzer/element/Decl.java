package SyntaxAnalyzer.element;

public class Decl extends SyntaxNode{
    public ConstDecl constDecl;
    public VarDecl varDecl;

    public Decl(ConstDecl constDecl) {
        this.constDecl = constDecl;
        childrenNode.add(constDecl);
    }

    public Decl(VarDecl varDecl) {
        this.varDecl = varDecl;
        childrenNode.add(varDecl);
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        if(constDecl == null)
            res.append(varDecl.toString());
        else res.append(constDecl);
        return res.toString();
    }
}
