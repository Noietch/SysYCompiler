package SyntaxAnalyzer;

import LexicalAnalyzer.Token;
import SyntaxAnalyzer.SymbolTable.SymbolTable;
import SyntaxAnalyzer.element.*;
import SyntaxAnalyzer.element.Number;

import java.util.ArrayList;

import static java.lang.System.exit;

public class TokenHandler {
    private final ArrayList<Token> tokenList;
    private int curPos;
    private final int len;

    private final SymbolTable currentSymbolTable;

    private final SyntaxError syntaxError;

    public TokenHandler(ArrayList<Token> tokenList) {
        this.tokenList = tokenList;
        this.curPos = 0;
        this.len = tokenList.size();
        this.currentSymbolTable = new SymbolTable(null);
        this.syntaxError = new SyntaxError();
    }

    private void nextSym() throws ParseError {
        curPos++;
        if (curPos > len) {
            throw new ParseError("Finish");
        }
    }

    private Token getSym() {
        return tokenList.get(curPos);
    }

    private Token getSym(int bias) {
        return tokenList.get(curPos + bias);
    }

    private int getSymLine(int bias) {
        return tokenList.get(curPos + bias).lineNum;
    }

    private Boolean SymTypeIs(String type) {
        return getSym().categoryCode.equals(type);
    }

    private Boolean SymTypeIs(String type, int bias) {
        return getSym(bias).categoryCode.equals(type);
    }

    public String getSymList() {
        return compUnit().toString();
    }

    public String getErrorList() {
        return syntaxError.toString();
    }

    private CompUnit compUnit() {
        ArrayList<Decl> decls = new ArrayList<>();
        ArrayList<FuncDef> funcDefs = new ArrayList<>();
        MainFuncDef mainFuncDef = null;
        try {
            // 没有声明和函数定义
            if (SymTypeIs("INTTK") && SymTypeIs("MAINTK", 1)) {
                mainFuncDef = mainFuncDef();
            }
            // 只有函数定义
            else if (SymTypeIs("INTTK") && SymTypeIs("LPARENT", 2)) {
                while (SymTypeIs("LPARENT", 2) && !SymTypeIs("MAINTK", 1)) {
                    funcDefs.add(funcDef());
                }
                mainFuncDef = mainFuncDef();
            }
            // 既有变量声明又有函数定义
            else {
                while (SymTypeIs("CONSTTK") || SymTypeIs("INTTK") && !SymTypeIs("LPARENT", 2)) {
                    decls.add(decl());
                }
                while (SymTypeIs("LPARENT", 2) && !SymTypeIs("MAINTK", 1)) {
                    funcDefs.add(funcDef());
                }
                mainFuncDef = mainFuncDef();
            }
        } catch (Exception e) {
            e.printStackTrace();
            exit(0);
        }
        return new CompUnit(decls, funcDefs, mainFuncDef);
    }

    private Decl decl() throws ParseError {
        if (SymTypeIs("INTTK")) {
            return new Decl(varDecl());
        } else {
            return new Decl(constDecl());
        }
    }

    private ConstDecl constDecl() throws ParseError {
        ArrayList<ConstDef> constDefs = new ArrayList<>();
        if (SymTypeIs("CONSTTK")) {
            nextSym();
            if (SymTypeIs("INTTK")) {
                nextSym();
                while (true) {
                    constDefs.add(constDef());
                    if (SymTypeIs("COMMA")) nextSym(); // 过滤掉逗号
                    else if (SymTypeIs("SEMICN")) {
                        break; // 遇到分号停止
                    } else {
                        this.syntaxError.addError(ErrorType.NoSemi, getSymLine(-1));
                        return new ConstDecl(new BType(), constDefs);
                    }
                }
            } else throw new ParseError("[ConstDecl Error] INTTK");
        } else throw new ParseError("[ConstDecl Error] CONSTTK");
        nextSym(); // 将指针向下移动
        return new ConstDecl(new BType(), constDefs);
    }

