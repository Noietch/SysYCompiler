package LexicalAnalyzer;

import java.util.HashMap;
import java.util.Map;

public class CategoryCode {
    static public Map<String, String> name2code = new HashMap<>();

    static public String[] name = {"Ident", "IntConst", "FormatString", "main", "const",
            "int", "break", "continue", "if", "else", "!", "&&", "||", "while",
            "getint", "printf", "return", "+", "-", "void", "*", "/", "%", "<",
            "<=", ">", ">=", "==", "!=", "=", ";", ",", "(", ")", "[", "]", "{", "}"};
    static public String[] code = {"IDENFR", "INTCON", "STRCON", "MAINTK", "CONSTTK", "INTTK",
            "BREAKTK", "CONTINUETK", "IFTK", "ELSETK", "NOT", "AND", "OR", "WHILETK", "GETINTTK",
            "PRINTFTK", "RETURNTK", "PLUS", "MINU", "VOIDTK", "MULT", "DIV", "MOD", "LSS", "LEQ",
            "GRE", "GEQ", "EQL", "NEQ", "ASSIGN", "SEMICN", "COMMA", "LPARENT", "RPARENT", "LBRACK",
            "RBRACK", "LBRACE", "RBRACE"};

    static public String[] reservedWord = {"main", "const", "int", "break", "continue", "if", "else",
            "while", "getint", "printf", "return", "void"};

    static public Character[] OneSymbol = {'+', '-', '*', '%', ';', ',',
            '(', ')', '[', ']', '{', '}'};

    static public void init() {
        // init the Name2Code dict
        for (int i = 0; i < name.length; i++) {
            name2code.put(name[i], code[i]);
        }
    }

    static public boolean isReservedWord(String word) {
        boolean res = false;
        for (String w : reservedWord) {
            res |= word.equals(w);
        }
        return res;
    }

    static public boolean isOneSymbol(char word) {
        boolean res = false;
        for (Character c : OneSymbol) {
            res |= c == word;
        }
        return res;
    }

    public static void main(String[] args) {
        CategoryCode c = new CategoryCode();
        System.out.println(CategoryCode.name2code);
    }
}
