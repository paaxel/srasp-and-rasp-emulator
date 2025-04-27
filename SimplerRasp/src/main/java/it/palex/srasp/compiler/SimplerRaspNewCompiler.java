package it.palex.srasp.compiler;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import it.palex.srasp.compiler.exeptions.AlreadyDefinedIdentifier;
import it.palex.srasp.compiler.exeptions.ArgumentNumberMismatchException;
import it.palex.srasp.compiler.exeptions.FunctionAlreadyDefinedException;
import it.palex.srasp.compiler.exeptions.FunctionNotDefinedException;
import it.palex.srasp.compiler.exeptions.NotDefinedIdentifier;
import it.palex.srasp.compiler.exeptions.TypeMismatchException;
import it.palex.srasp.lang.SimplerRaspParser;
import it.palex.srasp.lang.SimplerRaspParserBaseVisitor;
import it.palex.srasp.lang.SimplerRaspParser.FunctionCallInputVariablesContext;
import it.palex.srasp.lang.SimplerRaspParser.FunctionDeclarationContext;
import it.palex.srasp.lang.SimplerRaspParser.FunctionDeclarationInputVariablesContext;
import it.palex.srasp.lang.SimplerRaspParser.FuntionCallVariableContext;
import it.palex.srasp.lang.SimplerRaspParser.FuntionInputVariableContext;

public class SimplerRaspNewCompiler extends SimplerRaspParserBaseVisitor<CompileNode> {

	private static final String ADD = "ADD";
	private static final String ADD_DIRECT = ADD + "#";
	private static final String SUB = "SUB";
	private static final String SUB_DIRECT = SUB + "#";
	private static final String DIV = "DIV";
	private static final String DIV_DIRECT = DIV + "#";
	private static final String MUL = "MUL";
	private static final String MUL_DIRECT = MUL + "#";

	private static final String STORE = "STORE";
	private static final String STORE_BY_REFERENCE = STORE + "@";
	
	private static final String LOAD = "LOAD";
	private static final String LOAD_DIRECT = LOAD + "#";
	private static final String LOAD_BY_REFERENCE = LOAD + "@";

	private static final String WRITE = "WRITE";
	private static final String WRITE_DIRECT = "WRITE#";

	private static final String READ = "READ";

	private static final String JZ = "JZ";
	private static final String JNZ = "JNZ";
	private static final String JGZ = "JGZ";
	private static final String JLZ = "JLZ";
	private static final String JLEZ = "JLEZ";
	private static final String JGEZ = "JGEZ";
	private static final String JUMP = "JUMP";
	private static final String JUMP_BY_REFERENCE = JUMP+"@";
	
	private static final String HALT = "HALT";
	
	private static final String STACK_POINTER = "SP";
	
	private static final String ERROR_LABEL = "ERROR";
	private static final String TERMINATE_LABEL = "TERMINATE";
	private static final String MAIN_LABEL = "MAIN";
	
	
	
	private LayeredMemory memory;
	private StringBuilder compiledProgram;

	public SimplerRaspNewCompiler() {
		this.memory = new LayeredMemory();
		this.compiledProgram = new StringBuilder(1024);
		UniqueIdentifierManager.getInstance().reset();
	}

	public String getCompiledProgram() {
		return this.compiledProgram.toString();
	}

	private void appendNewInstruction(String instruction, String operator) {
		this.compiledProgram.append("\t");
		this.compiledProgram.append(instruction);
		this.compiledProgram.append(" ");
		this.compiledProgram.append(operator);
		this.compiledProgram.append("\n");
	}

	private void appendNewLabel(String instruction) {
		this.compiledProgram.append(instruction);
		this.compiledProgram.append(":");
		this.compiledProgram.append("\n");
	}
	
	
	@Override
	public CompileNode visitProgram(SimplerRaspParser.ProgramContext ctx) {
		//initialize stack pointer to zero
		this.appendNewInstruction(LOAD_DIRECT, "0");
		this.appendNewInstruction(STORE, STACK_POINTER);
				
		this.appendNewInstruction(JUMP, MAIN_LABEL);
		
		//previsit function declaration to discover the type returned and the arguments
		for (FunctionDeclarationContext elem: ctx.functionDeclaration()) {
			this.previsitFunctionDeclaration(elem);
		}
		
		for (FunctionDeclarationContext elem: ctx.functionDeclaration()) {
			this.visitFunctionDeclaration(elem);
		}
		
		visitMainFunction(ctx.mainFunction());

		return new CompileNode(NodeType.PROGRAM);
	}
	
	
	
	@Override
	public CompileNode visitMainFunction(SimplerRaspParser.MainFunctionContext ctx) {
		this.memory.pushNewBlock();
		
		this.memory.setReturnBlockLabel(TERMINATE_LABEL);
		this.memory.setReturnBlockType(NodeType.VOID);
		
		this.appendNewLabel(MAIN_LABEL);
		
		visitBlock(ctx.block());

		this.appendNewInstruction(JUMP, TERMINATE_LABEL);

		this.appendNewLabel(ERROR_LABEL);
		this.appendNewInstruction(WRITE_DIRECT, Integer.MIN_VALUE+"");

		this.appendNewLabel(TERMINATE_LABEL);
		this.appendNewInstruction(HALT, "");

		return new CompileNode(NodeType.MAIN_FUNCTION);
	}
	
	
	@Override
	public CompileNode visitBlock(SimplerRaspParser.BlockContext ctx) {
		this.memory.pushNewBlock();

		this.visitChildren(ctx);

		Block block = this.memory.popBlock();
		freeStackCells(block.getVariableDeclared());		
				
		return new CompileNode(NodeType.BLOCK);
	}
	
	@Override
	public CompileNode visitFunctionBlock(SimplerRaspParser.FunctionBlockContext ctx) {
		this.memory.pushNewBlock();

		this.visitChildren(ctx);

		Block block = this.memory.popBlock();
		freeStackCells(block.getVariableDeclared());		
				
		return new CompileNode(NodeType.BLOCK);
	}
	
