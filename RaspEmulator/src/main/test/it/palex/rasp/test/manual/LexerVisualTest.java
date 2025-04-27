package it.palex.rasp.test.manual;

import java.io.IOException;
import java.io.InputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.palex.rasp.lexer.Lexer;
import it.palex.rasp.lexer.Token;
import it.palex.rasp.lexer.TokenType;

public class LexerVisualTest {

	private static final Logger LOGGER = LogManager.getLogger(LexerVisualTest.class);
	
	public static void main(String[] args) throws IOException {
		InputStream srcFileInputStream = LexerVisualTest.class.getResourceAsStream("/it.palex.rasp.examples/primo.rasp");


		LOGGER.info("Emulator is starting...\n");
		
		Lexer lexer = new Lexer(srcFileInputStream);
		
		Token currentToken = lexer.nextToken();
		
		while(currentToken.getType()!=TokenType.EOF) {
			LOGGER.info(currentToken);
			currentToken = lexer.nextToken();
		}
		
		LOGGER.info(currentToken);
	}

}