    private ConstDef constDef() throws ParseError {
        ArrayList<ConstExp> constExps = new ArrayList<>();
        Ident ident;
        ConstInitVal tempConstInitVal;
        if (SymTypeIs("IDENFR")) {
            ident = new Ident(getSym());
            nextSym();
            while (true) { // 数组类型
                if (!SymTypeIs("LBRACK")) break;
                else nextSym();//掠过左边括号
                constExps.add(constExp());
                if (SymTypeIs("RBRACK")) nextSym();
                else {
                    this.syntaxError.addError(ErrorType.NoRightMiddle, getSymLine(-1));
                }
            }
            if (SymTypeIs("ASSIGN")) {
                nextSym();
                tempConstInitVal = constInitVal();
            } else throw new ParseError("[ConstDef Error] ASSIGN");
        } else throw new ParseError("[ConstDef Error] CONSTTK");
        return new ConstDef(ident, constExps, tempConstInitVal);
    }

    private ConstInitVal constInitVal() throws ParseError {
        ArrayList<SyntaxNode> syntaxNodes = new ArrayList<>();
        VarType initType;
        if (SymTypeIs("LBRACE")) { // 数组
            nextSym();
            initType = VarType.Array;
            while (true) {
                if (SymTypeIs("RBRACE")) {
                    nextSym();
                    break;
                }
                syntaxNodes.add(constInitVal());
                if (SymTypeIs("COMMA")) nextSym();
                else throw new ParseError("[ConstInitVal Error] COMMA");
            }
        } else { // 普通变量
            syntaxNodes.add(constExp());
            initType = VarType.Var;
        }
        return new ConstInitVal(syntaxNodes, initType);
    }

    private VarDecl varDecl() throws ParseError {
        ArrayList<VarDef> varDefs = new ArrayList<>();
        if (SymTypeIs("INTTK")) {
            nextSym();
            while (true) {
                varDefs.add(varDef());
                if (SymTypeIs("COMMA")) nextSym(); // 过滤掉逗号
                else if (SymTypeIs("SEMICN")) {
                    break; // 遇到分号停止
                } else {
                    this.syntaxError.addError(ErrorType.NoSemi, getSymLine(-1));
                    return new VarDecl(new BType(), varDefs);
                }
            }
        } else throw new ParseError("[VarDecl Error] INTTK");
        nextSym();
        return new VarDecl(new BType(), varDefs);
    }


    private VarDef varDef() throws ParseError {
        ArrayList<ConstExp> constExps = new ArrayList<>();
        Ident ident;
        InitVal tempinitVal;

        if (SymTypeIs("IDENFR")) {
            ident = new Ident(getSym());
            nextSym();
            while (true) { // 数组类型
                if (!SymTypeIs("LBRACK")) break;
                else nextSym();//掠过左边括号
                constExps.add(constExp());
                if (SymTypeIs("RBRACK")) nextSym();
                else {
                    this.syntaxError.addError(ErrorType.NoRightMiddle, getSymLine(-1));
                }
            }
            // 有赋值
            if (SymTypeIs("ASSIGN")) {
                nextSym();
                tempinitVal = initVal();
                return new VarDef(ident, constExps, tempinitVal);
            }
            // 没有赋值
            else return new VarDef(ident, constExps);
        } else throw new ParseError("[VarDef Error] IDENFR");
    }

    private InitVal initVal() throws ParseError {
        ArrayList<SyntaxNode> syntaxNodes = new ArrayList<>();
        VarType initType = null;

        if (SymTypeIs("LBRACE")) { // 数组
            nextSym();
            while (true) {
                syntaxNodes.add(initVal());
                if (SymTypeIs("COMMA")) nextSym();
                else if (SymTypeIs("RBRACE")) { // 右边大括号
                    nextSym();
                    break;
                }
            }
        } else { // 普通变量
            syntaxNodes.add(exp());
            initType = VarType.Var;
        }
        return new InitVal(syntaxNodes, initType);
    }

