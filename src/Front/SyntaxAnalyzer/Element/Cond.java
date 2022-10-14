package Front.SyntaxAnalyzer.Element;

public class Cond extends SyntaxNode {
    public final LOrExp lOrExp;

    public Cond(LOrExp lOrExp) {
        this.lOrExp = lOrExp;
        childrenNode.add(lOrExp);
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        res.append(lOrExp.toString());
        res.append("<Cond>\n");
        return res.toString();
    }
}
