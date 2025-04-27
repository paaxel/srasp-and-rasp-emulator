package it.palex.rasp.test.manual;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.palex.rasp.lexer.Lexer;
import it.palex.rasp.parser.Parser;
import it.palex.rasp.parser.operations.generic.MachineInstruction;

public class ParserVisualTest {

private static final Logger LOGGER = LogManager.getLogger(ParserVisualTest.class);
	
	public static void main(String[] args) throws IOException {
		InputStream srcFileInputStream = ParserVisualTest.class.getResourceAsStream("/it.palex.rasp.examples/primo.rasp");
		
		LOGGER.info("Emulator is starting...\n");
		
		Lexer lexer = new Lexer(srcFileInputStream);
		
		Parser parser = new Parser(lexer);
		List<MachineInstruction> operations = parser.parseProgram();
		
		LOGGER.info(operations.toString());
		
		LOGGER.info("Emulator is starting...\n");
	}
	
}
