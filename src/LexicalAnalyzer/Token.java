package LexicalAnalyzer;

public class Token {
    public String value;
    public String categoryCode;
    public int lineNum;

    public Token(String value, String categoryCode, int lineNum) {
        this.value = value;
        this.categoryCode = categoryCode;
        this.lineNum = lineNum;
    }

    @Override
    public String toString() {
        return categoryCode + " " + value;
    }
}