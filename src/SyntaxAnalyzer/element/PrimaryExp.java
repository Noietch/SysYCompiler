package SyntaxAnalyzer.element;

public class PrimaryExp extends SyntaxNode{
    private Exp exp;
    private LVal lVal;
    private Number number;

    private boolean isExp;
    public PrimaryExp(Exp exp) {
        this.exp = exp;
        this.isExp = true;
        childrenNode.add(exp);
    }

    public PrimaryExp(Number number) {
        this.number = number;
        this.isExp = false;
        childrenNode.add(number);
    }

    public PrimaryExp(LVal lVal) {
        this.lVal = lVal;
        this.isExp = false;
        childrenNode.add(lVal);
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        if(exp != null){
            res.append("LPARENT (\n");
            res.append(exp);
            res.append("RPARENT )\n");
        }
        if(lVal !=null){
            res.append(lVal);
        }
        if(number!=null){
            res.append(number);
        }
        res.append("<PrimaryExp>\n");
        return res.toString();
    }
}
