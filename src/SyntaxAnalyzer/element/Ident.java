package SyntaxAnalyzer.element;

import LexicalAnalyzer.Token;

public class Ident extends SyntaxNode{
    private final Token token;
    public Ident(Token token) {
        this.token = token;
    }
    @Override
    public String toString() {
        return token.toString() +
                "\n";
    }
}
