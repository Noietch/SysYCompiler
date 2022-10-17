package Front.SyntaxAnalyzer.Element;

public class UnaryOp extends SyntaxNode{
    public enum Type {
        MINUS,
        NOT,
        PLUS,
    }

    public final Type opType;

    public UnaryOp(Type type) {
        this.opType = type;
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        if (this.opType == Type.MINUS) {
            res.append("MINU -\n");
        }
        else if (this.opType == Type.NOT) {
            res.append("NOT !\n");
        }
        else if (this.opType == Type.PLUS){
            res.append("PLUS +\n");
        }
        res.append("<UnaryOp>\n");
        return res.toString();
    }
}