	@Override
	public CompileNode visitBlockStatement(SimplerRaspParser.BlockStatementContext ctx) {
		this.visitChildren(ctx);

		return new CompileNode(NodeType.BLOCK_STATEMENT);
	}
	
	
	private void loadInAccExistantNodeInStack(String varIdentifier, CompileNode oldNode) {
		//trovare la profondità totale in base a dove è stato definito
		int totalVariableDeclared = this.memory.getTotalVariableDefinedAfterNode(varIdentifier);
		
		int relativeMemoryPosition = totalVariableDeclared - oldNode.getRelativeStackIndexPosition();
		
		CompileNode tempVariable = new CompileNode(NodeType.NUMBER);
		this.memory.addNewTempNode(tempVariable);

		this.appendNewInstruction(LOAD, STACK_POINTER);
		this.appendNewInstruction(SUB_DIRECT, ""+relativeMemoryPosition);
		this.appendNewInstruction(STORE, STACK_POINTER);
		
		//store the accumulator
		this.appendNewInstruction(LOAD_BY_REFERENCE, STACK_POINTER);
		this.appendNewInstruction(STORE, tempVariable.getUniqueNodeId());
		
		//fix stack pointer
		this.appendNewInstruction(LOAD, STACK_POINTER);
		this.appendNewInstruction(ADD_DIRECT, ""+relativeMemoryPosition);
		this.appendNewInstruction(STORE, STACK_POINTER);
		
		//reload variable in the accumulator
		this.appendNewInstruction(LOAD, tempVariable.getUniqueNodeId());
	}
	
	private void storeAccValueInExistantNodeInStack(String varIdentifier, CompileNode oldNode) {
		int totalVariableDeclared = this.memory.getTotalVariableDefinedAfterNode(varIdentifier);
		
		int relativeMemoryPosition = totalVariableDeclared - oldNode.getRelativeStackIndexPosition();
		
		CompileNode tempVariable = new CompileNode(NodeType.NUMBER);
		this.memory.addNewTempNode(tempVariable);
		//store accumulator
		this.appendNewInstruction(STORE, tempVariable.getUniqueNodeId());
		
		this.appendNewInstruction(LOAD, STACK_POINTER);
		this.appendNewInstruction(SUB_DIRECT, ""+relativeMemoryPosition);
		this.appendNewInstruction(STORE, STACK_POINTER);
		//store variable in memory
		this.appendNewInstruction(LOAD, tempVariable.getUniqueNodeId());
		this.appendNewInstruction(STORE_BY_REFERENCE, STACK_POINTER);
		
		//fix stack pointer
		this.appendNewInstruction(LOAD, STACK_POINTER);
		this.appendNewInstruction(ADD_DIRECT, ""+relativeMemoryPosition);
		this.appendNewInstruction(STORE, STACK_POINTER);
		
		//reload variable in the accumulator
		this.appendNewInstruction(LOAD, tempVariable.getUniqueNodeId());
	}
	
	private void storeAccValueInNewNodeInStack(String nodeId, CompileNode valNode) {
		this.memory.addNewStackNode(nodeId, valNode);
		
		this.appendNewInstruction(STORE_BY_REFERENCE, STACK_POINTER);
		
		this.moveStackPointerAheadByOne();
	}
	
	private void freeStackCells(int cellNumber) {
		if(cellNumber>0) {
			this.appendNewInstruction(LOAD, STACK_POINTER);
			this.appendNewInstruction(SUB_DIRECT, cellNumber+"");
			this.appendNewInstruction(STORE, STACK_POINTER);
		}
	}
	
	
	private void moveStackPointerAheadByOne() {
		CompileNode tempVariable = new CompileNode(NodeType.NUMBER);
		this.memory.addNewTempNode(tempVariable);
		//store accumulator
		this.appendNewInstruction(STORE, tempVariable.getUniqueNodeId());
		
		this.appendNewInstruction(LOAD, STACK_POINTER);
		this.appendNewInstruction(ADD_DIRECT, "1");
		this.appendNewInstruction(STORE, STACK_POINTER);
		
		//check that the stack is not fully it it so jump to error
		this.appendNewInstruction(SUB_DIRECT, UniqueIdentifierManager.STACK_MAX_LENGTH+"");
		this.appendNewInstruction(JGZ, ERROR_LABEL);
		
		//reload accumulator
		this.appendNewInstruction(LOAD, tempVariable.getUniqueNodeId());
	}
	
	@Override
	public CompileNode visitDeclaration(SimplerRaspParser.DeclarationContext ctx) {
		String identifier = ctx.IDENTIFIER().getText();

		if (this.memory.hasAlreadyDefinedIdentifier(identifier)) {
			throw new AlreadyDefinedIdentifier("Already defined variable " + identifier, ctx.start.getLine());
		}
		NodeType declaredType = NodeType.valueOf(ctx.TYPE().getText().toUpperCase());
		
		CompileNode valNode = new CompileNode(declaredType);
		
		if (ctx.expression() != null) {
			CompileNode node = this.visit(ctx.expression());

			if (node.getType() != declaredType) {
				throw new TypeMismatchException("Cannot assign " + node.getType().toString().toLowerCase() + " to a "
						+ declaredType.toString().toLowerCase(), ctx.start.getLine());
			}
			
		} else {
			// default initialization to zero
			// zero if integer or false if boolean
			this.appendNewInstruction(LOAD_DIRECT,"0");
		}
		
		// ADD THE NODE IN MEMORY ONLY AFTER RIGHT EXPR IS EVALUATED
		this.storeAccValueInNewNodeInStack(identifier, valNode);

		return new CompileNode(NodeType.DECLARATION);
	}
	
