import LexicalAnalyzer.Parser;
import SyntaxAnalyzer.ErrorHandler;
import SyntaxAnalyzer.TokenHandler;
import Utils.FileUtils;

import java.io.IOException;

public class CompilerTest {
    public static void main(String[] args) {
        for (int i = 1; i <= 30; i++) {
            try {
                System.out.println("file: " + i);
                String src = FileUtils.readFile("test/2022/B/testfile" + i + ".txt");
                Parser p = new Parser(src);
                p.getSymbol();
                TokenHandler tokenHandler = new TokenHandler(p.getTokenArrayList());
                ErrorHandler errorHandler = new ErrorHandler(tokenHandler.getSyntaxTreeRoot(),tokenHandler.getErrorList());
                errorHandler.travelSyntaxTree(errorHandler.syntaxTreeRoot);
                String dist = errorHandler.getErrorList().trim();
                String symbols = errorHandler.syntaxTreeRoot.toString().trim();
                FileUtils.toFile(symbols, "output/output" + i + ".txt");
                FileUtils.diff("output/output" + i + ".txt","answer/2022/B/output" + i + ".txt");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
