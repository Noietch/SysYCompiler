package Front.SyntaxAnalyzer.Element;

public class ConstExp extends SyntaxNode{
    public AddExp addExp;

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

    public int eval(){
        return addExp.eval();
    }
}