	@Override
	public CompileNode visitAssignment(SimplerRaspParser.AssignmentContext ctx) {
		String identifier = ctx.IDENTIFIER().getText();

		CompileNode oldNode = this.memory.getNodeById(identifier);

		if (oldNode == null) {
			throw new NotDefinedIdentifier("Undefined variable " + identifier, ctx.start.getLine());
		}

		CompileNode newNode = this.visit(ctx.expression());

		if (oldNode.getType() != newNode.getType()) {
			throw new TypeMismatchException("Cannot assign " + newNode.getType().toString().toLowerCase() + " to a "
					+ oldNode.getType().toString().toLowerCase(), ctx.start.getLine());
		}
		
		this.storeAccValueInExistantNodeInStack(identifier, oldNode);

		return new CompileNode(NodeType.DECLARATION);
	}

	@Override
	public CompileNode visitSignedNumber(SimplerRaspParser.SignedNumberContext ctx) {
		//TODO
		if (ctx.SUB() != null) {
			this.appendNewInstruction(LOAD_DIRECT, "-"+ctx.NUMBER().getText());
		}else {//ctx.ADD()
			this.appendNewInstruction(LOAD_DIRECT, ctx.NUMBER().getText());
		}

		return new CompileNode(NodeType.NUMBER);
	}
	
	@Override
	public CompileNode visitBooleans(SimplerRaspParser.BooleansContext ctx) {
		if (ctx.TRUE() != null) {
			this.appendNewInstruction(LOAD_DIRECT, "1");
		} else { // ctx.FALSE()!=null
			this.appendNewInstruction(LOAD_DIRECT, "0");
		}

		return new CompileNode(NodeType.BOOL);
	}
	
	
	@Override
	public CompileNode visitSignedIdentifier(SimplerRaspParser.SignedIdentifierContext ctx) {
		String identifier = ctx.IDENTIFIER().getText();
		CompileNode variableNode = this.memory.getNodeById(identifier);

		if (variableNode == null) {
			throw new NotDefinedIdentifier("Found an undefined variable <" + identifier + ">", ctx.start.getLine());
		}
		
		this.loadInAccExistantNodeInStack(identifier, variableNode);
		
		if (ctx.SUB() != null || ctx.ADD()!=null) {
			if (variableNode.getType() != NodeType.NUMBER) {
				throw new TypeMismatchException(
						"Cannot use mathematical operations on term " + variableNode.getType().toString().toLowerCase(),
						ctx.start.getLine());
			}
		}
		
		if(ctx.SUB()!=null) {
			this.appendNewInstruction(MUL_DIRECT, "-1");
		}

		return variableNode;
	}
	
	
	@Override
	public CompileNode visitInOutStatement(SimplerRaspParser.InOutStatementContext ctx) {
		visitChildren(ctx);

		return new CompileNode(NodeType.IN_OUT_STATEMENT);
	}
	
	
	@Override
	public CompileNode visitInStatement(SimplerRaspParser.InStatementContext ctx) {
		String identifier = ctx.IDENTIFIER().getText();

		CompileNode oldNode = this.memory.getNodeById(identifier);

		if (oldNode == null) {
			throw new NotDefinedIdentifier("Found an undefined variable found", ctx.start.getLine());
		}

		CompileNode tempVariable = new CompileNode(NodeType.NUMBER);
		this.memory.addNewTempNode(tempVariable);
		//store temp variable
		this.appendNewInstruction(LOAD_DIRECT, "0");
		this.appendNewInstruction(STORE, tempVariable.getUniqueNodeId());
		
		
		this.appendNewInstruction(READ, tempVariable.getUniqueNodeId());
		
		if (oldNode.getType() == NodeType.BOOL) {
			// check if is an invalid boolean
			this.appendNewInstruction(LOAD, tempVariable.getUniqueNodeId());
			//this.loadInAccExistantNodeInStack(identifier, oldNode);
			this.appendNewInstruction(SUB_DIRECT, "1");
			// if 0 or 1 was read the with -1 the result must be <=0 (superior limit check)
			this.appendNewInstruction(JGZ, "ERROR");
			// if -1 or 0 adding 1 the result must be must be >=0 (inferior limit check)
			this.appendNewInstruction(ADD_DIRECT, "1");
			this.appendNewInstruction(JLZ, "ERROR");
		}
		
		//load variable
		this.appendNewInstruction(LOAD, tempVariable.getUniqueNodeId());
		
		this.storeAccValueInExistantNodeInStack(identifier, oldNode);

		return new CompileNode(NodeType.CIN_STATEMENT);
	}


	@Override
	public CompileNode visitOutStatement(SimplerRaspParser.OutStatementContext ctx) {
		this.visit(ctx.expression());

		CompileNode numberTerm = new CompileNode(NodeType.NUMBER);
		this.memory.addNewTempNode(numberTerm);

		this.appendNewInstruction(STORE, numberTerm.getUniqueNodeId());
		this.appendNewInstruction(WRITE, numberTerm.getUniqueNodeId());

		return new CompileNode(NodeType.COUT_STATEMENT);
	}
	
	@Override
	public CompileNode visitAddSubExpr(SimplerRaspParser.AddSubExprContext ctx) {
		CompileNode leftTerm = this.visit(ctx.expression(0));

		if (leftTerm.getType() != NodeType.NUMBER) {
			throw new TypeMismatchException(
					"Cannot use mathematical operations on term " + leftTerm.getType().toString().toLowerCase(),
					ctx.start.getLine());
		}
		// create temp node where you can store the result of the left expression
		CompileNode parkLeftTerm = new CompileNode(NodeType.NUMBER);
		String randomId = UniqueIdentifierManager.getInstance().getNewUniqueId();
		this.storeAccValueInNewNodeInStack(randomId, parkLeftTerm);
		
		CompileNode rightTerm = this.visit(ctx.expression(1));
		if (rightTerm.getType() != NodeType.NUMBER) {
			throw new TypeMismatchException(
					"Cannot use mathematical operations on term " + rightTerm.getType().toString().toLowerCase(),
					ctx.start.getLine());
		}

		if (ctx.SUB() != null) {
			// multiply the content of the accumulator
			this.appendNewInstruction(MUL_DIRECT, "-1");
		}
		
		CompileNode rightTermPark = new CompileNode(NodeType.NUMBER);
		this.memory.addNewTempNode(rightTermPark);
		this.appendNewInstruction(STORE, rightTermPark.getUniqueNodeId());
		
		this.loadInAccExistantNodeInStack(randomId, parkLeftTerm);
		this.appendNewInstruction(ADD, rightTermPark.getUniqueNodeId());
		this.appendNewInstruction(STORE, rightTermPark.getUniqueNodeId());
		
		this.freeStackCells(1);
		this.memory.decreseNumberOfVariableDeclared();

		this.appendNewInstruction(LOAD, rightTermPark.getUniqueNodeId());

		return new CompileNode(NodeType.NUMBER);
	}
	
