package it.palex.rasp.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import it.palex.rasp.executors.exception.ParsingException;
import it.palex.rasp.lexer.Lexer;
import it.palex.rasp.lexer.Token;
import it.palex.rasp.lexer.TokenType;
import it.palex.rasp.parser.operations.DirectArgument;
import it.palex.rasp.parser.operations.Identifier;
import it.palex.rasp.parser.operations.InstructionBlock;
import it.palex.rasp.parser.operations.NumberValue;
import it.palex.rasp.parser.operations.ReferenceArgument;
import it.palex.rasp.parser.operations.StandardArgument;
import it.palex.rasp.parser.operations.generic.MachineInstruction;
import it.palex.rasp.parser.operations.instructions.AddDirectInstruction;
import it.palex.rasp.parser.operations.instructions.AddReferenceInstruction;
import it.palex.rasp.parser.operations.instructions.AddStandardInstruction;
import it.palex.rasp.parser.operations.instructions.DivDirectInstruction;
import it.palex.rasp.parser.operations.instructions.DivReferenceInstruction;
import it.palex.rasp.parser.operations.instructions.DivStandardInstruction;
import it.palex.rasp.parser.operations.instructions.HaltInstruction;
import it.palex.rasp.parser.operations.instructions.JGEZInstruction;
import it.palex.rasp.parser.operations.instructions.JGZInstruction;
import it.palex.rasp.parser.operations.instructions.JLEZInstruction;
import it.palex.rasp.parser.operations.instructions.JLZInstruction;
import it.palex.rasp.parser.operations.instructions.JNZInstruction;
import it.palex.rasp.parser.operations.instructions.JUMPInstruction;
import it.palex.rasp.parser.operations.instructions.JUMPReferenceInstruction;
import it.palex.rasp.parser.operations.instructions.JZInstruction;
import it.palex.rasp.parser.operations.instructions.LoadDirectInstruction;
import it.palex.rasp.parser.operations.instructions.LoadReferenceInstruction;
import it.palex.rasp.parser.operations.instructions.LoadStandardInstruction;
import it.palex.rasp.parser.operations.instructions.MulDirectInstruction;
import it.palex.rasp.parser.operations.instructions.MulReferenceInstruction;
import it.palex.rasp.parser.operations.instructions.MulStandardInstruction;
import it.palex.rasp.parser.operations.instructions.ReadReferenceInstruction;
import it.palex.rasp.parser.operations.instructions.ReadStandardInstruction;
import it.palex.rasp.parser.operations.instructions.StoreReferenceInstruction;
import it.palex.rasp.parser.operations.instructions.StoreStandardInstruction;
import it.palex.rasp.parser.operations.instructions.SubDirectInstruction;
import it.palex.rasp.parser.operations.instructions.SubReferenceInstruction;
import it.palex.rasp.parser.operations.instructions.SubStandardInstruction;
import it.palex.rasp.parser.operations.instructions.WriteDirectInstruction;
import it.palex.rasp.parser.operations.instructions.WriteReferenceInstruction;
import it.palex.rasp.parser.operations.instructions.WriteStandardInstruction;

public class Parser {
	private final static String INSTRUCTION_LABELS = "ADD|SUB|MUL|DIV|HALT|JZ|JNZ|JGZ|JLZ|JLEZ|JGEZ|JUMP|LOAD|STORE|READ|WRITE";
	private final Lexer lexer;
	private Token currentToken = null;
	
	private static final String DIRECT_MODE = "#";
	private static final String REFERENCED_MODE = "@";

	
	public Parser(Lexer lexer) {
		if(lexer==null) {
			throw new NullPointerException("Null lexer at Parser constructor");
		}
		this.lexer = lexer;
	}

	public List<MachineInstruction> parseProgram() throws IOException {
		 this.currentToken = this.lexer.nextToken();
		 
		 List<MachineInstruction> instructions = new ArrayList<>();
		 
		 while (this.currentToken.getType() != TokenType.EOF) {
			
			//skip multiple end of line
			while(this.currentToken.getType()==TokenType.EOL) {
				this.currentToken = this.lexer.nextToken();
			}
			
			if(this.currentToken.getType() == TokenType.EOF) {
				return instructions;
			}
			
			instructions.add(parseInstruction());
		 }
		 
		 return instructions;
	}

	private boolean isAnIdentifier(Token token) {
		return token.getType() == TokenType.LETTERS || 
				token.getType() == TokenType.NUMBER_WITHOUT_SIGN;
	}
	
	private boolean isANumber(Token token) {
		return token.getType() == TokenType.NUMBER || 
				token.getType() == TokenType.NUMBER_WITHOUT_SIGN;
	}
	
	private MachineInstruction parseInstruction() throws IOException {
		// if is not a reserved word
		if (this.currentToken.getType() != TokenType.KEYWORD) {
			return parseInstructionBlock();
	    }else {
	    	return parseSingleInstruction();
	    }
	}
	
