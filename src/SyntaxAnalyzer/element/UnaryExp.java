package SyntaxAnalyzer.element;

public class UnaryExp extends SyntaxNode{
    private PrimaryExp primaryExp;
    private Ident ident;
    private FuncRParams funcRParams;
    private UnaryOp unaryOp;
    private UnaryExp unaryExp;

    public UnaryExp(PrimaryExp primaryExp) {
        this.primaryExp = primaryExp;
        childrenNode.add(primaryExp);
    }
    public UnaryExp(Ident ident) {
        this.ident = ident;
    }
    public UnaryExp(Ident ident, FuncRParams funcRParams) {
        this.ident = ident;
        this.funcRParams = funcRParams;
    }

    public UnaryExp(UnaryOp unaryOp, UnaryExp unaryExp) {
        this.unaryOp = unaryOp;
        this.unaryExp = unaryExp;
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        if(primaryExp != null){
            res.append(primaryExp);
        }
        else if(ident != null){
            res.append(ident);
            res.append("LPARENT (\n");
            if(funcRParams != null){
                res.append(funcRParams);
            }
            res.append("RPARENT )\n");
        }
        else {
            res.append(unaryOp.toString());
            res.append(unaryExp.toString());
        }
        res.append("<UnaryExp>\n");
        return res.toString();
    }
}