	@Override
	public CompileNode visitMulDivRemExpr(SimplerRaspParser.MulDivRemExprContext ctx) {
		CompileNode leftTerm = this.visit(ctx.expression(0));

		if (leftTerm.getType() != NodeType.NUMBER) {
			throw new TypeMismatchException(
					"Cannot use mathematical operations on term " + leftTerm.getType().toString().toLowerCase(),
					ctx.start.getLine());
		}
		// create temp node where you can store the result of the left expression
		CompileNode parkLeftTerm = new CompileNode(NodeType.NUMBER);
		String randomId = UniqueIdentifierManager.getInstance().getNewUniqueId();
		this.storeAccValueInNewNodeInStack(randomId, parkLeftTerm);


		CompileNode rightTerm = this.visit(ctx.expression(1));
		if (rightTerm.getType() != NodeType.NUMBER) {
			throw new TypeMismatchException(
					"Cannot use mathematical operations on term " + rightTerm.getType().toString().toLowerCase(),
					ctx.start.getLine());
		}
		// create temp node where you can store the result of the right expression
		CompileNode parkRightTerm = new CompileNode(NodeType.NUMBER);
		this.memory.addNewTempNode(parkRightTerm);

		//STORE RIGHT TERM AND LOAD left term
		this.appendNewInstruction(STORE, parkRightTerm.getUniqueNodeId());
		this.loadInAccExistantNodeInStack(randomId, parkLeftTerm);
		
				
		if (ctx.MUL() != null) {
			// multiply the content of the accumulator			
			this.appendNewInstruction(MUL, parkRightTerm.getUniqueNodeId());
			this.appendNewInstruction(STORE, parkRightTerm.getUniqueNodeId());
		} else if (ctx.DIV() != null) {
			// load left term and execute division
			this.appendNewInstruction(DIV, parkRightTerm.getUniqueNodeId());
			this.appendNewInstruction(STORE, parkRightTerm.getUniqueNodeId());
		} else if (ctx.REMAINDER() != null) { // remainder is %
			/**
			 * To make the remainder is used the following logic. Ex: 11%2 --> acc=11%2. acc
			 * is 5. Now multiply park=acc*2 and substract to 11 11 - (5*2)
			 */
			this.appendNewInstruction(DIV, parkRightTerm.getUniqueNodeId());

			this.appendNewInstruction(MUL, parkRightTerm.getUniqueNodeId());
			// store division result in park
			CompileNode park = new CompileNode(NodeType.NUMBER);
			this.memory.addNewTempNode(park);
			this.appendNewInstruction(STORE, park.getUniqueNodeId());
			
			this.loadInAccExistantNodeInStack(randomId, parkLeftTerm);			
			this.appendNewInstruction(SUB, park.getUniqueNodeId());
			
			this.appendNewInstruction(STORE, parkRightTerm.getUniqueNodeId());
		}

		
		this.freeStackCells(1);
		this.memory.decreseNumberOfVariableDeclared();

		this.appendNewInstruction(LOAD, parkRightTerm.getUniqueNodeId());
		
		
		return new CompileNode(NodeType.NUMBER);
	}
	
