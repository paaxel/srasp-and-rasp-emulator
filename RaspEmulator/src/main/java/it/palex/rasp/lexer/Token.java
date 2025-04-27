package it.palex.rasp.lexer;

public class Token {
	
	private final TokenType type;
	private final String value;
	private int line;
	
	public Token(TokenType type, String value, int line) {
		this.type = type;
		this.value = value;
		this.line = line;
	}

	public TokenType getType() {
		return type;
	}

	public String getValue() {
		return value;
	}

	/**
	 * 
	 * @return the line where the token was found in the source file
	 */
	public int getLine() {
		return line;
	}

	@Override
	public String toString() {
		return "Token [type=" + type + ", value=" + value + ", line=" + line + "]";
	}

	
}