    private FuncDef funcDef() throws ParseError {
        FuncType tempFuncType;
        Ident tempIdent;
        ArrayList<FuncFParams> tempFuncFParams = new ArrayList<>();
        Block tempBlock;

        tempFuncType = funcType();
        if (SymTypeIs("IDENFR")) {
            tempIdent = new Ident(getSym());
            nextSym();
            if (SymTypeIs("LPARENT")) {
                nextSym();
                if (!SymTypeIs("RPARENT")) {
                    int tempPos = this.curPos;
                    try {
                        tempFuncFParams.add(funcFParams());
                    } catch (Exception e) {
                        this.curPos = tempPos;
                        this.syntaxError.addError(ErrorType.NoRightSmall, getSymLine(-1));
                    }
                } else nextSym();
                tempBlock = block();
            } else throw new ParseError("[FuncDef Error] LPARENT");
        } else throw new ParseError("[FuncDef Error] FuncDef");
        return new FuncDef(tempFuncType, tempIdent, tempFuncFParams, tempBlock);
    }

    private MainFuncDef mainFuncDef() throws ParseError {
        Block tempBlock = null;
        if (SymTypeIs("INTTK")) {
            nextSym();
            if (SymTypeIs("MAINTK")) {
                nextSym();
                if (SymTypeIs("LPARENT")) {
                    nextSym();
                    if (SymTypeIs("RPARENT"))
                        nextSym();
                    else
                        this.syntaxError.addError(ErrorType.NoRightSmall, getSymLine(-1));
                    tempBlock = block();
                } else throw new ParseError("[MainFuncDef Error] LPARENT");
            } else throw new ParseError("[MainFuncDef Error] MAINTK");
        } else throw new ParseError("[MainFuncDef Error] INTTK");
        return new MainFuncDef(tempBlock);
    }

    private FuncType funcType() throws ParseError {
        String type;
        if (SymTypeIs("VOIDTK")) {
            type = "void";
        } else if (SymTypeIs("INTTK")) {
            type = "int";
        } else throw new ParseError("[FuncType Error] TYPE");
        nextSym();
        return new FuncType(type);
    }

    private FuncFParams funcFParams() throws ParseError {
        ArrayList<FuncFParam> funcFParams = new ArrayList<>();
        while (true) {
            funcFParams.add(funcFParam());
            if (SymTypeIs("COMMA")) nextSym();
            else break;
        }
        nextSym();
        return new FuncFParams(funcFParams);
    }

    private FuncFParam funcFParam() throws ParseError {
        Ident tempIdent;
        VarType varType;
        ArrayList<ConstExp> constExps = new ArrayList<>();
        if (SymTypeIs("INTTK")) {
            nextSym();
            tempIdent = new Ident(getSym());
            varType = VarType.Var;
            // 参数是数组
            nextSym();
            if (SymTypeIs("LBRACK")) {
                varType = VarType.oneDimArray;
                nextSym();
                if (SymTypeIs("RBRACK")) {
                    nextSym();
                    if (SymTypeIs("LBRACK")) {
                        nextSym();
                        constExps.add(constExp());
                        varType = VarType.twoDimArray;
                        if (!SymTypeIs("RBRACK")) {
                            this.syntaxError.addError(ErrorType.NoRightMiddle, getSymLine(-1));
                            return new FuncFParam(new BType(), tempIdent, varType, constExps);
                        }
                    }
                } else {
                    this.syntaxError.addError(ErrorType.NoRightMiddle, getSymLine(-1));
                    return new FuncFParam(new BType(), tempIdent, varType, constExps);
                }
            }
        } else throw new ParseError("[FuncFParam Error] INTTK");
        return new FuncFParam(new BType(), tempIdent, varType, constExps);
    }

    private Block block() throws ParseError {
        ArrayList<BlockItem> blockItems = new ArrayList<>();
        if (SymTypeIs("LBRACE")) {
            nextSym();
            while (!SymTypeIs("RBRACE")) {
                blockItems.add(blockItem());
            }
        }
        nextSym();
        return new Block(blockItems);
    }

