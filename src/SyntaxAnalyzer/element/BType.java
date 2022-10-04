package SyntaxAnalyzer.element;

public class BType extends SyntaxNode{
    private String type;
    public BType() {
        this.type = "int";
    }

    @Override
    public String toString() {
        return "INTTK int\n";
    }
}
