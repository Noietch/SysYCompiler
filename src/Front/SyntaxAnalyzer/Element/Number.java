package Front.SyntaxAnalyzer.Element;

public class Number extends SyntaxNode{
    public IntConst intConst;

    public Number(IntConst intConst) {
        this.intConst = intConst;
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        res.append(intConst.toString());
        res.append("<Number>\n");
        return res.toString();
    }


}