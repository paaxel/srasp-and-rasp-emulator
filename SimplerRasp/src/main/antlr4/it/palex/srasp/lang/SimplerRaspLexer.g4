lexer grammar SimplerRaspLexer;

MUL:'*';
DIV: '/';
REMAINDER: '%';
ADD: '+';
SUB: '-';

TYPE: 'bool' | 'number';
VOID: 'void';


ASSIGN:             '=';

GT:                 '>';
LT:                 '<';
LE:                 '<=';
GE:                 '>=';
EQUAL:              '==';
NOT_EQUAL:           '!=';

AND:                '&&';
OR:                 '||';
NOT:				'!';

COLON : ':';
SEMI_COLON : ';';
COMMA : ',';


OPEN_BRACE : '{';
CLOSE_BRACE : '}';
OPEN_PAREN : '(';
CLOSE_PAREN : ')';

ARROW_LEFT : '->';
ARROW_RIGHT: '<-';

TRUE: 'true';
FALSE: 'false';

CIN: 'cin';
COUT: 'cout';

RETURN: 'return';
DEF_FUNCTION: 'def';


WHILE: 'while';
IF: 'if';
ELSE: 'else';

MAIN: 'main';


fragment NON_ZERO_DIGIT: [1-9];
fragment ZERO_DIGIT: '0';
fragment DIGIT: (NON_ZERO_DIGIT | ZERO_DIGIT);
NUMBER : ZERO_DIGIT | (DIGIT)+;

fragment LETTER: [a-zA-Z]+;
fragment LETTER_OR_DIGIT: LETTER | [0-9];
IDENTIFIER: LETTER LETTER_OR_DIGIT*;


fragment COMMENT: '#' ~[\r\n]*;
fragment SPACES: [ \t]+;
fragment LINE_BREAK : [\r\n]+;

SKIP_ : ( SPACES | COMMENT | LINE_BREAK ) -> skip;