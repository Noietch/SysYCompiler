package Front.SyntaxAnalyzer;

import Front.SyntaxAnalyzer.SymbolTable.Symbol;
import Front.SyntaxAnalyzer.SymbolTable.SymbolTable;
import Front.SyntaxAnalyzer.Element.*;

import java.util.ArrayList;

public class ErrorHandler {
    public CompUnit syntaxTreeRoot; // 语法树根
    public SymbolTable currentSymbolTable = new SymbolTable(null); //

    public SyntaxError syntaxErrorList;

    public void getSymbolTable() {
        currentSymbolTable.getAll(currentSymbolTable);
    }

    public String getErrorList() {
        travelSyntaxTree(syntaxTreeRoot);
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
                if (syntaxNode instanceof ConstDef) {
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
                        if (funcDef.funcType.type.equals("int") && !funcDef.isReturn()) {
                            syntaxErrorList.addError(ErrorType.NoReturn, funcDef.endLine);
                        }
                    }
                    if (syntaxNode instanceof MainFuncDef) {
                        MainFuncDef mainFuncDef = (MainFuncDef) syntaxNode;
                        if (currentSymbolTable.isDuplicateCurField(mainFuncDef.ident))
                            syntaxErrorList.addError(ErrorType.MultiDefinition, mainFuncDef.ident.token.lineNum);
                        else currentSymbolTable.addSymbol(mainFuncDef.ident, new FuncType("int"), 0, null);
                        if (!mainFuncDef.isReturn()) {
                            syntaxErrorList.addError(ErrorType.NoReturn, mainFuncDef.endLine);
                        }
                    }
                    currentSymbolTable = currentSymbolTable.newSon();
                    travelSyntaxTree(syntaxNode);
                    currentSymbolTable = currentSymbolTable.back();
                    continue;
                } else if (syntaxNode instanceof Stmt) {
                    Stmt stmt = (Stmt) syntaxNode;
                    if (stmt.getType() == Stmt.Type.Block) {
                        currentSymbolTable = currentSymbolTable.newSon();
                        travelSyntaxTree(syntaxNode);
                        currentSymbolTable = currentSymbolTable.back();
                        continue;
                    }
                    if (stmt.getType() == Stmt.Type.Return) {
                        if (!currentSymbolTable.checkFatherFuncType(stmt.exps.size())) {
                            syntaxErrorList.addError(ErrorType.WrongReturn, stmt.ident.token.lineNum);
                        }
                    }
                    if (stmt.getType() == Stmt.Type.Print) {
                        if (stmt.exps.size() != stmt.formatString.paramNum()) {
                            syntaxErrorList.addError(ErrorType.PrintNum, stmt.ident.token.lineNum);
                        }
                    }
                    if (stmt.getType() == Stmt.Type.AssignmentExp || stmt.getType() == Stmt.Type.AssignmentInput) {
                        if (currentSymbolTable.checkIsConst(stmt.lVal.ident)) {
                            syntaxErrorList.addError(ErrorType.AssignConst, stmt.lVal.ident.token.lineNum);
                        }
                    }
                } else if (syntaxNode instanceof LVal) {
                    LVal lVal = (LVal) syntaxNode;
                    if (currentSymbolTable.isExistUpField(lVal.ident) == null)
                        syntaxErrorList.addError(ErrorType.Undefined, lVal.ident.token.lineNum);
                } else if (syntaxNode instanceof UnaryExp) {
                    UnaryExp unaryExp = (UnaryExp) syntaxNode;
                    // 调用函数
                    if (unaryExp.ident != null) {
                        Symbol symbol = currentSymbolTable.isExistUpField(unaryExp.ident);
                        if (symbol == null) {
                            syntaxErrorList.addError(ErrorType.Undefined, unaryExp.ident.token.lineNum);
                        } else if (unaryExp.getNumOfParam() != symbol.paramsNum) {
                            syntaxErrorList.addError(ErrorType.ParamNumber, unaryExp.ident.token.lineNum);
                        } else {
                            if (unaryExp.funcRParams != null) {
                                ArrayList<Symbol.Type> types = unaryExp.funcRParams.getParamType(currentSymbolTable);
                                for (int i = 0; i < types.size(); i++) {
                                    if (!symbol.funcFParams.funcFParams.get(i).checkType(types.get(i))) {
                                        syntaxErrorList.addError(ErrorType.ParamClass, unaryExp.ident.token.lineNum);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                travelSyntaxTree(syntaxNode);
            }
        }
    }
}