	@Override 
	public CompileNode visitNotExpr(SimplerRaspParser.NotExprContext ctx) { 
		CompileNode leftTerm = this.visit(ctx.expression());
		
		if (leftTerm.getType() != NodeType.BOOL) {
			throw new TypeMismatchException(
					"Cannot use ! operations on term " + leftTerm.getType().toString().toLowerCase(),
					ctx.start.getLine());
		}
		CompileNode parkLeftTerm = new CompileNode(NodeType.BOOL);
		this.memory.addNewTempNode(parkLeftTerm);
		
		this.appendNewInstruction(STORE, parkLeftTerm.getUniqueNodeId());
		
		/*
		 * To not expression you can consider the truth table
		 * true 1 --> 0
		 * false 0 --> 1
		 * 
		 * We are sure that the value is a boolean so we can load 0
		 */
		
		String continueIdentifier = this.memory.getNewLabelIdentifier();
		String loadZeroIdentifier = this.memory.getNewLabelIdentifier();
		
		//if is greater than zero so is true load zero
		this.appendNewInstruction(JGZ, loadZeroIdentifier);
		
		this.appendNewInstruction(LOAD_DIRECT, "1");
		this.appendNewInstruction(JUMP, continueIdentifier);
		
		this.appendNewLabel(loadZeroIdentifier);
		this.appendNewInstruction(LOAD_DIRECT, "0");
		
		
		this.appendNewLabel(continueIdentifier);
		
		// push a jump operation to have no problem of compilation (label without instruction)
		// this jump instruction will never jump because the value of accumulator is 0 or 1
		this.appendNewInstruction(JLZ, ERROR_LABEL);
		
		return new CompileNode(NodeType.BOOL);
	}

	
	@Override
	public CompileNode visitAndExpr(SimplerRaspParser.AndExprContext ctx) {
		CompileNode leftTerm = this.visit(ctx.expression(0));

		if (leftTerm.getType() != NodeType.BOOL) {
			throw new TypeMismatchException(
					"Cannot use && operations on term " + leftTerm.getType().toString().toLowerCase(),
					ctx.start.getLine());
		}
		// create temp node where you can store the result of the left expression
		CompileNode parkLeftTerm = new CompileNode(NodeType.BOOL);
		String randomId = UniqueIdentifierManager.getInstance().getNewUniqueId();
		this.storeAccValueInNewNodeInStack(randomId, parkLeftTerm);
		

		CompileNode rightTerm = this.visit(ctx.expression(1));
		if (rightTerm.getType() != NodeType.BOOL) {
			throw new TypeMismatchException(
					"Cannot use && operator on term " + rightTerm.getType().toString().toLowerCase(),
					ctx.start.getLine());
		}

		CompileNode parkRightTerm = new CompileNode(NodeType.BOOL);
		this.memory.addNewTempNode(parkRightTerm);

		//IS NECESSARY TO STORE THE VALUE IN ACCUMULATOR FOR RECURSION COMPATIBILITY
		//STORE RIGHT TERM AND LOAD left term
		this.appendNewInstruction(STORE, parkRightTerm.getUniqueNodeId());
		this.loadInAccExistantNodeInStack(randomId, parkLeftTerm);
		
		/*
		 * To calculate and between two bool is used the following logic: A B 0 0 > and
		 * false sum = 0 0 1 > and false sum = 1 1 0 > and false sum = 1 1 1 > and true
		 * sum = 2
		 * 
		 * So to check if and return 1 you can subtract 2 and check if result is Zero.
		 * If it is so the result is true (1) otherwise zero
		 */
		
		
		this.appendNewInstruction(ADD, parkRightTerm.getUniqueNodeId());

		String continueIdentifier = this.memory.getNewLabelIdentifier();
		String loadOneIdentifier = this.memory.getNewLabelIdentifier();

		this.appendNewInstruction(SUB_DIRECT, "2");
		this.appendNewInstruction(JZ, loadOneIdentifier);

		// define load zero if not jump zero will be loaded
		this.appendNewInstruction(LOAD_DIRECT, "0");
		this.appendNewInstruction(JUMP, continueIdentifier);

		// define load one action
		this.appendNewLabel(loadOneIdentifier);
		this.appendNewInstruction(LOAD_DIRECT, "1");

		this.appendNewLabel(continueIdentifier);
		
		this.appendNewInstruction(STORE, parkRightTerm.getUniqueNodeId());
		
		this.freeStackCells(1);
		this.memory.decreseNumberOfVariableDeclared();

		this.appendNewInstruction(LOAD, parkRightTerm.getUniqueNodeId());

		return new CompileNode(NodeType.BOOL);
	}
	
	@Override
	public CompileNode visitOrExpr(SimplerRaspParser.OrExprContext ctx) {
		CompileNode leftTerm = this.visit(ctx.expression(0));

		if (leftTerm.getType() != NodeType.BOOL) {
			throw new TypeMismatchException(
					"Cannot use || operator on term " + leftTerm.getType().toString().toLowerCase(),
					ctx.start.getLine());
		}
		// create temp node where you can store the result of the left expression
		CompileNode parkLeftTerm = new CompileNode(NodeType.BOOL);
		String randomId = UniqueIdentifierManager.getInstance().getNewUniqueId();
		this.storeAccValueInNewNodeInStack(randomId, parkLeftTerm);

		
		CompileNode rightTerm = this.visit(ctx.expression(1));
		if (rightTerm.getType() != NodeType.BOOL) {
			throw new TypeMismatchException(
					"Cannot use && operations on term " + rightTerm.getType().toString().toLowerCase(),
					ctx.start.getLine());
		}

		CompileNode parkRightTerm = new CompileNode(NodeType.BOOL);
		this.memory.addNewTempNode(parkRightTerm);

		//STORE RIGHT TERM AND LOAD left term
		this.appendNewInstruction(STORE, parkRightTerm.getUniqueNodeId());
		this.loadInAccExistantNodeInStack(randomId, parkLeftTerm);
		
		/*
		 * To calculate and between two bool is used the following logic: A B 0 0 > and
		 * false sum = 0 0 1 > and true sum = 1 1 0 > and true sum = 1 1 1 > and false
		 * sum = 2
		 * 
		 * So to check if and return 1 you can sum two operands and check if is 
		 * equal to zero. If it is so the result is true (1) otherwise zero
		 */
		
		this.appendNewInstruction(ADD, parkRightTerm.getUniqueNodeId());
		

		String continueIdentifier = this.memory.getNewLabelIdentifier();
		String loadOneIdentifier = this.memory.getNewLabelIdentifier();

		this.appendNewInstruction(JGZ, loadOneIdentifier);

		// define load zero if not jump zero will be loaded
		this.appendNewInstruction(LOAD_DIRECT, "0");
		this.appendNewInstruction(JUMP, continueIdentifier);

		// define load one action
		this.appendNewLabel(loadOneIdentifier);
		this.appendNewInstruction(LOAD_DIRECT, "1");

		this.appendNewLabel(continueIdentifier);
		
		this.appendNewInstruction(STORE, parkRightTerm.getUniqueNodeId());
		
		this.freeStackCells(1);
		this.memory.decreseNumberOfVariableDeclared();

		this.appendNewInstruction(LOAD, parkRightTerm.getUniqueNodeId());

		return new CompileNode(NodeType.BOOL);
	}
	
