package SyntaxAnalyzer.element;

import LexicalAnalyzer.Token;

public class FormatString extends SyntaxNode {

    private final Token token;

    public FormatString(Token token) {
        this.token = token;
    }

    public int getLine() {
        return token.lineNum;
    }

    public boolean check() {
        for (int i = 1; i < token.value.length() - 1; i++) {
            char elem = token.value.charAt(i);
            if (elem == '%' && i != token.value.length() - 2) {
                if (token.value.charAt(i + 1) != 'd') return false;
            } else if (elem == '\\' && i != token.value.length() - 2) {
                if (token.value.charAt(i + 1) != 'n') return false;
            } else if (!(elem == 32 || elem == 33 || (elem >= 40 && elem <= 126))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        res.append(token.toString());
        res.append("\n");
        return res.toString();
    }
}
