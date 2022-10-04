package SyntaxAnalyzer.element;

import LexicalAnalyzer.Token;

public class IntConst {
    private Token token;
    public IntConst(Token token) {
        this.token = token;
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        res.append(token.toString());
        res.append("\n");
//        res.append("<IntConst>\n");
        return res.toString();
    }
}