	@Override
	public CompileNode visitCompExpr(SimplerRaspParser.CompExprContext ctx) {
		CompileNode leftTerm = this.visit(ctx.expression(0));
						
		boolean isSupportedOperationForBool = false;
		if(ctx.NOT_EQUAL()!=null || ctx.EQUAL() != null) {
			isSupportedOperationForBool = true;
		}
		
		boolean isNumberValidComparation = leftTerm.getType() == NodeType.NUMBER;
		boolean isBoolValidComparation = leftTerm.getType() == NodeType.BOOL && isSupportedOperationForBool;
				
		if (!isNumberValidComparation && !isBoolValidComparation) {
			throw new TypeMismatchException(
					"Cannot use compare operations on term " + leftTerm.getType().toString().toLowerCase(),
					ctx.start.getLine());
		}
		
		// create temp node where you can store the result of the left expression
		CompileNode parkLeftTerm = new CompileNode(NodeType.BOOL);
		String randomId = UniqueIdentifierManager.getInstance().getNewUniqueId();
		this.storeAccValueInNewNodeInStack(randomId, parkLeftTerm);
		

		CompileNode rightTerm = this.visit(ctx.expression(1));
		if (rightTerm.getType() != leftTerm.getType()) {
			throw new TypeMismatchException(
					"Cannot use compare operations between " + rightTerm.getType().toString().toLowerCase()
					+" and "+leftTerm.getType().toString().toLowerCase(), ctx.start.getLine());
		}
		// create temp node where you can store the result of the right expression
		CompileNode parkRightTerm = new CompileNode(NodeType.BOOL);
		this.memory.addNewTempNode(parkRightTerm);

		//STORE RIGHT TERM AND LOAD left term
		this.appendNewInstruction(STORE, parkRightTerm.getUniqueNodeId());
		
		

		String continueIdentifier = this.memory.getNewLabelIdentifier();
		String loadOneIdentifier = this.memory.getNewLabelIdentifier();

		/*
		 * The logic behind the implementation a {sign} b Example: a<b is equivalent to
		 * --> a-b{sign}0 --> a-b<0
		 */

		this.loadInAccExistantNodeInStack(randomId, parkLeftTerm);
		
		this.appendNewInstruction(SUB, parkRightTerm.getUniqueNodeId());

		if (ctx.GT() != null) {
			this.appendNewInstruction(JGZ, loadOneIdentifier);
		} else if (ctx.LT() != null) {
			this.appendNewInstruction(JLZ, loadOneIdentifier);
		} else if (ctx.LE() != null) {
			this.appendNewInstruction(JLEZ, loadOneIdentifier);
		} else if (ctx.GE() != null) {
			this.appendNewInstruction(JGEZ, loadOneIdentifier);
		} else if (ctx.EQUAL() != null) {
			this.appendNewInstruction(JZ, loadOneIdentifier);
		} else if (ctx.NOT_EQUAL() != null) {
			this.appendNewInstruction(JNZ, loadOneIdentifier);
		}

		//this temp term is used to leave the continue with a valid instruction and not empty
		CompileNode tempResult = new CompileNode(NodeType.NUMBER);
		this.memory.addNewTempNode(tempResult);

		this.appendNewInstruction(STORE, parkRightTerm.getUniqueNodeId());
		
		// define load zero if not jump zero will be loaded
		this.appendNewInstruction(LOAD_DIRECT, "0");
		this.appendNewInstruction(STORE, tempResult.getUniqueNodeId());
		this.appendNewInstruction(JUMP, continueIdentifier);

		// define load one action
		this.appendNewLabel(loadOneIdentifier);
		this.appendNewInstruction(LOAD_DIRECT, "1");
		this.appendNewInstruction(STORE, tempResult.getUniqueNodeId());
		
		this.appendNewLabel(continueIdentifier);
		
		this.appendNewInstruction(STORE, parkRightTerm.getUniqueNodeId());
		
		this.freeStackCells(1);
		this.memory.decreseNumberOfVariableDeclared();

		this.appendNewInstruction(LOAD, tempResult.getUniqueNodeId());
		

		return new CompileNode(NodeType.BOOL);
	}

	@Override
	public CompileNode visitParensExpr(SimplerRaspParser.ParensExprContext ctx) {
		CompileNode node = this.visit(ctx.expression());

		return node;
	}
	
	@Override
	public CompileNode visitIfStatement(SimplerRaspParser.IfStatementContext ctx) {
		// visit if expression. The result is in accumulator
		CompileNode node = this.visit(ctx.expression());
		
		if(node.getType()!=NodeType.BOOL) {
			throw new TypeMismatchException(
					"If condition value must be a bool. Found --> " + node.getType().toString().toLowerCase(),
					ctx.start.getLine());
		}
		
		String continueIdentifier = this.memory.getNewLabelIdentifier();
		String ifBlockIdentifier = this.memory.getNewLabelIdentifier();
		String elseBlockIdentifier = null;
		
		boolean elseBlockPresent = ctx.block().size()>1;
				
		
		if(elseBlockPresent) {
			elseBlockIdentifier = this.memory.getNewLabelIdentifier();
			//is true if is one so >0 false otherwise
			this.appendNewInstruction(JLEZ, elseBlockIdentifier);
			this.appendNewInstruction(JUMP, ifBlockIdentifier);
		}else {
			//is true if is one so >0 false otherwise
			this.appendNewInstruction(JLEZ, continueIdentifier);
			this.appendNewInstruction(JUMP, ifBlockIdentifier);
		}
		
		this.appendNewLabel(ifBlockIdentifier);
		this.visitBlock(ctx.block(0));
		this.appendNewInstruction(JUMP, continueIdentifier);
		
		if(elseBlockPresent) {
			this.appendNewLabel(elseBlockIdentifier);
			this.visitBlock(ctx.block(1));
			this.appendNewInstruction(JUMP, continueIdentifier);
		}
				
		this.appendNewLabel(continueIdentifier);
		
		return new CompileNode(NodeType.IF_STATEMENT);
	}

	
	@Override 
	public CompileNode visitWhileStatement(SimplerRaspParser.WhileStatementContext ctx) { 
		//calculate the condition
		CompileNode node = this.visit(ctx.expression());
				
		//add the while identifier
		String whileIdentifier = this.memory.getNewLabelIdentifier();
		this.appendNewLabel(whileIdentifier);
		
		if(node.getType()!=NodeType.BOOL) {
			throw new TypeMismatchException(
					"If condition value must be a bool. Found --> " + node.getType().toString().toLowerCase(),
					ctx.start.getLine());
		}
		
		String endWhile = this.memory.getNewLabelIdentifier();
		
		//if is true is one so >0 false otherwise. If is <0 terminate the while otherwise continue
		this.appendNewInstruction(JLEZ, endWhile);
			
		this.visitBlock(ctx.block());
		//code to reavaluate the expression
		this.visit(ctx.expression());
		
		this.appendNewInstruction(JUMP, whileIdentifier);
		
		//add an instruction after while label because can cause compilation errors
		this.appendNewLabel(endWhile);
		//so we add the instruction that evaluate the condition that stop the while 
		//if we are at the end of the while we know that the value is JLEZ
		//so we add an JGZ that will be never execute the jump instruction
		this.appendNewInstruction(JGZ, whileIdentifier);
		
		return new CompileNode(NodeType.WHILE_STATEMENT);
	}
	
	
	@Override 
	public CompileNode visitReturnStatement(SimplerRaspParser.ReturnStatementContext ctx) { 
		NodeType resType = NodeType.VOID;
		
		if(ctx.expression()!=null) {
			CompileNode exprResultNode = this.visit(ctx.expression());
			resType = exprResultNode.getType();
		}
		
		
		NodeType type = this.memory.getCurrentBlockType();
		
		if(resType!=type) {
			throw new TypeMismatchException(
					"Cannot return type " + resType.toString().toLowerCase()+
							 ". The type should be "+type,
					ctx.start.getLine());
		}
		
		String blockLabel = this.memory.getCurrentBlockLabel();
		
		if(blockLabel==null) {
			throw new RuntimeException("Compiler Error at visitReturnStatement. blockLabel was null");
		}
		
		CompileNode tempValueAcc = new CompileNode(NodeType.NUMBER);
		this.memory.addNewTempNode(tempValueAcc);
		this.appendNewInstruction(STORE, tempValueAcc.getUniqueNodeId());
		
		int totalVariableDeclared = this.memory.getTotalVariableDefinedInFunction();
		this.appendNewInstruction(LOAD, STACK_POINTER);
		this.appendNewInstruction(SUB_DIRECT, ""+totalVariableDeclared);
		this.appendNewInstruction(STORE, STACK_POINTER);
		
		this.appendNewInstruction(LOAD, tempValueAcc.getUniqueNodeId());
		
		this.appendNewInstruction(JUMP, blockLabel);
		
		return new CompileNode(NodeType.RETURN_STATEMENT);
	}
	
	
	