	private MachineInstruction parseInstructionBlock() throws IOException {
		String blockLabel = this.currentToken.getValue();
				
		this.currentToken = this.lexer.nextToken();
		
		if(this.currentToken.getType() != TokenType.END_LABEL) {
			throw new ParsingException("Unexpected Parameter found at line "+
						this.currentToken.getLine()+" near --> "+this.currentToken.getValue()+"."
								+ "Expected: "+TokenType.END_LABEL);
		}
		
		List<MachineInstruction> instructionInBlock = new ArrayList<>();
		int instructionBlockStartLine = this.currentToken.getLine();
		
		this.currentToken = this.lexer.nextToken();
		
		//skip end of line
		while(this.currentToken.getType()==TokenType.EOL) {
			this.currentToken = this.lexer.nextToken();
		}
		
		// is mandatory if a label is added at least one operation must be specified in block
		if(this.currentToken.getType() != TokenType.KEYWORD) {
			throw new ParsingException("Unexpected Parameter found at line "+
					this.currentToken.getLine()+" near --> "+this.currentToken.getValue()+". Expected: INSTRUCTION");
		}
		
		
		while(this.currentToken.getType() == TokenType.KEYWORD) {
			instructionInBlock.add(parseSingleInstruction());
		}
		
		InstructionBlock block = new InstructionBlock(instructionInBlock, 
				new Identifier(blockLabel), instructionBlockStartLine);
		
		return block;
	}
	
	private MachineInstruction parseSingleInstruction() throws IOException {
		if(this.currentToken.getType() != TokenType.KEYWORD) {
			throw new ParsingException("Expected an instruction label at line "+this.currentToken.getLine()
					+ " near "+this.currentToken+". Instruction labels are-->"+INSTRUCTION_LABELS);
		}
		
		String operation = this.currentToken.getValue();
		MachineInstruction res = null;
		
		switch(operation){
			case "ADD":
				res = this.parseAddInstruction();
				break;
			case "SUB":
				res = this.parseSubInstruction();
				break;
			case "MUL":
				res = this.parseMulInstruction();
				break;
			case "DIV":
				res = this.parseDivInstruction();
				break;
			case "HALT":
				res = this.parseHaltInstruction();
				break;
			case "JZ":
				res = this.parseJzInstruction();
				break;
			case "JNZ":
				res = this.parseJnzInstruction();
				break;
			case "JGZ":
				res = this.parseJgzInstruction();
				break;
			case "JLZ":
				res = this.parseJlzInstruction();
				break;
			case "JLEZ":
				res = this.parseJlezInstruction();
				break;
			case "JGEZ":
				res = this.parseJgezInstruction();
				break;
			case "JUMP":
				res = this.parseJumpInstruction();
				break;
			case "LOAD":
				res = this.parseLoadInstruction();
				break;
			case "STORE":
				res = this.parseStoreInstruction();
				break;
			case "READ":
				res = this.parseReadInstruction();
				break;
			case "WRITE":
				res = this.parseWriteInstruction();
				break;
			 default:
				 throw new RuntimeException("Parser error. operation="+operation);
		}
		
		
		this.currentToken = this.lexer.nextToken();
		
		//if end of file is reached return the parsed operation
		if(this.currentToken.getType()==TokenType.EOF) {
			return res;
		}
		
		if(this.currentToken.getType()!=TokenType.EOL) {
			throw new ParsingException("Expected End of line at line "+this.currentToken.getLine()
											+" near --> "+this.currentToken.getValue());
		}
		
		this.currentToken = this.lexer.nextToken();
		
		return res;
	}

		
	private MachineInstruction parseWriteInstruction() throws IOException {
		int tokenLine = this.currentToken.getLine();
		this.currentToken = this.lexer.nextToken();
		
		if(this.currentToken.getType()==TokenType.MODE) {
			String mode = this.currentToken.getValue();
			
			if(mode.equals(DIRECT_MODE)) {
				this.currentToken = this.lexer.nextToken();
				return new WriteDirectInstruction(this.parseDirectArgument(), tokenLine);
			}
			
			if(mode.equals(REFERENCED_MODE)) {
				this.currentToken = this.lexer.nextToken();
				return new WriteReferenceInstruction(this.parseReferenceArgument(), tokenLine);
			}
			
			throw new ParsingException("Unsupported Mode found at line "+this.currentToken.getLine()+""
					+ " near --> "+tokenLine);
		}
				
		return new WriteStandardInstruction(this.parseStandardArgument(), this.currentToken.getLine());
	}

	
	private MachineInstruction parseLoadInstruction() throws IOException {
		int tokenLine = this.currentToken.getLine();
		this.currentToken = this.lexer.nextToken();
		
		if(this.currentToken.getType()==TokenType.MODE) {
			String mode = this.currentToken.getValue();
			
			if(mode.equals(DIRECT_MODE)) {
				this.currentToken = this.lexer.nextToken();
				return new LoadDirectInstruction(this.parseDirectArgument(), tokenLine);
			}
			
			if(mode.equals(REFERENCED_MODE)) {
				this.currentToken = this.lexer.nextToken();
				return new LoadReferenceInstruction(this.parseReferenceArgument(), tokenLine);
			}
			
			throw new ParsingException("Unsupported Mode found at line "+tokenLine+""
					+ " near --> "+this.currentToken.getValue());
		}
				
		return new LoadStandardInstruction(this.parseStandardArgument(), tokenLine);
	}
	