    private BlockItem blockItem() throws ParseError {
        if (SymTypeIs("CONSTTK") || SymTypeIs("INTTK")) {
            return new BlockItem(decl());
        } else return new BlockItem(stmt());
    }

    private Stmt stmt() throws ParseError {
        Stmt.Type type;
        ArrayList<Stmt> stmts = new ArrayList<>();
        ArrayList<Exp> exps = new ArrayList<>();
        FormatString formatString;
        Cond tempCond;
        Block tempBlock;
        LVal tempLVal;
        // print
        if (SymTypeIs("PRINTFTK")) {
            nextSym();
            if (SymTypeIs("LPARENT")) {
                nextSym();
                if (SymTypeIs("STRCON")) {
                    formatString = new FormatString(getSym());
                    if (!formatString.check()) {
                        this.syntaxError.addError(ErrorType.IllegalSymbol, formatString.getLine());
                    }
                    nextSym();
                    while (true) {
                        if (SymTypeIs("COMMA")) {
                            nextSym();
                            exps.add(exp());
                        }
                        else {
                            if (!SymTypeIs("RPARENT"))
                                this.syntaxError.addError(ErrorType.NoSemi, getSym(-1).lineNum);
                            break;
                        }
                    }
                    nextSym();
                    type = Stmt.Type.Print;
                    if (SymTypeIs("SEMICN")) nextSym();
                    else this.syntaxError.addError(ErrorType.NoSemi, getSym(-1).lineNum);
                    return new Stmt(exps, formatString, type);
                }
            }
        }
        // return
        else if (SymTypeIs("RETURNTK")) {
            type = Stmt.Type.Return;
            nextSym();
            if (!SymTypeIs("SEMICN")) {
                int tempPos = this.curPos;
                try {
                    exps.add(exp());
                } catch (Exception e) {
                    this.curPos = tempPos;
                }
                if (SymTypeIs("SEMICN")) nextSym();
                else {
                    System.out.println(getSym(-1));
                    this.syntaxError.addError(ErrorType.NoSemi, getSym(-1).lineNum);
                }
            }
            return new Stmt(exps, type);
        }
        // break
        else if (SymTypeIs("BREAKTK")) {
            nextSym();
            type = Stmt.Type.Break;
            if (SymTypeIs("SEMICN")) nextSym();
            else this.syntaxError.addError(ErrorType.NoSemi, getSym(-1).lineNum);
            return new Stmt(type);
        }
        // continue
        else if (SymTypeIs("CONTINUETK")) {
            nextSym();
            type = Stmt.Type.Continue;
            if (SymTypeIs("SEMICN")) nextSym();
            else this.syntaxError.addError(ErrorType.NoSemi, getSym(-1).lineNum);
            return new Stmt(type);
        }
        // while
        else if (SymTypeIs("WHILETK")) {
            nextSym();
            if (SymTypeIs("LPARENT")) {
                nextSym();
                tempCond = cond();
                if (SymTypeIs("RPARENT")) nextSym();
                else this.syntaxError.addError(ErrorType.NoRightSmall,getSymLine(-1));
                stmts.add(stmt());
                type = Stmt.Type.Loop;
                return new Stmt(tempCond, stmts, type);
            }
        }
        // if
        else if (SymTypeIs("IFTK")) {
            nextSym();
            if (SymTypeIs("LPARENT")) {
                nextSym();
                tempCond = cond();
                if (SymTypeIs("RPARENT")) nextSym();
                else this.syntaxError.addError(ErrorType.NoRightSmall,getSymLine(-1));
                stmts.add(stmt());
                if (SymTypeIs("ELSETK")) {
                    nextSym();
                    stmts.add(stmt());
                    type = Stmt.Type.Branch_ELSE;
                } else {
                    type = Stmt.Type.Branch_IF;
                }
                return new Stmt(tempCond, stmts, type);
            }
        }
        // block
        else if (SymTypeIs("LBRACE")) {
            tempBlock = block();
            type = Stmt.Type.Block;
            return new Stmt(type, tempBlock);
        } else {
            boolean flag = true;
            int idx = 0;
            while (!SymTypeIs("SEMICN", idx)) {
                if (SymTypeIs("ASSIGN", idx)) {
                    flag = false;
                    break;
                }
                idx++;
            }
            // expression
            if (flag) {
                type = Stmt.Type.Expression;
                if (!SymTypeIs("SEMICN")) {
                    exps.add(exp());
                }
                nextSym();
                return new Stmt(exps, type);
            }
            // assign
            else {
                tempLVal = lVal();
                if (SymTypeIs("ASSIGN")) {
                    // getint()
                    if (SymTypeIs("GETINTTK", 1)) {
                        type = Stmt.Type.AssignmentInput;
                        nextSym();
                        nextSym();
                        if (SymTypeIs("LPARENT")) {
                            nextSym();
                            if (SymTypeIs("RPARENT")) nextSym();
                            else this.syntaxError.addError(ErrorType.NoRightSmall,getSymLine(-1));

                            if (SymTypeIs("SEMICN")) nextSym();
                            else this.syntaxError.addError(ErrorType.NoRightSmall,getSymLine(-1));

                            return new Stmt(tempLVal, type);
                        }
                    }
                    // left value = exp
                    else {
                        nextSym();
                        exps.add(exp());
                        if (SymTypeIs("SEMICN")) {
                            type = Stmt.Type.AssignmentExp;
                            nextSym();
                            return new Stmt(tempLVal, exps, type);
                        }
                    }
                }
            }
            throw new ParseError("stmt");
        }
        throw new ParseError("stmt");
    }

