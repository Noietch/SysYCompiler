package SyntaxAnalyzer;

import java.util.ArrayList;

enum ErrorType {
    IllegalSymbol,
    MultiDefinition,
    Undefined,
    ParamNumber,
    ParamClass,
    WrongReturn,
    NoReturn,
    AssignConst,
    NoSemi,
    NoRightSmall,
    NoRightMiddle,
    PrintNum,
    BreakContinue;

    public static String toCode(ErrorType type) {
        if (type == IllegalSymbol) return "a";
        if (type == MultiDefinition) return "b";
        if (type == Undefined) return "c";
        if (type == ParamNumber) return "d";
        if (type == ParamClass) return "e";
        if (type == WrongReturn) return "f";
        if (type == NoReturn) return "g";
        if (type == AssignConst) return "h";
        if (type == NoSemi) return "i";
        if (type == NoRightSmall) return "j";
        if (type == NoRightMiddle) return "k";
        if (type == PrintNum) return "l";
        if (type == BreakContinue) return "m";
        return null;
    }
}

class Error {
    public ErrorType type;
    public Integer ErrorLine;

    public Error(ErrorType type, Integer errorLine) {
        this.type = type;
        ErrorLine = errorLine;
    }

    @Override
    public String toString() {
        return ErrorType.toCode(this.type) + " " + this.ErrorLine + "\n";
    }
}


public class SyntaxError {
    ArrayList<Error> errors;

    public SyntaxError() {
        this.errors = new ArrayList<>();
    }

    public void addError(ErrorType type, Integer line) {
        this.errors.add(new Error(type, line));
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        for (Error error : errors) {
            res.append(error);
        }
        return res.toString();
    }
}

class ParseError extends Exception {  // 自定义的类
    ParseError(String s) {
        super(s);
    }
}