	private CompileNode previsitFunctionDeclaration(SimplerRaspParser.FunctionDeclarationContext ctx) {
		String functionName = ctx.IDENTIFIER().getText();
		String returnType = ctx.returnType().getText().toUpperCase();
		NodeType returnNodeType = NodeType.valueOf(returnType);
		
		FunctionInfo checkFuncInfo = this.memory.getDeclaredFunction(functionName);
		if(checkFuncInfo!=null) {
			throw new FunctionAlreadyDefinedException("The Function '"+functionName+"' is defined multiple times!"
					, ctx.start.getLine());
		}
		
		this.memory.pushNewBlock();
		
		FunctionInfo functionInfo;
		
		FunctionDeclarationInputVariablesContext declarations = ctx.functionDeclarationInputVariables();
		
		String functionLabel = this.memory.getNewLabelIdentifier();
		int totalVariables = 0;
		
		Set<String> definedIdentifiers = new HashSet<>();
		
		if(declarations!=null && !declarations.funtionInputVariable().isEmpty()) {
			List<FuntionInputVariableContext> variables = declarations.funtionInputVariable();
			
			totalVariables = variables.size();
			NodeType[] inputTypes = new NodeType[variables.size()];
			int index = 0; //start from 1 because totalVariables are computed by lenght
			
			for (FuntionInputVariableContext funtionInputVariableContext: variables) {
				String paramType = funtionInputVariableContext.TYPE().getText().toUpperCase();
				NodeType paramNodetype = NodeType.valueOf(paramType);
				inputTypes[index] = paramNodetype;
				String paramId = funtionInputVariableContext.IDENTIFIER().getText();
				
				if (definedIdentifiers.contains(paramId)) {
					throw new AlreadyDefinedIdentifier("Already defined variable " + paramId, 
							ctx.start.getLine());
				}
				
				definedIdentifiers.add(paramId);
				
				index++;
			}
			
			functionInfo = new FunctionInfo(returnNodeType, inputTypes.length, inputTypes, functionLabel);
			
		}else {
			functionInfo = new FunctionInfo(returnNodeType, totalVariables, new NodeType[0], functionLabel);
		}
		
		this.memory.addNewDeclaredFunction(functionName, functionInfo);
		this.memory.popBlock();
		
		return new CompileNode(NodeType.FUNCTION_DECLARATION);
	}
	
	
	@Override 
	public CompileNode visitFunctionDeclaration(SimplerRaspParser.FunctionDeclarationContext ctx) {
		String functionName = ctx.IDENTIFIER().getText();
		String returnType = ctx.returnType().getText().toUpperCase();
		NodeType returnNodeType = NodeType.valueOf(returnType);
		
		FunctionInfo functionInfo = this.memory.getDeclaredFunction(functionName);
		
		if(functionInfo==null) {
			throw new RuntimeException("Compile exception at visitFunctionDeclaration!"
					+ "All function should be preprocecessed first");
		}
		
		this.memory.pushNewBlock();
		
		
		FunctionDeclarationInputVariablesContext declarations = ctx.functionDeclarationInputVariables();
		
		int totalVariables = functionInfo.getParameterNumber();
		
		//leave a free position for the return address
		this.memory.increaseNumberOfVariableDeclared();
		
		if(declarations!=null && !declarations.funtionInputVariable().isEmpty()) {
			List<FuntionInputVariableContext> variables = declarations.funtionInputVariable();
			
			totalVariables = variables.size();
			NodeType[] inputTypes = new NodeType[variables.size()];
			int index = 0;
			
			for (FuntionInputVariableContext funtionInputVariableContext: variables) {
				String paramType = funtionInputVariableContext.TYPE().getText().toUpperCase();
				NodeType paramNodetype = NodeType.valueOf(paramType);
				inputTypes[index] = paramNodetype;
				String paramId = funtionInputVariableContext.IDENTIFIER().getText();
								
				//this compile node is a compile node that must be get from stack
				CompileNode node = new CompileNode(paramNodetype);				
				this.memory.addNewStackNode(paramId, node);
								
				index++;
			}
		}
		
		String returnStatetmentLabel = this.memory.getNewLabelIdentifier();
		
		//set the label for the return
		this.memory.setReturnBlockLabel(returnStatetmentLabel);
		this.memory.setReturnBlockType(returnNodeType);
		
		//append the function label 
		this.appendNewLabel(functionInfo.getLabel());
				

		this.visitFunctionBlock(ctx.functionBlock());
		
		
		this.freeStackCells(totalVariables + 1);
		//move to the return point stack pointer
		
		//load zero if no return instruction is present
		this.appendNewInstruction(LOAD_DIRECT, "0");
		
		this.appendNewLabel(returnStatetmentLabel);
		
		//store result on temp variable
		CompileNode tempResult = new CompileNode(NodeType.NUMBER);
		this.memory.addNewTempNode(tempResult);
		this.appendNewInstruction(STORE, tempResult.getUniqueNodeId());
		
		
		//move the stack pointer to the return address
		this.appendNewInstruction(LOAD, STACK_POINTER);
		
		//store the return address in a temp variable
		CompileNode returnAddress = new CompileNode(NodeType.NUMBER);
		this.memory.addNewTempNode(returnAddress);
		this.appendNewInstruction(STORE, returnAddress.getUniqueNodeId());
		
		//load the result in the accumulator
		this.appendNewInstruction(LOAD, tempResult.getUniqueNodeId());
		this.appendNewInstruction(JUMP_BY_REFERENCE, returnAddress.getUniqueNodeId());
				
		this.memory.popBlock();
		
		return new CompileNode(NodeType.FUNCTION_DECLARATION);
	}
	
	
	@Override 
	public CompileNode visitFunctionCall(SimplerRaspParser.FunctionCallContext ctx) { 
		String functionName = ctx.IDENTIFIER().getText();
				
		FunctionInfo functionInfo = this.memory.getDeclaredFunction(functionName);
		if(functionInfo==null) {
			throw new FunctionNotDefinedException("The Function '"+functionName+"' is not defined!"
					, ctx.start.getLine());
		}
		
		String returnAddressId = this.memory.getNewLabelIdentifier();
		int totalVariables = 0;

		FunctionCallInputVariablesContext callInputVars = ctx.functionCallInputVariables();

		if(callInputVars!=null && !callInputVars.funtionCallVariable().isEmpty()) {
			List<FuntionCallVariableContext> variables = callInputVars.funtionCallVariable();
			
			totalVariables = variables.size();
			
			if(totalVariables!=functionInfo.getParameterNumber()) {
				throw new ArgumentNumberMismatchException("Function accept "+functionInfo.getParameterNumber()
								+ " variable but "+totalVariables+" was found. ", ctx.start.getLine());
			}
			
			int index = 0;
			
			//PUSH THE RETURN ADDRESS IN THE STACK
			this.addReturnAddressInStack(returnAddressId);
			this.memory.increaseNumberOfVariableDeclared();
			// in questo punto viene incrementato il numero di variabili sullo stack per far si che 
			// il riferimento allo stack pointer sia corretto. Potrebbe succedere infatti che se una variabile
			// dovrà essere inserita sullo stack perchè è una variabile di input del metodo
			// questa variabile andrebbe ad assumere un valore errato perchè il riferimento allo stack 
			// pointer è stato alterato e quindi potrebbe non essere sulla posizione relativa salvata in precedenza
			
			for (FuntionCallVariableContext funtionInputVariableContext : variables) {
				CompileNode exprEvaluated = this.visitFuntionCallVariable(funtionInputVariableContext);
				
				
				NodeType functionToCallNodeType = functionInfo.getParametersTypes()[index];
				if(exprEvaluated.getType()!=functionToCallNodeType) {
					throw new TypeMismatchException("The argument num "+(index+1)
									+" in the function call is not valid type. received--> " 
									+ exprEvaluated.getType().toString().toLowerCase() 
								+ ", expected--> "+ functionToCallNodeType.toString().toLowerCase(), 
								ctx.start.getLine());
				}
								
				this.appendNewInstruction(STORE_BY_REFERENCE, STACK_POINTER);
				
				this.moveStackPointerAheadByOne();
				this.memory.increaseNumberOfVariableDeclared();		
				//ignore the variables added in stack now because are declared for the called function
				//the callec function consider that the variables are owned by him so he deallocate the
				// variables when is requires
				
				index++;
			}
			
			while(index>0) {
				this.memory.decreseNumberOfVariableDeclared();
				index--;
			}
			// si rimuove il conteggio dell'indirizzo di ritorno questo perchè
			// verrà rimosso automaticamente dalla funzione chiamata
			this.memory.decreseNumberOfVariableDeclared();
			
		}else {
			if(totalVariables!=functionInfo.getParameterNumber()) {
				throw new ArgumentNumberMismatchException("Function accept "+functionInfo.getParameterNumber()
								+ " variable but "+totalVariables+" was found", ctx.start.getLine());
			}
			
			this.addReturnAddressInStack(returnAddressId);
		}
		
		
		this.appendNewInstruction(JUMP, functionInfo.getLabel());
				
		this.appendNewLabel(returnAddressId);
		
		
		
		return new CompileNode(functionInfo.getReturnType()); 
	}

	@Override 
	public CompileNode visitFuntionCallVariable(SimplerRaspParser.FuntionCallVariableContext ctx) { 
		return this.visit(ctx.expression());
	}

	private void addReturnAddressInStack(String returnAddressId) {
		//save the return address
		this.appendNewInstruction(LOAD_DIRECT, returnAddressId);
		this.appendNewInstruction(STORE_BY_REFERENCE, STACK_POINTER);
		
		//check that the stack is not fully it it so jump to error
		this.moveStackPointerAheadByOne();
				
	}
	
	
}
