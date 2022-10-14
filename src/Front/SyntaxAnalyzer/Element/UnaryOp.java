package Front.SyntaxAnalyzer.Element;

public class UnaryOp extends SyntaxNode{
    public enum type{
        MINUS,
        NOT,
        PLUS,
    }

    public final type opType;

    public UnaryOp(type type) {
        this.opType = type;
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        if (this.opType == UnaryOp.type.MINUS) {
            res.append("MINU -\n");
        }
        else if (this.opType == UnaryOp.type.NOT) {
            res.append("NOT !\n");
        }
        else if (this.opType == UnaryOp.type.PLUS){
            res.append("PLUS +\n");
        }
        res.append("<UnaryOp>\n");
        return res.toString();
    }
}
