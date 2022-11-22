import Front.LexicalAnalyzer.Scanner;
import Front.SyntaxAnalyzer.TokenHandler;
import Middle.IRBuilder;
import Utils.FileUtils;

public class CompilerTest {
    public static void main(String[] args) {
        for (int i = 1; i <= 100; i++) {
            try {
                System.out.println("file: " + i);
                String src = FileUtils.readFile("test/public/testfile" + i + ".txt");
                Scanner p = new Scanner(src);
                p.getSymbol();
                TokenHandler tokenHandler = new TokenHandler(p.getTokenArrayList());
                IRBuilder irBuilder = new IRBuilder(tokenHandler.getSyntaxTreeRoot());
                String ir = irBuilder.getIR();
                FileUtils.toFile(ir, "output/output" + i + ".txt");
            } catch (Exception e) {
//                e.printStackTrace();
                System.out.println("error at file " + i);
            }
        }
    }
}
