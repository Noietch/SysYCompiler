import Front.LexicalAnalyzer.Parser;
import Front.SyntaxAnalyzer.TokenHandler;
import Middle.IRBuilder;
import Utils.FileUtils;

public class CompilerTest {
    public static void main(String[] args) {
        for (int i = 1; i <= 30; i++) {
            try {
                System.out.println("file: " + i);
                String src = FileUtils.readFile("test/2022/C/testfile" + i + ".txt");
                Parser p = new Parser(src);
                p.getSymbol();
                TokenHandler tokenHandler = new TokenHandler(p.getTokenArrayList());
                IRBuilder irBuilder = new IRBuilder(tokenHandler.getSyntaxTreeRoot());
                String ir = irBuilder.getIR();
                FileUtils.toFile(ir, "output/output" + i + ".txt");

            } catch (Exception e) {
                System.out.println("error at file " + i);
            }
        }
    }
}