    private Exp exp() throws ParseError {
        return new Exp(addExp());
    }

    private Cond cond() throws ParseError {
        return new Cond(lOrExp());
    }

    private LVal lVal() throws ParseError {
        if (SymTypeIs("IDENFR")) {
            Ident ident = new Ident(getSym());
            VarType type = VarType.Var;
            ArrayList<Exp> exps = new ArrayList<>();
            if (SymTypeIs("LBRACK", 1)) {
                nextSym();
                type = VarType.Array;
                while (SymTypeIs("LBRACK")) {
                    nextSym();
                    exps.add(exp());
                    if (SymTypeIs("RBRACK")) {
                        nextSym();
                    } else throw new ParseError("lVal error");
                }
            } else nextSym();
            return new LVal(ident, type, exps);
        } else throw new ParseError("[LVal Error] IDENFR");
    }

    private PrimaryExp primaryExp() throws ParseError {
        if (SymTypeIs("INTCON")) return new PrimaryExp(number());
        else if (SymTypeIs("LPARENT")) {
            nextSym();
            Exp tempExp = exp();
            if (SymTypeIs("RPARENT")) {
                nextSym();
                return new PrimaryExp(tempExp);
            } else throw new ParseError("error !!");
        } else return new PrimaryExp(lVal());
    }

    private Number number() throws ParseError {
        IntConst intConst = new IntConst(getSym());
        nextSym();
        return new Number(intConst);
    }

    private UnaryExp unaryExp() throws ParseError {
        PrimaryExp tempPrimaryExp;
        Ident ident;
        FuncRParams funcRParams;
        UnaryOp tempUnaryOp;
        UnaryExp tempUnaryExp;
        if (SymTypeIs("IDENFR") && SymTypeIs("LPARENT", 1)) {
            ident = new Ident(getSym());
            nextSym(); // skip func name
            nextSym(); // skip (
            if (!SymTypeIs("RPARENT")) {
                funcRParams = funcRParams();
                if (SymTypeIs("RPARENT")) {
                    nextSym();
                    return new UnaryExp(ident, funcRParams);
                } else throw new ParseError(") Lost");
            } else {
                nextSym();
                return new UnaryExp(ident);
            }
        } else if (SymTypeIs("PLUS") || SymTypeIs("MINU") || SymTypeIs("NOT")) {
            tempUnaryOp = unaryOp();
            nextSym();
            tempUnaryExp = unaryExp();
            return new UnaryExp(tempUnaryOp, tempUnaryExp);
        } else {
            tempPrimaryExp = primaryExp();
            return new UnaryExp(tempPrimaryExp);
        }
    }

