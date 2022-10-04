package SyntaxAnalyzer.SymbolTable;

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

    public void newSon(){
        SymbolTable son = new SymbolTable(this);
        this.children.add(son);
    }

}
