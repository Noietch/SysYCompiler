package SyntaxAnalyzer.element;

import LexicalAnalyzer.Token;
import SyntaxAnalyzer.SymbolTable.Symbol;

public class Ident extends SyntaxNode {
    private final Token token;

    public Ident(Token token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return token.toString() +
                "\n";
    }

    public Symbol toSymbol() {
        return new Symbol(token.value, token.lineNum);
    }

    public String getValue() {
        return token.value;
    }
}
