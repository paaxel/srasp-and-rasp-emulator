
parser grammar SimplerRaspParser;

options { tokenVocab=SimplerRaspLexer; }


program: functionDeclaration* mainFunction functionDeclaration* EOF;

mainFunction: MAIN block;

returnType: TYPE | VOID;


functionDeclaration: DEF_FUNCTION IDENTIFIER OPEN_PAREN functionDeclarationInputVariables? 
									CLOSE_PAREN COLON returnType functionBlock;
 
functionDeclarationInputVariables: funtionInputVariable (COMMA funtionInputVariable)*;

funtionInputVariable: TYPE IDENTIFIER;

funtionCallVariable: expression;

functionCallInputVariables: funtionCallVariable (COMMA funtionCallVariable)*;

functionCall: IDENTIFIER OPEN_PAREN functionCallInputVariables? CLOSE_PAREN;


block: OPEN_BRACE blockStatement* CLOSE_BRACE;

functionBlock: OPEN_BRACE blockStatement* returnStatement CLOSE_BRACE;

blockStatement: statement | assignment | declaration | functionCallStatement;

declaration: TYPE IDENTIFIER (ASSIGN expression)? SEMI_COLON;
assignment: IDENTIFIER ASSIGN expression SEMI_COLON;
functionCallStatement: functionCall SEMI_COLON;

statement: (ifStatement | block | whileStatement | inOutStatement | returnStatement);

returnStatement: RETURN expression? SEMI_COLON;

ifStatement: IF OPEN_PAREN expression CLOSE_PAREN block (ELSE block)?;

whileStatement: WHILE OPEN_PAREN expression CLOSE_PAREN block;

signedNumber: (ADD | SUB)? NUMBER;
signedIdentifier: (ADD | SUB)? IDENTIFIER;

booleans: FALSE|TRUE;

inOutStatement: (inStatement | outStatement);
inStatement: CIN ARROW_LEFT IDENTIFIER SEMI_COLON;
outStatement: COUT ARROW_RIGHT (expression | signedNumber) SEMI_COLON;


expression:
 	 (NOT) expression																	#notExpr
 	| expression (MUL | DIV | REMAINDER) expression      								#mulDivRemExpr
 	| expression (ADD | SUB) expression                        							#addSubExpr
 	| expression (GT | LT | LE | GE | EQUAL | NOT_EQUAL) expression						#compExpr
	| expression AND expression															#andExpr
	| expression OR expression															#orExpr
 	| (signedNumber | booleans | signedIdentifier)  									#atomExpr
 	| (OPEN_PAREN expression CLOSE_PAREN)												#parensExpr
 	| (functionCall)																	#functionExpr
 	
 	;
 	
 	
 	
 	
 	
 	

