package SyntaxAnalyzer.element;

public class Exp extends SyntaxNode{
    private final AddExp addExp;

    public Exp(AddExp addExp) {
        this.addExp = addExp;
        childrenNode.add(addExp);
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        res.append(addExp.toString());
        res.append("<Exp>\n");
        return res.toString();
    }
}
