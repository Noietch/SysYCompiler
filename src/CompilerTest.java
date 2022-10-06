import LexicalAnalyzer.Parser;
import SyntaxAnalyzer.TokenHandler;
import Utils.FileUtils;

import java.io.IOException;

public class CompilerTest {
    public static void main(String[] args) {
        for (int i = 1; i <= 30; i++) {
            try {
                System.out.println("file: " + i);
                String src = FileUtils.readFile("test/B/testfile" + i + ".txt");
                Parser p = new Parser(src);
                p.getSymbol();
                TokenHandler th = new TokenHandler(p.getTokenArrayList());
                String dist = th.getSymList();
                FileUtils.toFile(dist, "output/output" + i + ".txt");
                FileUtils.diff("output/output" + i + ".txt","answer/B/output" + i + ".txt");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