    private UnaryOp unaryOp() {
        if (SymTypeIs("PLUS")) return new UnaryOp(UnaryOp.type.PLUS);
        else if (SymTypeIs("MINU")) return new UnaryOp(UnaryOp.type.MINUS);
        else if (SymTypeIs("NOT")) return new UnaryOp(UnaryOp.type.NOT);
        else return null;
    }

    private FuncRParams funcRParams() throws ParseError {
        ArrayList<Exp> exps = new ArrayList<>();
        while (true) {
            exps.add(exp());
            if (SymTypeIs("COMMA")) nextSym();
            else break;
        }
        return new FuncRParams(exps);
    }

    private MulExp mulExp() throws ParseError {
        ArrayList<UnaryExp> unaryExps = new ArrayList<>();
        ArrayList<Ident> unaryOps = new ArrayList<>();
        unaryExps.add(unaryExp());
        while (SymTypeIs("MULT") || SymTypeIs("DIV") || SymTypeIs("MOD")) {
            unaryOps.add(new Ident(getSym()));
            nextSym();
            unaryExps.add(unaryExp());
        }
        return new MulExp(unaryExps, unaryOps);
    }

    private AddExp addExp() throws ParseError {
        ArrayList<MulExp> mulExps = new ArrayList<>();
        ArrayList<Ident> unaryOps = new ArrayList<>();
        mulExps.add(mulExp());
        while (SymTypeIs("PLUS") || SymTypeIs("MINU")) {
            unaryOps.add(new Ident(getSym()));
            nextSym();
            mulExps.add(mulExp());
        }
        return new AddExp(mulExps, unaryOps);
    }

    private RelExp relExp() throws ParseError {
        ArrayList<AddExp> addExps = new ArrayList<>();
        ArrayList<Ident> unaryOps = new ArrayList<>();
        addExps.add(addExp());
        while (SymTypeIs("LSS") || SymTypeIs("LEQ") || SymTypeIs("GRE") || SymTypeIs("GEQ")) {
            unaryOps.add(new Ident(getSym()));
            nextSym();
            addExps.add(addExp());
        }
        return new RelExp(addExps, unaryOps);
    }

    private EqExp eqExp() throws ParseError {
        ArrayList<RelExp> relExps = new ArrayList<>();
        ArrayList<Ident> unaryOps = new ArrayList<>();
        relExps.add(relExp());
        while (SymTypeIs("EQL") || SymTypeIs("NEQ")) {
            unaryOps.add(new Ident(getSym()));
            nextSym();
            relExps.add(relExp());
        }
        return new EqExp(relExps, unaryOps);
    }

    private LAndExp lAndExp() throws ParseError {
        ArrayList<EqExp> eqExps = new ArrayList<>();
        ArrayList<Ident> unaryOps = new ArrayList<>();
        eqExps.add(eqExp());
        while (SymTypeIs("AND")) {
            unaryOps.add(new Ident(getSym()));
            nextSym();
            eqExps.add(eqExp());
        }
        return new LAndExp(eqExps, unaryOps);
    }

    private LOrExp lOrExp() throws ParseError {
        ArrayList<LAndExp> lAndExps = new ArrayList<>();
        ArrayList<Ident> unaryOps = new ArrayList<>();
        lAndExps.add(lAndExp());
        while (SymTypeIs("OR")) {
            unaryOps.add(new Ident(getSym()));
            nextSym();
            lAndExps.add(lAndExp());
        }
        return new LOrExp(lAndExps, unaryOps);
    }

    private ConstExp constExp() throws ParseError {
        return new ConstExp(addExp());
    }
}