	private MachineInstruction parseReadInstruction() throws IOException {
		int tokenLine = this.currentToken.getLine();
		this.currentToken = this.lexer.nextToken();
		
		if(this.currentToken.getType()==TokenType.MODE) {
			String mode = this.currentToken.getValue();
						
			if(mode.equals(REFERENCED_MODE)) {
				this.currentToken = this.lexer.nextToken();
				return new ReadReferenceInstruction(this.parseReferenceArgument(), 
						tokenLine);
			}
			
			throw new ParsingException("Unsupported Mode found at line "+tokenLine+""
					+ " near --> "+this.currentToken.getValue());
		}
				
		return new ReadStandardInstruction(this.parseStandardArgument(), tokenLine);
	}

	private MachineInstruction parseStoreInstruction() throws IOException {
		int tokenLine = this.currentToken.getLine();
		this.currentToken = this.lexer.nextToken();
		
		if(this.currentToken.getType()==TokenType.MODE) {
			String mode = this.currentToken.getValue();
						
			if(mode.equals(REFERENCED_MODE)) {
				this.currentToken = this.lexer.nextToken();
				return new StoreReferenceInstruction(this.parseReferenceArgument(), 
						tokenLine);
			}
			
			throw new ParsingException("Unsupported Mode found at line "+tokenLine+""
					+ " near --> "+this.currentToken.getValue());
		}
				
		return new StoreStandardInstruction(this.parseStandardArgument(), tokenLine);
	}

	

	private MachineInstruction parseJumpInstruction() throws IOException {
		int tokenLine = this.currentToken.getLine();
		this.currentToken = this.lexer.nextToken();
		
		if(this.currentToken.getType()==TokenType.MODE) {
			String mode = this.currentToken.getValue();
			
			if(mode.equals(REFERENCED_MODE)) {
				this.currentToken = this.lexer.nextToken();
				return new JUMPReferenceInstruction(this.parseReferenceArgument(), 
						tokenLine);
			}
			
			throw new ParsingException("Unsupported Mode found at line "+tokenLine+""
					+ " near --> "+this.currentToken.getValue());
		}
		
		return new JUMPInstruction(this.parseStandardArgument(), tokenLine);
	}

	private MachineInstruction parseJgezInstruction() throws IOException {
		int tokenLine = this.currentToken.getLine();
		this.currentToken = this.lexer.nextToken();
		
		return new JGEZInstruction(this.parseStandardArgument(), tokenLine);
	}

	private MachineInstruction parseJlezInstruction() throws IOException {
		int tokenLine = this.currentToken.getLine();
		this.currentToken = this.lexer.nextToken();
		
		return new JLEZInstruction(this.parseStandardArgument(), tokenLine);
	}

	private MachineInstruction parseJlzInstruction() throws IOException {
		int tokenLine = this.currentToken.getLine();
		this.currentToken = this.lexer.nextToken();
		
		return new JLZInstruction(this.parseStandardArgument(), tokenLine);
	}

	private MachineInstruction parseJgzInstruction() throws IOException {
		int tokenLine = this.currentToken.getLine();
		this.currentToken = this.lexer.nextToken();
		
		return new JGZInstruction(this.parseStandardArgument(), tokenLine);
	}

	private MachineInstruction parseJnzInstruction() throws IOException {
		int tokenLine = this.currentToken.getLine();
		this.currentToken = this.lexer.nextToken();
		
		return new JNZInstruction(this.parseStandardArgument(), tokenLine);
	}

	private MachineInstruction parseJzInstruction() throws IOException {
		int tokenLine = this.currentToken.getLine();
		this.currentToken = this.lexer.nextToken();
		
		return new JZInstruction(this.parseStandardArgument(), tokenLine);
	}

	private MachineInstruction parseHaltInstruction() throws IOException {
		HaltInstruction instruction = new HaltInstruction(this.currentToken.getLine());
		
		return instruction;
	}

