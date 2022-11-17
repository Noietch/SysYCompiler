import Backend.CodeGen;
import Front.LexicalAnalyzer.Scanner;
import Front.SyntaxAnalyzer.TokenHandler;
import Middle.IRBuilder;
import Middle.IRElement.Basic.Module;
import Optimize.Optimizer;
import Utils.FileUtils;

public class CompilerTest {
    public static void main(String[] args) {
        for (int i = 1; i <= 30; i++) {
            try {
                System.out.println("file: " + i);
                String src = FileUtils.readFile("test/2022/C/testfile" + i + ".txt");
                Scanner p = new Scanner(src);
                p.getSymbol();
                TokenHandler tokenHandler = new TokenHandler(p.getTokenArrayList());
                IRBuilder irBuilder = new IRBuilder(tokenHandler.getSyntaxTreeRoot());
                String ir = irBuilder.getIR();
                Module irModule = irBuilder.getCurrentModule();
//                Optimizer optimizer = new Optimizer(irModule);
//                optimizer.ReorganizePrintParam();
                // backend code generation
                CodeGen mipsGen = new CodeGen(irModule);
                String mips = mipsGen.genMips();
                FileUtils.toFile(ir, "output/output" + i + ".txt");
            } catch (Exception e) {
                e.printStackTrace();
//                System.out.println("error at file " + i);
            }
        }
    }
}
