import LexicalAnalyzer.Parser;
import SyntaxAnalyzer.ErrorHandler;
import SyntaxAnalyzer.TokenHandler;
import Utils.FileUtils;
import java.io.IOException;

public class Compiler {
    public static void main(String[] args) {
        try {
            String src = FileUtils.readFile("testfile.txt");
            Parser p = new Parser(src);
            p.getSymbol();
            TokenHandler tokenHandler = new TokenHandler(p.getTokenArrayList());
            ErrorHandler errorHandler = new ErrorHandler(tokenHandler.getSyntaxTreeRoot(),tokenHandler.getErrorList());
            errorHandler.travelSyntaxTree(errorHandler.syntaxTreeRoot);
//            errorHandler.getSymbolTable();
            System.out.println(errorHandler.getErrorList());
//            String error = th.getErrorList();
//            th.getSymbolTable();
//            System.out.println(symbolTable);
//            System.out.println(error);
//            FileUtils.toFile(symbol,"output.txt");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}