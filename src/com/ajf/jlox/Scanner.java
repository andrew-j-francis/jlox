package com.ajf.jlox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ajf.jlox.TokenType.*;

public class Scanner {
	private final String source;
	private final List<Token> tokens = new ArrayList<>();
	private int startOfTokenIndex = 0;
	private int cursorIndex = 0;
	private int line = 1;

	private static final Map<String, TokenType> keywords;

	static {
		keywords = new HashMap<>();
		keywords.put("and", AND);
		keywords.put("class", CLASS);
		keywords.put("else", ELSE);
		keywords.put("false", FALSE);
		keywords.put("for", FOR);
		keywords.put("fun", FUN);
		keywords.put("if", IF);
		keywords.put("nil", NIL);
		keywords.put("or", OR);
		keywords.put("print", PRINT);
		keywords.put("return", RETURN);
		keywords.put("super", SUPER);
		keywords.put("this", THIS);
		keywords.put("true", TRUE);
		keywords.put("var", VAR);
		keywords.put("while", WHILE);
	}

	public Scanner (String source) {
		this.source = source;
	}

	List<Token> scanTokens () {
		while (! isAtEnd()) {
			startOfTokenIndex = cursorIndex;
			scanToken();
		}

		tokens.add(new Token(EOF, "", null, line));

		return tokens;
	}

	//Check if token scanner consumed all source characters
	private boolean isAtEnd () {
		return cursorIndex >= source.length();
	}

	private void scanToken () {
		char currentChar = getCurrentChar();
		incrementCursorIndex();

		switch (currentChar) {
			case '(':
				addToken(LEFT_PAREN);
				break;
			case ')':
				addToken(RIGHT_PAREN);
				break;
			case '{':
				addToken(LEFT_BRACE);
				break;
			case '}':
				addToken(RIGHT_BRACE);
				break;
			case ',':
				addToken(COMMA);
				break;
			case '.':
				addToken(DOT);
				break;
			case '-':
				addToken(MINUS);
				break;
			case '+':
				addToken(PLUS);
				break;
			case ';':
				addToken(SEMICOLON);
				break;
			case '*':
				addToken(STAR);
				break;
			case '!':
				addToken(matchCurrentChar('=') ? BANG_EQUAL : BANG);
				break;
			case '=':
				addToken(matchCurrentChar('=') ? EQUAL_EQUAL : EQUAL);
				break;
			case '<':
				addToken(matchCurrentChar('=') ? LESS_EQUAL : LESS);
				break;
			case '>':
				addToken(matchCurrentChar('=') ? GREATER_EQUAL : GREATER);
				break;
			case '/':
				if (matchCurrentChar('/')) {
					while (peekAtCursorChar() != '\n' && ! isAtEnd()) {
						getCurrentChar();
						incrementCursorIndex();
					}
				} else {
					addToken(SLASH);
				}
				break;
			case ' ':
			case '\r':
			case '\t':
				break;
			case '\n':
				line++;
				break;
			case '"':
				String stringValue = getStringValue();
				addToken(STRING, stringValue);
				break;
			default:
				if (charIsDigit(currentChar)) {
					Double numberValue = getNumberValue();
					addToken(NUMBER, numberValue);
				} else if (charIsLetterOrUnderscore(currentChar)) {
					addIdentifierToken();
				} else {
					Lox.error(line,
							  "Unexpected character: " + currentChar + " | Start of broken token: " + startOfTokenIndex + " | Cursor Index " + cursorIndex);
				}
				break;
		}
	}

	private char getCurrentChar () {
		char currentChar = source.charAt(cursorIndex);

		return currentChar;
	}

	private void incrementCursorIndex () {
		cursorIndex++;
	}

	private void addToken (TokenType type) {
		addToken(type, null);
	}

	private void addToken (TokenType tokenType, Object literal) {
		String lexeme = source.substring(startOfTokenIndex, cursorIndex);
		tokens.add(new Token(tokenType, lexeme, literal, line));
	}

	private boolean matchCurrentChar (char expectedChar) {
		if (isAtEnd()) {
			return false;
		}

		if (source.charAt(cursorIndex) != expectedChar) {
			return false;
		}

		incrementCursorIndex();

		return true;

	}

	private char peekAtCursorChar () {
		if (isAtEnd()) {
			return '\0';
		}
		return source.charAt(cursorIndex);
	}

	private String getStringValue () {
		while (peekAtCursorChar() != '"' && ! isAtEnd()) {
			if (peekAtCursorChar() == '\n') {
				line++;
			}
			incrementCursorIndex();
		}

		if (isAtEnd()) {
			Lox.error(line, "Unterminated String");
			return null;
		}

		incrementCursorIndex();
		return source.substring(startOfTokenIndex + 1, cursorIndex - 1);
	}

	private boolean charIsDigit (char character) {
		return character >= '0' && character <= '9';
	}

	private Double getNumberValue () {
		while (charIsDigit(peekAtCursorChar())) {
			incrementCursorIndex();
		}

		if (peekAtCursorChar() == '.' && charIsDigit(peekAtNextCursorChar())) {
			incrementCursorIndex();

			while (charIsDigit(peekAtCursorChar())) {
				incrementCursorIndex();
			}
		}


		return Double.parseDouble(source.substring(startOfTokenIndex, cursorIndex));
	}

	private char peekAtNextCursorChar () {
		if (cursorIndex + 1 >= source.length()) {
			return '\0';
		}

		return source.charAt(cursorIndex + 1);
	}

	private boolean charIsLetterOrUnderscore (char character) {
		return (character >= 'a' && character <= 'z') || (character >= 'A' && character <= 'Z') || character == '_';
	}

	private void addIdentifierToken () {
		while (isAlphaNumeric(peekAtCursorChar())) {
			incrementCursorIndex();
		}

		String tokenText = source.substring(startOfTokenIndex, cursorIndex);
		TokenType tokenType = keywords.get(tokenText);
		if (tokenType == null) {
			tokenType = IDENTIFIER;
		}
		addToken(tokenType);
	}

	private boolean isAlphaNumeric (char character) {
		return charIsLetterOrUnderscore(character) || charIsDigit(character);
	}

}