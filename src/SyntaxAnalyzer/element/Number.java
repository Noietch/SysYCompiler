package SyntaxAnalyzer.element;

public class Number extends SyntaxNode{
    private IntConst intConst;

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