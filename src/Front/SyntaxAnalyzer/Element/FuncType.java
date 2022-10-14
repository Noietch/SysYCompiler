package Front.SyntaxAnalyzer.Element;

public class FuncType extends SyntaxNode{
    public final String type;

    public FuncType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        if(type.equals("int")){
            res.append("INTTK int\n");
        }
        else res.append("VOIDTK void\n");
        res.append("<FuncType>\n");
        return res.toString();
    }
}
