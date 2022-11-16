import Backend.CodeGen;
import Front.LexicalAnalyzer.Scanner;
import Front.SyntaxAnalyzer.TokenHandler;
import Middle.IRBuilder;
import Middle.IRElement.Basic.Module;
import Utils.FileUtils;

import java.io.IOException;

public class Compiler {
    public static void main(String[] args) {
        try {
            // frontend code generation
            String src = FileUtils.readFile("testfile.txt");
            Scanner p = new Scanner(src);
            p.getSymbol();
            TokenHandler tokenHandler = new TokenHandler(p.getTokenArrayList());
            IRBuilder irBuilder = new IRBuilder(tokenHandler.getSyntaxTreeRoot());
            String ir = irBuilder.getIR();
            FileUtils.toFile(ir, "llvm_ir.txt");
            // backend code generation
            Module irModule = irBuilder.getCurrentModule();
            CodeGen mipsGen = new CodeGen(irModule);
            String mips = mipsGen.genMips();
            FileUtils.toFile(mips, "mips.txt");
        } catch (IOException e) {

            throw new RuntimeException(e);
        }
    }
}