	private MachineInstruction parseAddInstruction() throws IOException {	
		int tokenLine = this.currentToken.getLine();
		this.currentToken = this.lexer.nextToken();
		
		if(this.currentToken.getType()==TokenType.MODE) {
			String mode = this.currentToken.getValue();
			
			if(mode.equals(DIRECT_MODE)) {
				this.currentToken = this.lexer.nextToken();
				return new AddDirectInstruction(this.parseDirectArgument(), 
						tokenLine);
			}
			
			if(mode.equals(REFERENCED_MODE)) {
				this.currentToken = this.lexer.nextToken();
				return new AddReferenceInstruction(this.parseReferenceArgument(), 
						tokenLine);
			}
			
			throw new ParsingException("Unsupported Mode found at line "+tokenLine+""
					+ " near --> "+this.currentToken.getValue());
		}
				
		return new AddStandardInstruction(this.parseStandardArgument(), tokenLine);		
	}
	
	private MachineInstruction parseSubInstruction() throws IOException {
		int tokenLine = this.currentToken.getLine();
		this.currentToken = this.lexer.nextToken();
		
		if(this.currentToken.getType()==TokenType.MODE) {
			String mode = this.currentToken.getValue();
			
			if(mode.equals(DIRECT_MODE)) {
				this.currentToken = this.lexer.nextToken();
				return new SubDirectInstruction(this.parseDirectArgument(), 
						tokenLine);
			}
			
			if(mode.equals(REFERENCED_MODE)) {
				this.currentToken = this.lexer.nextToken();
				return new SubReferenceInstruction(this.parseReferenceArgument(), 
						tokenLine);
			}
			
			throw new ParsingException("Unsupported Mode found at line "+tokenLine+""
					+ " near --> "+this.currentToken.getValue());
		}
				
		return new SubStandardInstruction(this.parseStandardArgument(), tokenLine);		
	}
	
	private MachineInstruction parseMulInstruction() throws IOException {
		int tokenLine = this.currentToken.getLine();
		this.currentToken = this.lexer.nextToken();
		
		if(this.currentToken.getType()==TokenType.MODE) {
			String mode = this.currentToken.getValue();
			
			if(mode.equals(DIRECT_MODE)) {
				this.currentToken = this.lexer.nextToken();
				return new MulDirectInstruction(this.parseDirectArgument(), 
						tokenLine);
			}
			
			if(mode.equals(REFERENCED_MODE)) {
				this.currentToken = this.lexer.nextToken();
				return new MulReferenceInstruction(this.parseReferenceArgument(), 
						tokenLine);
			}
			
			throw new ParsingException("Unsupported Mode found at line "+tokenLine+""
					+ " near --> "+this.currentToken.getValue());
		}
				
		return new MulStandardInstruction(this.parseStandardArgument(), tokenLine);		
	}
	
	private MachineInstruction parseDivInstruction() throws IOException {	
		int tokenLine = this.currentToken.getLine();
		this.currentToken = this.lexer.nextToken();
		
		if(this.currentToken.getType()==TokenType.MODE) {
			String mode = this.currentToken.getValue();
			
			if(mode.equals(DIRECT_MODE)) {
				this.currentToken = this.lexer.nextToken();
				return new DivDirectInstruction(this.parseDirectArgument(), tokenLine);
			}
			
			if(mode.equals(REFERENCED_MODE)) {
				this.currentToken = this.lexer.nextToken();
				return new DivReferenceInstruction(this.parseReferenceArgument(), 
						tokenLine);
			}
			
			throw new ParsingException("Unsupported Mode found at line "+tokenLine+""
					+ " near --> "+tokenLine);
		}
				
		return new DivStandardInstruction(this.parseStandardArgument(), tokenLine);		
	}
	
		
	private DirectArgument parseDirectArgument() throws IOException {		
		if(this.isANumber(this.currentToken)) {
			Integer value = Integer.parseInt(this.currentToken.getValue());
			
			return new DirectArgument(new NumberValue(value));
		}
		
		throw new ParsingException("Unexpected argument found at line "+this.currentToken.getLine()+""
				+ " near --> "+this.currentToken.getValue()+". Expected: NUMBER");
	}
	
	private ReferenceArgument parseReferenceArgument() throws IOException {
		if(this.isAnIdentifier(this.currentToken)) {
			return new ReferenceArgument(new Identifier(this.currentToken.getValue()));
		}
		
		throw new ParsingException("Unexpected argument found at line "+this.currentToken.getLine()+""
				+ " near --> "+this.currentToken.getValue()+". Expected: Identifier");
	}
	
	private StandardArgument parseStandardArgument() throws IOException {		
		if(this.isAnIdentifier(this.currentToken)) {
			return new StandardArgument(new Identifier(this.currentToken.getValue()));
		}
		
		throw new ParsingException("Unexpected argument found at line: "+this.currentToken.getLine()+""
				+ ", near --> "+this.currentToken.getValue()+". Expected: Identifier");
	}
	
	
	
}
