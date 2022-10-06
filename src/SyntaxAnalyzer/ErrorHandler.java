package SyntaxAnalyzer;

import SyntaxAnalyzer.SymbolTable.SymbolTable;
import SyntaxAnalyzer.element.*;

public class ErrorHandler {
    public CompUnit syntaxTreeRoot; // 语法树根
    public static SymbolTable currentSymbolTable = new SymbolTable(null); //

    public SyntaxError syntaxErrorList;

    public void getSymbolTable() {
        currentSymbolTable.getAll(currentSymbolTable);
    }

    public String getErrorList() {
        return syntaxErrorList.toString();
    }

    public ErrorHandler(CompUnit treeRoot, SyntaxError syntaxErrorList) {
        this.syntaxTreeRoot = treeRoot;
        this.syntaxErrorList = new SyntaxError();
        this.syntaxErrorList.errors.addAll(syntaxErrorList.errors);
    }


    public void travelSyntaxTree(SyntaxNode node) {
        if (node != null) {
            for (SyntaxNode syntaxNode : node.childrenNode) {
                if (syntaxNode instanceof Decl || syntaxNode instanceof ConstDecl || syntaxNode instanceof VarDecl || syntaxNode instanceof BlockItem || syntaxNode instanceof FuncFParams || syntaxNode instanceof Block)
                    travelSyntaxTree(syntaxNode);
                else if (syntaxNode instanceof ConstDef) {
                    ConstDef constDef = (ConstDef) syntaxNode;
                    if (currentSymbolTable.isDuplicateCurField(constDef.ident))
                        syntaxErrorList.addError(ErrorType.MultiDefinition, constDef.ident.token.lineNum);
                    else
                        currentSymbolTable.addSymbol(constDef.ident, true, constDef.constInitVal, null, constDef.constExps.size());
                } else if (syntaxNode instanceof VarDef) {
                    VarDef varDef = (VarDef) syntaxNode;
                    if (currentSymbolTable.isDuplicateCurField(varDef.ident))
                        syntaxErrorList.addError(ErrorType.MultiDefinition, varDef.ident.token.lineNum);
                    else
                        currentSymbolTable.addSymbol(varDef.ident, false, null, varDef.initVal, varDef.constExps.size());
                } else if (syntaxNode instanceof FuncFParam) {
                    FuncFParam funcFParam = (FuncFParam) syntaxNode;
                    if (currentSymbolTable.isDuplicateCurField(funcFParam.ident))
                        syntaxErrorList.addError(ErrorType.MultiDefinition, funcFParam.ident.token.lineNum);
                    else currentSymbolTable.addSymbol(funcFParam.ident, funcFParam.varType);
                } else if (syntaxNode instanceof FuncDef || syntaxNode instanceof MainFuncDef) {
                    if (syntaxNode instanceof FuncDef) {
                        FuncDef funcDef = (FuncDef) syntaxNode;
                        if (currentSymbolTable.isDuplicateCurField(funcDef.ident))
                            syntaxErrorList.addError(ErrorType.MultiDefinition, funcDef.ident.token.lineNum);
                        else
                            currentSymbolTable.addSymbol(funcDef.ident, funcDef.funcType, funcDef.getNumOfParams(), funcDef.funcFParams);
                    }
                    if (syntaxNode instanceof MainFuncDef) {
                        MainFuncDef mainFuncDef = (MainFuncDef) syntaxNode;
                        if (currentSymbolTable.isDuplicateCurField(mainFuncDef.ident))
                            syntaxErrorList.addError(ErrorType.MultiDefinition, mainFuncDef.ident.token.lineNum);
                        else currentSymbolTable.addSymbol(mainFuncDef.ident, new FuncType("int"), 0, null);
                    }
                    currentSymbolTable = currentSymbolTable.newSon();
                    travelSyntaxTree(syntaxNode);
                    currentSymbolTable = currentSymbolTable.back();
                } else if (syntaxNode instanceof Stmt) {
                    Stmt stmt = (Stmt) syntaxNode;
                    if (stmt.getType() == Stmt.Type.Block) {
                        currentSymbolTable = currentSymbolTable.newSon();
                        travelSyntaxTree(syntaxNode);
                        currentSymbolTable = currentSymbolTable.back();
                    }
                }
                else if(syntaxNode instanceof LVal){

                }
            }
        }
    }
}