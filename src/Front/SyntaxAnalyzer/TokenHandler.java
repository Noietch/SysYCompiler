package Front.SyntaxAnalyzer;

import Front.LexicalAnalyzer.Token;
import Front.SyntaxAnalyzer.Element.*;
import Front.SyntaxAnalyzer.Element.Number;

import java.util.ArrayList;

public class TokenHandler {
    private final ArrayList<Token> tokenList;
    private int curPos;
    private final int len;
    private final SyntaxError syntaxError;

    public TokenHandler(ArrayList<Token> tokenList) {
        this.tokenList = tokenList;
        this.curPos = 0;
        this.len = tokenList.size();
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

    public CompUnit getSyntaxTreeRoot() {
        return compUnit();
    }

    public SyntaxError getErrorList() {
        return syntaxError;
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
                else this.syntaxError.addError(ErrorType.NoRightMiddle, getSymLine(-1));
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
            while (true) {
                if (SymTypeIs("RBRACE")) {
                    nextSym();
                    break;
                }
                syntaxNodes.add(constInitVal());
                if (SymTypeIs("COMMA")) nextSym();
            }
            if (syntaxNodes.size() == 1) initType = VarType.oneDimArray;
            else initType = VarType.twoDimArray;
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
            // 变量维度
            nextSym();
            while (true) { // 数组类型
                if (!SymTypeIs("LBRACK")) break;
                else nextSym();//掠过左边括号
                constExps.add(constExp());
                if (SymTypeIs("RBRACK")) nextSym();
                else this.syntaxError.addError(ErrorType.NoRightMiddle, getSymLine(-1));
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
        FuncFParams tempFuncFParams = null;
        Block tempBlock;
        tempFuncType = funcType();

        if (SymTypeIs("IDENFR")) {
            tempIdent = new Ident(getSym());
            nextSym();
            // 加入参数
            if (SymTypeIs("LPARENT")) {
                nextSym();
                if (!SymTypeIs("RPARENT")) {
                    int tempPos = this.curPos;
                    try {
                        tempFuncFParams = funcFParams();
                    } catch (Exception e) {
                        this.curPos = tempPos;
                        this.syntaxError.addError(ErrorType.NoRightSmall, getSymLine(-1));
                    }
                } else nextSym();
            } else throw new ParseError("[FuncDef Error] LPARENT");
        } else throw new ParseError("[FuncDef Error] FuncDef");
        tempBlock = block(false);
        return new FuncDef(tempFuncType, tempIdent, tempFuncFParams, tempBlock, getSymLine(-1));
    }

    private MainFuncDef mainFuncDef() throws ParseError {
        Block tempBlock;
        Ident mainToken;
        if (SymTypeIs("INTTK")) {
            nextSym();
            if (SymTypeIs("MAINTK")) {
                mainToken = new Ident(getSym());
                nextSym();
                if (SymTypeIs("LPARENT")) {
                    nextSym();
                    if (SymTypeIs("RPARENT")) nextSym();
                    else this.syntaxError.addError(ErrorType.NoRightSmall, getSymLine(-1));
                    tempBlock = block(false);
                } else throw new ParseError("[MainFuncDef Error] LPARENT");
            } else throw new ParseError("[MainFuncDef Error] MAINTK");
        } else throw new ParseError("[MainFuncDef Error] INTTK");
        return new MainFuncDef(mainToken, tempBlock, getSymLine(-1));
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
        if (SymTypeIs("RPARENT")) nextSym();
        else this.syntaxError.addError(ErrorType.NoRightSmall, getSymLine(-1));
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
                        } else nextSym();
                    }
                } else {
                    this.syntaxError.addError(ErrorType.NoRightMiddle, getSymLine(-1));
                    return new FuncFParam(new BType(), tempIdent, varType, constExps);
                }
            }
        } else throw new ParseError("[FuncFParam Error] INTTK");
        return new FuncFParam(new BType(), tempIdent, varType, constExps);
    }

    private Block block(boolean inLoop) throws ParseError {
        ArrayList<BlockItem> blockItems = new ArrayList<>();
        if (SymTypeIs("LBRACE")) {
            nextSym();
            while (!SymTypeIs("RBRACE")) {
                blockItems.add(blockItem(inLoop));
            }
        }
        nextSym();
        return new Block(blockItems);
    }

    private BlockItem blockItem(boolean inLoop) throws ParseError {
        if (SymTypeIs("CONSTTK") || SymTypeIs("INTTK")) {
            return new BlockItem(decl());
        } else return new BlockItem(stmt(inLoop));
    }

    private Stmt stmt(boolean inLoop) throws ParseError {
        Stmt.Type type;
        ArrayList<Stmt> stmts = new ArrayList<>();
        ArrayList<Exp> exps = new ArrayList<>();
        FormatString formatString;
        Cond tempCond;
        Block tempBlock;
        LVal tempLVal = null;
        // print
        if (SymTypeIs("PRINTFTK")) {
            Ident ident = new Ident(getSym());
            type = Stmt.Type.Print;
            nextSym();
            if (SymTypeIs("LPARENT")) {
                nextSym();
                if (SymTypeIs("STRCON")) {
                    formatString = new FormatString(getSym());
                    // 检测格式化字符串合法性
                    if (!formatString.check())
                        this.syntaxError.addError(ErrorType.IllegalSymbol, formatString.getLine());
                    // 跳过逗号
                    nextSym();
                    while (true) {
                        if (SymTypeIs("COMMA")) {
                            nextSym();
                            exps.add(exp());
                        } else {
                            if (!SymTypeIs("RPARENT"))
                                this.syntaxError.addError(ErrorType.NoRightSmall, getSym(-1).lineNum);
                            else nextSym();
                            break;
                        }
                    }
                    // 检测参数个数
                    if (SymTypeIs("SEMICN")) nextSym();
                    else this.syntaxError.addError(ErrorType.NoSemi, getSym(-1).lineNum);
                    return new Stmt(exps, formatString, type, ident);
                }
            }
        }
        // return
        else if (SymTypeIs("RETURNTK")) {
            type = Stmt.Type.Return;
            Ident tempIdent = new Ident(getSym());
            nextSym();
            if (!SymTypeIs("SEMICN")) {
                int tempPos = this.curPos;
                try {
                    exps.add(exp());
                } catch (Exception e) {
                    this.curPos = tempPos;
                }
                if (SymTypeIs("SEMICN")) nextSym();
                else this.syntaxError.addError(ErrorType.NoSemi, getSym(-1).lineNum);
            } else nextSym();
            return new Stmt(exps, type, tempIdent);
        }
        // break
        else if (SymTypeIs("BREAKTK")) {
            if (!inLoop) this.syntaxError.addError(ErrorType.BreakContinue, getSymLine(0));
            nextSym();
            type = Stmt.Type.Break;
            if (SymTypeIs("SEMICN")) nextSym();
            else this.syntaxError.addError(ErrorType.NoSemi, getSym(-1).lineNum);
            return new Stmt(type);
        }
        // continue
        else if (SymTypeIs("CONTINUETK")) {
            if (!inLoop) this.syntaxError.addError(ErrorType.BreakContinue, getSymLine(0));
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
                else this.syntaxError.addError(ErrorType.NoRightSmall, getSymLine(-1));
                stmts.add(stmt(true));
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
                else this.syntaxError.addError(ErrorType.NoRightSmall, getSymLine(-1));
                stmts.add(stmt(inLoop));
                if (SymTypeIs("ELSETK")) {
                    nextSym();
                    stmts.add(stmt(inLoop));
                    type = Stmt.Type.Branch_ELSE;
                } else {
                    type = Stmt.Type.Branch_IF;
                }
                return new Stmt(tempCond, stmts, type);
            }
        }
        // block
        else if (SymTypeIs("LBRACE")) {
            tempBlock = block(inLoop);
            type = Stmt.Type.Block;
            return new Stmt(type, tempBlock);
        } else {
            int tempPos = this.curPos;
            try {
                tempLVal = lVal();
            } catch (Exception ignored) {
            }
            // expression
            if (!SymTypeIs("ASSIGN")) {
                this.curPos = tempPos;
                type = Stmt.Type.Expression;
                if (!SymTypeIs("SEMICN")) exps.add(exp());
                if (SymTypeIs("SEMICN")) nextSym();
                else this.syntaxError.addError(ErrorType.NoSemi, getSymLine(-1));
                return new Stmt(exps, type, null);
            }
            // assign
            else {
                // getint()
                if (SymTypeIs("GETINTTK", 1)) {
                    type = Stmt.Type.AssignmentInput;
                    nextSym();
                    nextSym();
                    if (SymTypeIs("LPARENT")) {
                        nextSym();
                        if (SymTypeIs("RPARENT")) nextSym();
                        else this.syntaxError.addError(ErrorType.NoRightSmall, getSymLine(-1));

                        if (SymTypeIs("SEMICN")) nextSym();
                        else this.syntaxError.addError(ErrorType.NoSemi, getSymLine(-1));
                        return new Stmt(tempLVal, type);
                    }
                }
                // left value = exp
                else {
                    nextSym();
                    exps.add(exp());
                    type = Stmt.Type.AssignmentExp;
                    if (SymTypeIs("SEMICN")) nextSym();
                    else this.syntaxError.addError(ErrorType.NoSemi, getSymLine(-1));
                    return new Stmt(tempLVal, exps, type);
                }

            }
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
                while (SymTypeIs("LBRACK")) {
                    nextSym();
                    exps.add(exp());
                    if (SymTypeIs("RBRACK")) nextSym();
                    else this.syntaxError.addError(ErrorType.NoRightMiddle, getSymLine(-1));
                }
                if (exps.size() == 1) type = VarType.oneDimArray;
                else type = VarType.twoDimArray;
            } else nextSym();
            return new LVal(ident, type, exps);
        } else throw new ParseError("[LVal Error] IDENFR");
    }

    private PrimaryExp primaryExp() throws ParseError {
        if (SymTypeIs("INTCON")) return new PrimaryExp(number());
        else if (SymTypeIs("LPARENT")) {
            nextSym();
            Exp tempExp = exp();
            if (SymTypeIs("RPARENT")) nextSym();
            else this.syntaxError.addError(ErrorType.NoRightSmall, getSymLine(-1));
            return new PrimaryExp(tempExp);
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
                int tempPos = this.curPos;
                try {
                    funcRParams = funcRParams();
                    if (SymTypeIs("RPARENT")) nextSym();
                    else this.syntaxError.addError(ErrorType.NoRightSmall, getSymLine(-1));
                    return new UnaryExp(ident, funcRParams);
                } catch (Exception e) {
                    this.curPos = tempPos;
                    this.syntaxError.addError(ErrorType.NoRightSmall, getSymLine(-1));
                    return new UnaryExp(ident);
                }
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
        if (SymTypeIs("PLUS")) return new UnaryOp(UnaryOp.Type.PLUS);
        else if (SymTypeIs("MINU")) return new UnaryOp(UnaryOp.Type.MINUS);
        else if (SymTypeIs("NOT")) return new UnaryOp(UnaryOp.Type.NOT);
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
