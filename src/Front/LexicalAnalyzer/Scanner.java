package Front.LexicalAnalyzer;

import java.util.ArrayList;

public class Scanner {
    public String sourceCode;
    private int curPos;
    private final int len;
    private int curLine;

    private final ArrayList<Token> tokenArrayList;

    public ArrayList<Token> getTokenArrayList() {
        return tokenArrayList;
    }

    public Scanner(String source) {
        sourceCode = source;
        curPos = 0;
        curLine = 1;
        len = sourceCode.length();
        tokenArrayList = new ArrayList<>();
        CategoryCode.init();
    }

    private boolean isLast() {
        return len == curPos;
    }

    private int getChar() {
        if (isLast()) return -1;
        char res = sourceCode.charAt(curPos++);
        if (res == '\n') {
            curLine++;
        }
        return res;
    }

    private void backward() {
        if (curPos > 0) {
            curPos--;
            if (sourceCode.charAt(curPos) == '\n') curLine--;
        }
    }

    private void errorMsg() {
        System.out.println("[LexicalError] There is a lexical error at line " + curLine);
    }

    public void getSymbol() {
        StringBuilder token = new StringBuilder();
        while (!isLast()) {
            token.delete(0, token.length());
            int newChar = ' ';
            while (Character.isWhitespace(newChar) && !isLast()) newChar = getChar();
            if (Character.isLetter(newChar) || newChar == '_') {
                while (Character.isLetter(newChar) || Character.isDigit(newChar) || newChar == '_') {
                    token.append((char) newChar);
                    newChar = getChar();
                    if (newChar == -1) {
                        errorMsg();
                        return;
                    }
                }
                backward();
                if (CategoryCode.isReservedWord(token.toString())) {
                    tokenArrayList.add(new Token(token.toString(), CategoryCode.name2code.get(token.toString()), curLine));
                } else {
                    tokenArrayList.add(new Token(token.toString(), "IDENFR", curLine));
                }
            } else if (Character.isDigit(newChar)) {
                while (Character.isDigit(newChar)) {
                    token.append((char) newChar);
                    newChar = getChar();
                    if (newChar == -1) {
                        errorMsg();
                        return;
                    }
                }
                backward();
                tokenArrayList.add(new Token(token.toString(), "INTCON", curLine));
            } else if (CategoryCode.isOneSymbol((char) newChar)) {
                token.append((char) newChar);
                tokenArrayList.add(new Token(token.toString(), CategoryCode.name2code.get(token.toString()), curLine));
            } else if (newChar == '<' || newChar == '>' || newChar == '=' || newChar == '!') {
                token.append((char) newChar);
                newChar = getChar();
                if (newChar == -1) {
                    errorMsg();
                    return;
                }

                if (newChar != '=') {
                    backward();
                } else token.append((char) newChar);
                tokenArrayList.add(new Token(token.toString(), CategoryCode.name2code.get(token.toString()), curLine));
            } else if (newChar == '"') {
                token.append((char) newChar);
                newChar = getChar();
                token.append((char) newChar);
                while (newChar != '"' && !isLast()) {
                    newChar = getChar();
                    token.append((char) newChar);
                }
                tokenArrayList.add(new Token(token.toString(), "STRCON", curLine));
            } else if (newChar == '/') {
                newChar = getChar();
                if (newChar == '/') {
                    while (newChar != '\n' && !isLast()) {
                        newChar = getChar();
                    }
                } else if (newChar == '*') {
                    while (true) {
                        newChar = getChar();
                        if (newChar == -1) {
                            errorMsg();
                            return;
                        }
                        if (newChar == '*') {
                            newChar = getChar();
                            if (newChar == '/') break;
                            else backward();
                        }
                    }
                } else {
                    backward();
                    tokenArrayList.add(new Token("/", CategoryCode.name2code.get("/"), curLine));
                }
            } else if (newChar == '&') {
                token.append((char) newChar);
                newChar = getChar();
                if (newChar == '&') {
                    token.append((char) newChar);
                    tokenArrayList.add(new Token(token.toString(), CategoryCode.name2code.get(token.toString()), curLine));
                }
            } else if (newChar == '|') {
                token.append((char) newChar);
                newChar = getChar();
                if (newChar == '|') {
                    token.append((char) newChar);
                    tokenArrayList.add(new Token(token.toString(), CategoryCode.name2code.get(token.toString()), curLine));
                }
            }
        }
    }

    public String generate() {
        getSymbol();
        StringBuilder res = new StringBuilder();
        for (Token token : tokenArrayList) {
            res.append(token.toString());
            res.append("\n");
        }
        return res.toString();
    }

    public static void main(String[] args) {

    }
}
