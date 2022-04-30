package com.ajf.jlox;

class Token {
	final TokenType type;
	final String lexeme;
	final Object literal;
	final int line;

	Token (TokenType type, String lexeme, Object literal, int line) {
		this.type = type;
		this.lexeme = lexeme;
		this.literal = literal;
		this.line = line;
	}

	public String toString () {
		return "\n\t{\n\t\tToken Type: " + type + "\n\t\tLexeme: " + lexeme + "\n\t\tObject Literal:  " + literal + "\n\t}\n";
	}
}
