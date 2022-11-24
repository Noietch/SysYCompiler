import Backend.NaiveCodeGen.CodeGen;
import Backend.OptimizeCodeGen.OptimizeCodeGen;
import Front.LexicalAnalyzer.Scanner;
import Front.SyntaxAnalyzer.Element.CompUnit;
import Front.SyntaxAnalyzer.ErrorHandler;
import Front.SyntaxAnalyzer.TokenHandler;
import Middle.IRBuilder;
import Middle.IRElement.Basic.Module;
import Optimize.Optimizer;
import Utils.FileUtils;

import java.io.IOException;

public class Compiler {
    public static void main(String[] args) {
        try {
            // frontend code generation
            String src = FileUtils.readFile(CompilerConfig.TEST_FILE);
            Scanner p = new Scanner(src);
            p.getSymbol();
            TokenHandler tokenHandler = new TokenHandler(p.getTokenArrayList());
            CompUnit syntaxTreeRoot = tokenHandler.getSyntaxTreeRoot();
            // error handler default is false
            if(CompilerConfig.ERROR_HANDLER){
                ErrorHandler errorHandler = new ErrorHandler(syntaxTreeRoot,tokenHandler.getErrorList());
                String error = errorHandler.getErrorList();
                FileUtils.toFile(error, CompilerConfig.ERROR_FILE);
            }
            // llvm ir generation
            IRBuilder irBuilder = new IRBuilder(syntaxTreeRoot);
            String ir = irBuilder.getIR();
            FileUtils.toFile(ir, CompilerConfig.LLVM_IR_FILE);
            // backend code generation
            Module irModule = irBuilder.getCurrentModule();
            if(CompilerConfig.OPTIMIZE){
                // llvm ir optimize
                Optimizer optimizer = new Optimizer(irModule);
                optimizer.instructionOptimize();
                String optIr = irModule.toString();
                FileUtils.toFile(optIr, CompilerConfig.LLVM_OPT_IR_FILE);
                // optimize backend code generation
                OptimizeCodeGen mipsGen = new OptimizeCodeGen(irModule);
                String mips = mipsGen.genMips();
                FileUtils.toFile(mips, CompilerConfig.MIPS_FILE);
            }else {
                // naive backend code generation
                CodeGen mipsGen = new CodeGen(irModule);
                String mips = mipsGen.genMips();
                FileUtils.toFile(mips, CompilerConfig.MIPS_FILE);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}