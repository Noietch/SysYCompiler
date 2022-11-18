package Front.SyntaxAnalyzer.Element;

import Front.LexicalAnalyzer.Token;

public class FormatString extends SyntaxNode {

    public final Token token;

    public FormatString(Token token) {
        this.token = token;
    }

    public int getLine() {
        return token.lineNum;
    }

    public boolean check() {
        for (int i = 1; i < token.value.length() - 1; i++) {
            char elem = token.value.charAt(i);
            if (elem == '%') {
                return token.value.charAt(i + 1) == 'd';
            } else if (elem == '\\') {
                return token.value.charAt(i + 1) == 'n';
            } else if (!(elem == 32 || elem == 33 || (elem >= 40 && elem <= 126))) {
                return false;
            }
        }
        return true;
    }

    public int paramNum(){
        int res = 0;
        for (int i = 1; i < token.value.length() - 1; i++) {
            if(token.value.charAt(i) == '%' && token.value.charAt(i+1) == 'd'){
                res ++;
            }
        }
        return res;
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        res.append(token.toString());
        res.append("\n");
        return res.toString();
    }
}
