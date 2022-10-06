package SyntaxAnalyzer.element;

import java.util.ArrayList;

public class FuncFParam extends SyntaxNode{
    public BType bType;
    public Ident ident;
    public VarType varType;
    public ArrayList<ConstExp> constExps;

    public FuncFParam(BType bType, Ident ident, VarType varType, ArrayList<ConstExp> constExps) {
        this.bType = bType;
        this.ident = ident;
        this.varType = varType;
        this.constExps = constExps;
        childrenNode.add(bType);
        childrenNode.add(ident);
        childrenNode.addAll(constExps);
    }

    public FuncFParam(BType bType, Ident ident, VarType varType) {
        this.bType = bType;
        this.ident = ident;
        this.varType = varType;
        childrenNode.add(bType);
        childrenNode.add(ident);
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        res.append(bType.toString());
        res.append(ident.toString());
        if(varType == VarType.oneDimArray){
            res.append("LBRACK [\n");
            res.append("RBRACK ]\n");
        }
        if(varType == VarType.twoDimArray){
            res.append("LBRACK [\n");
            res.append("RBRACK ]\n");
            res.append("LBRACK [\n");
            res.append(constExps.get(0).toString());
            res.append("RBRACK ]\n");
        }
        res.append("<FuncFParam>\n");
        return res.toString();
    }
}
