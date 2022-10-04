package SyntaxAnalyzer.element;

public class ConstExp extends SyntaxNode{
    private AddExp addExp;

    public ConstExp(AddExp addExp) {
        this.addExp = addExp;
        childrenNode.add(addExp);
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        res.append(addExp.toString());
        res.append("<ConstExp>\n");
        return res.toString();
    }
}
