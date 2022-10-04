import LexicalAnalyzer.Parser;
import SyntaxAnalyzer.TokenHandler;
import Utils.FileUtils;

import java.io.IOException;

public class CompilerTest {
    public static void main(String[] args) {
        for (int i=1;i<=30;i++){
            try {
                String src = FileUtils.readFile("test/2021/testfiles-only/A/testfile"+i+".txt");
                Parser p = new Parser(src);
                p.getSymbol();
                TokenHandler th = new TokenHandler(p.getTokenArrayList());
                String dist = th.getSymList();
                FileUtils.toFile(dist,"output/output"+i+".txt");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
