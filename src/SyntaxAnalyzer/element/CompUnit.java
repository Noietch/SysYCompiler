package SyntaxAnalyzer.element;

import java.util.ArrayList;

public class CompUnit extends SyntaxNode{
    private ArrayList<Decl> decls;
    private ArrayList<FuncDef> funcDefs;
    private MainFuncDef mainFuncDef;

    public CompUnit(ArrayList<Decl> decls, ArrayList<FuncDef> funcDefs, MainFuncDef mainFuncDef) {
        this.decls = decls;
        this.funcDefs = funcDefs;
        this.mainFuncDef = mainFuncDef;
        childrenNode.addAll(decls);
        childrenNode.addAll(funcDefs);
        childrenNode.add(mainFuncDef);
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        for(Decl decl:decls) res.append(decl.toString());
        for(FuncDef funcDef:funcDefs) res.append(funcDef.toString());
        res.append(mainFuncDef.toString());
        res.append("<CompUnit>\n");
        return res.toString();
    }
}
