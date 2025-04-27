package it.palex.rasp.lexer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

import it.palex.rasp.executors.exception.UnknownTokenException;

public class Lexer {
	
	private static final String LETTERS_REGEX = "[a-zA-Z]*";
	private static final String NUMBER_REGEX = "[-+]?[0-9]*";
	private static final String NUMBER_WITHOUT_SIGN_REGEX = "[0-9]*";
	private static final String KEYWORD_REGEX = "(ADD|SUB|MUL|DIV|HALT|JZ|JNZ|JGZ|JLZ|JLEZ|JGEZ|JUMP|LOAD|STORE|READ|WRITE)";
		
	private final InputStream toCompile;
	private BufferedReader srcFileBR;
	private StringTokenizer strTokenizer;
	private int sourceFileLine;
	
	
	public Lexer(final InputStream toCompile) {
		if (toCompile==null) {
			throw new NullPointerException("sourceFileName null at Lexer constructor");
		}
		
		this.toCompile = toCompile;
		this.srcFileBR = new BufferedReader(new InputStreamReader(toCompile));
		this.sourceFileLine = 0;
	}
	
	public void resetLexer() throws IOException {
		this.toCompile.reset();
		this.srcFileBR = new BufferedReader(new InputStreamReader(toCompile));
		this.strTokenizer = null;
		this.sourceFileLine = 0;
	}
	
	/**
	 * 
	 * @return the new line of file if there is another line in the file otherwise null if EOF is reached
	 * @throws IOException 
	 */
	private String moveToNextLine() throws IOException {
		this.sourceFileLine++;
		return this.srcFileBR.readLine();
	}
	
	/**
	 * 
	 * @return the next token. If there is no next token and EOF token will be return
	 * @throws IOException 
	 */
	public Token nextToken() throws IOException {
		String nextLine = null;
		
		if(this.strTokenizer==null || !this.strTokenizer.hasMoreTokens()) {
			boolean lineRead = false;
			
			do {
				nextLine = this.moveToNextLine();
				if(nextLine==null) {
					this.srcFileBR.close();
					return new Token(TokenType.EOF, "<eof>", this.sourceFileLine);
				}
				
				nextLine = nextLine.trim();
				
				//consider empty string or string with spaces like end of line
				if(nextLine.equals("")) {
					return new Token(TokenType.EOL, "<eol>", this.sourceFileLine);
				}
				
				boolean returnDelimiters = true;
				this.strTokenizer = new StringTokenizer(nextLine, " :#@\t", returnDelimiters);
				lineRead= true;
				
			// use while and not an if to skip empty string line
			}while(!this.strTokenizer.hasMoreTokens());
			
			if(lineRead) {
				return new Token(TokenType.EOL, "<eol>", this.sourceFileLine);
			}
		}
		
		
		String currentToken = this.strTokenizer.nextToken();
		
		//we can skip all separators because we are sure that we can't have an empty string to tokenize
		while(currentToken.charAt(0)==' ' || currentToken.charAt(0)=='\t') {
			currentToken = this.strTokenizer.nextToken();
		}
		
		switch(currentToken.charAt(0)) {
			case ':': 
				return new Token(TokenType.END_LABEL, ":", this.sourceFileLine);
			case '#':
				return new Token(TokenType.MODE, "#", this.sourceFileLine);
			case '@':
				return new Token(TokenType.MODE, "@", this.sourceFileLine);
			default:
				break;
		}
		
		
		if (currentToken.matches(KEYWORD_REGEX)) {
			return new Token(TokenType.KEYWORD, currentToken, this.sourceFileLine);
        }
		
		if (currentToken.matches(LETTERS_REGEX)) {
			return new Token(TokenType.LETTERS, currentToken, this.sourceFileLine);
        }
		
		if (currentToken.matches(NUMBER_WITHOUT_SIGN_REGEX)) {
            return new Token(TokenType.NUMBER_WITHOUT_SIGN, currentToken, this.sourceFileLine);
        }
		
        if (currentToken.matches(NUMBER_REGEX)) {
            return new Token(TokenType.NUMBER, currentToken, this.sourceFileLine);
        }
        
        throw new UnknownTokenException("Unknown Token found in source file at line: "
        		+this.sourceFileLine+", near --> "+currentToken);
	}
	
	

}
