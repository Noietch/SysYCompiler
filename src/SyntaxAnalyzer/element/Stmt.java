package SyntaxAnalyzer.element;

import java.util.ArrayList;

public class Stmt extends SyntaxNode{
    public enum Type{
        AssignmentExp,
        AssignmentInput,
        Expression,
        Block,
        Break,
        Continue,
        Branch_IF,
        Branch_ELSE,
        Loop,
        Return,
        Print
    }
    private LVal lVal;
    private ArrayList<Exp> exps;
    private Cond cond;
    private ArrayList<Stmt> stmts;
    private FormatString formatString;
    private final Type type;
    private Block block;

    public Stmt(Type type, Block block) {
        this.type = type;
        this.block = block;
    }

    public Stmt(Type type) {
        this.type = type;
    }
    public Stmt(Cond cond,Type type) {
        this.cond = cond;
        this.type = type;
        childrenNode.add(cond);
    }
    public Stmt(LVal lVal,Type type) {
        this.lVal = lVal;
        this.type = type;
        childrenNode.add(lVal);
    }
    public Stmt(ArrayList<Exp> exps,Type type) {
        this.exps = exps;
        this.type = type;
        childrenNode.addAll(exps);
    }
    public Stmt(ArrayList<Exp> exps, FormatString formatString,Type type) {
        this.exps = exps;
        this.type = type;
        this.formatString = formatString;
        childrenNode.addAll(exps);
        childrenNode.add(formatString);
    }
    public Stmt(Cond cond, ArrayList<Stmt> stmts,Type type) {
        this.cond = cond;
        this.stmts = stmts;
        this.type = type;
        childrenNode.add(cond);
        childrenNode.addAll(stmts);
    }
    public Stmt(LVal lVal, ArrayList<Exp> exps,Type type) {
        this.lVal = lVal;
        this.exps = exps;
        this.type = type;
        childrenNode.add(lVal);
        childrenNode.addAll(exps);
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        if(type == Type.AssignmentExp){
            res.append(lVal.toString());
            res.append("ASSIGN =\n");
            res.append(exps.get(0).toString());
            res.append("SEMICN ;\n");
        }
        else if(type==Type.Expression){
            for(Exp exp:exps){
                res.append(exp.toString());
            }
            res.append("SEMICN ;\n");
        }
        else if(type==Type.Block){
            res.append(block);
        }
        else if(type==Type.Branch_IF || type==Type.Branch_ELSE){
            res.append("IFTK if\n");
            res.append("LPARENT (\n");
            res.append(cond.toString());
            res.append("RPARENT )\n");
            res.append(stmts.get(0).toString());
            if(type==Type.Branch_ELSE){
                res.append("ELSETK else\n");
                res.append(stmts.get(1).toString());
            }
        }
        else if(type==Type.Loop){
            res.append("WHILETK while\n");
            res.append("LPARENT (\n");
            res.append(cond.toString());
            res.append("RPARENT )\n");
            res.append(stmts.get(0).toString());
        }
        else if(type==Type.Break){
            res.append("BREAKTK break\n");
            res.append("SEMICN ;\n");
        }
        else if(type==Type.Continue){
            res.append("CONTINUETK continue\n");
            res.append("SEMICN ;\n");
        }
        else if(type==Type.Return){
            res.append("RETURNTK return\n");
            for(Exp exp:exps){
                res.append(exp.toString());
            }
            res.append("SEMICN ;\n");
        }
        else if(type==Type.AssignmentInput){
            res.append(lVal.toString());
            res.append("ASSIGN =\n");
            res.append("GETINTTK getint\n");
            res.append("LPARENT (\n");
            res.append("RPARENT )\n");
            res.append("SEMICN ;\n");
        }
        else if(type==Type.Print){
            res.append("PRINTFTK printf\n");
            res.append("LPARENT (\n");
            res.append(formatString.toString());
            for(Exp exp:exps){
                res.append("COMMA ,\n");
                res.append(exp.toString());
            }
            res.append("RPARENT )\n");
            res.append("SEMICN ;\n");
        }
        res.append("<Stmt>\n");
        return res.toString();
    }

    public Type getType() {
        return type;
    }
}
