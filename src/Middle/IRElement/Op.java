package Middle.IRElement;

import Front.LexicalAnalyzer.Token;

public class Op {
    public enum Type {
        add,
        sub,
        mul,
        sdiv,
        sgt,
        sge,
        slt,
        sle,
        eq,
        ne,
        and,
        or,
        Error
    }

    public Type type;

    public Op(Type type) {
        this.type = type;
    }

    static public Type Op2Type(Token token) {
        if (token.value.equals("+")) return Type.add;
        if (token.value.equals("-")) return Type.sub;
        if (token.value.equals("*")) return Type.mul;
        if (token.value.equals("/")) return Type.sdiv;
        if (token.value.equals(">")) return Type.sgt;
        if (token.value.equals("<")) return Type.slt;
        if (token.value.equals(">=")) return Type.sge;
        if (token.value.equals("<=")) return Type.sle;
        if (token.value.equals("==")) return Type.eq;
        if (token.value.equals("!=")) return Type.ne;
        if (token.value.equals("&&")) return Type.and;
        if (token.value.equals("||")) return Type.or;
        return Type.Error;
    }

    @Override
    public String toString() {
        return type.toString();
    }
}
