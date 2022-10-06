package SyntaxAnalyzer.SymbolTable;

import SyntaxAnalyzer.element.*;

import java.util.ArrayList;

public class SymbolTable {
    public ArrayList<Symbol> symbolList;
    public SymbolTable father;
    public ArrayList<SymbolTable> children;

    public SymbolTable(SymbolTable father) {
        this.symbolList = new ArrayList<>();
        this.children = new ArrayList<>();
        this.father = father;
    }

    public SymbolTable newSon() {
        SymbolTable son = new SymbolTable(this);
        this.children.add(son);
        return son;
    }

    // 加入普通变量
    public void addSymbol(Ident ident, boolean isConst, ConstInitVal constInitVal,
                          InitVal initVal, int numOfBracket) {
        Symbol symbol = ident.toSymbol();
        symbol.setConst(isConst);
        symbol.setConstInitVal(constInitVal);
        symbol.setInitVal(initVal);

        if (numOfBracket == 0) symbol.setType(Symbol.Type.var);
        else if (numOfBracket == 1) symbol.setType(Symbol.Type.oneDimArray);
        else if (numOfBracket == 2) symbol.setType(Symbol.Type.twoDimArray);
        else symbol.setType(Symbol.Type.var); // 维度不可能超过三维
        symbolList.add(symbol);
    }

    // 加入函数
    public void addSymbol(Ident ident, FuncType returnType, int paramsNum, FuncFParams funcFParams) {
        Symbol symbol = ident.toSymbol();
        symbol.setType(Symbol.Type.func);
        symbol.setReturnType(returnType);
        symbol.setParamsNum(paramsNum);
        symbol.setFuncFParams(funcFParams);
        symbolList.add(symbol);
    }

    // 加入函数参数
    public void addSymbol(Ident ident, VarType type) {
        Symbol symbol = ident.toSymbol();
        if (type == VarType.Var) symbol.setType(Symbol.Type.var);
        if (type == VarType.oneDimArray) symbol.setType(Symbol.Type.oneDimArray);
        if (type == VarType.twoDimArray) symbol.setType(Symbol.Type.twoDimArray);
        symbolList.add(symbol);
    }

    public SymbolTable back() {
        return this.father;
    }

    public boolean isDuplicateCurField(Ident ident) {
        for (Symbol symbol : symbolList) {
            if (symbol.name.equals(ident.token.value)) {
                return true;
            }
        }
        return false;
    }

    public Symbol isExistUpField(String name) {
        SymbolTable curTable = this;
        while (curTable != null) {
            for (Symbol symbol : curTable.symbolList) {
                if (symbol.name.equals(name)) {
                    return symbol;
                }
            }
            curTable = curTable.father;
        }
        return null;
    }

    public boolean checkIsConst(String name) {
        SymbolTable curTable = this;
        while (curTable != null) {
            for (Symbol symbol : symbolList) {
                if (symbol.name.equals(name)) {
                    return symbol.isConst;
                }
            }
            curTable = curTable.father;
        }
        return false;
    }

    public boolean checkFatherFuncType(int size) {
        SymbolTable curTable = this.father;
        while (curTable != null) {
            int listSize = curTable.symbolList.size() - 1;
            Symbol lastItem = curTable.symbolList.get(listSize);
            if (lastItem.type == Symbol.Type.func) {
                return lastItem.returnType.type.equals("void") && size == 0;
            }
            curTable = curTable.father;
        }
        System.out.println("checkFatherFuncType Error");
        return false;
    }

    public void getAll(SymbolTable symbolTable) {
        for (SymbolTable s : symbolTable.children) {
            getAll(s);
        }
        System.out.println(symbolTable);
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        for (Symbol symbol : symbolList) {
            res.append(symbol);
        }
        return res.toString();
    }
}
