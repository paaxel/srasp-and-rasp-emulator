package it.palex.srasp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Scanner;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import it.palex.rasp.utils.FileUtility;
import it.palex.srasp.compiler.SimplerRaspNewCompiler;
import it.palex.srasp.lang.SimplerRaspLexer;
import it.palex.srasp.lang.SimplerRaspParser;
import it.palex.srasp.parser.SyntaxErrorListener;

public class StarterMain {

	public static void main(String[] args) throws IOException {
		Scanner sc = new Scanner(System.in);

		String srcFileName = null;
		File sourceFile = null;
		
		String dstFileName = null;
		File destFile = null;
		
		boolean invalidFileGiven = true;

		while (invalidFileGiven) {
			invalidFileGiven = false;

			System.out.println("Enter the name of the source file >> ");
			srcFileName = sc.nextLine();
			sourceFile = new File(srcFileName);

			if (!sourceFile.exists()) {
				System.out.println("File non esistente! Ti preghiamo di riprovare...");
				invalidFileGiven = true;
			} else {
				if (!FileUtility.checkIfIsAFileWithExtension(sourceFile, "srasp")) {
					System.out.println("File does not exist! Please try again...");
					invalidFileGiven = true;
				}
			}
			
			System.out.println("Enter the name of the compiled file >> ");
			dstFileName = sc.nextLine();
			destFile = new File(dstFileName);
			
			if (!FileUtility.checkFileExtension(destFile, "rasp")) {
				System.out.println("The file must have a .rasp extension. Please try again...");
				invalidFileGiven = true;
			}
		}
		
	    InputStream stream = new FileInputStream(srcFileName);
	    
		CharStream inputCharStream = CharStreams.fromStream(stream);
		SimplerRaspLexer lexer = new SimplerRaspLexer(inputCharStream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		SimplerRaspParser parser = new SimplerRaspParser(tokens);
				
		SyntaxErrorListener listener = new SyntaxErrorListener();
        parser.addErrorListener(listener);
        
        ParseTree three = parser.program();

        if(listener.getSyntaxErrors()!=null && !listener.getSyntaxErrors().isEmpty()) {
        	System.out.println("Parsing errors --> "+listener.getSyntaxErrors());
        }else {
        	SimplerRaspNewCompiler compiler = new SimplerRaspNewCompiler();
    		compiler.visit(three);
    		String compiledProgram = compiler.getCompiledProgram();
    		PrintWriter pw = new PrintWriter(destFile);
    		pw.append(compiledProgram);
    		pw.close();
        }		
		
        sc.close();
	}


}
