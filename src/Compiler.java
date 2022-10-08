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
            String symbols = errorHandler.syntaxTreeRoot.toString().trim();
            String error = errorHandler.getErrorList().trim();
            System.out.println(error);
            FileUtils.toFile(error,"error.txt");
            FileUtils.toFile(symbols,"output.txt");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}