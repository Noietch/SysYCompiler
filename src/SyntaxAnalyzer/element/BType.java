package SyntaxAnalyzer.element;

public class BType extends SyntaxNode{
    public String type;
    public BType() {
        this.type = "int";
    }

    @Override
    public String toString() {
        return "INTTK int\n";
    }
}
