import Backend.CodeGen;
import Front.LexicalAnalyzer.Scanner;
import Front.SyntaxAnalyzer.TokenHandler;
import Middle.IRBuilder;
import Middle.IRCodeSwaper;
import Utils.FileUtils;

import java.io.IOException;

public class Compiler {
    public static void main(String[] args) {
        try {
            String src = FileUtils.readFile("testfile.txt");
            Scanner p = new Scanner(src);
            p.getSymbol();
            TokenHandler tokenHandler = new TokenHandler(p.getTokenArrayList());
            IRBuilder irBuilder = new IRBuilder(tokenHandler.getSyntaxTreeRoot());
            String ir = irBuilder.getIR();
            FileUtils.toFile(ir, "llvm_ir.txt");
//            IRCodeSwaper.swapPrint(irBuilder.currentModule);
            System.out.println(irBuilder.currentModule);
            CodeGen mipsGen = new CodeGen(irBuilder.currentModule);
            String mips = mipsGen.genMips();
            FileUtils.toFile(mips, "mips.txt");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}