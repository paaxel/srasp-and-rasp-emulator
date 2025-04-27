package it.palex.rasp;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.palex.rasp.executors.RaspInterpreterVisitor;
import it.palex.rasp.inout.InputReader;
import it.palex.rasp.inout.OutputObserver;
import it.palex.rasp.lexer.Lexer;
import it.palex.rasp.parser.Parser;
import it.palex.rasp.parser.operations.generic.MachineInstruction;

public class EmulatorFacade {

	private InputStream sourceCode;
	private OutputObserver output;
	private InputReader reader;
	
	private static final Logger LOGGER = LogManager.getLogger(EmulatorFacade.class);
	
	public EmulatorFacade(InputStream sourceCode, OutputObserver output, InputReader reader) {
		this.sourceCode = sourceCode;
		this.output = output;
		this.reader = reader;
	}
	
	
	public void executeProgram() throws IOException, InterruptedException {
		LOGGER.info("Emulator is starting...");
		
		Lexer lexer = new Lexer(this.sourceCode);
		
		//parse phase
		Parser parser = new Parser(lexer);
		List<MachineInstruction> instructions = parser.parseProgram();
		
		RaspInterpreterVisitor interpreter = new RaspInterpreterVisitor(instructions, 
				this.output, this.reader);
		
		interpreter.startInterpretation();
		
		reader.close();
		
		LOGGER.info("Emulator is shutting down...");
	}
	
	
	
	
	
	
	
	
}
