import LexicalAnalyzer.Parser;
import SyntaxAnalyzer.TokenHandler;
import Utils.FileUtils;
import java.io.IOException;

public class Compiler {
    public static void main(String[] args) {
        try {
            String src = FileUtils.readFile("testfile.txt");
            Parser p = new Parser(src);
            p.getSymbol();
            TokenHandler th = new TokenHandler(p.getTokenArrayList());
            th.getSymList();
            String dist = th.getErrorList();
            System.out.println(dist);
//            FileUtils.toFile(dist,"error.txt");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}