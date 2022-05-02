package com.ajf.jlox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.ajf.jlox.TokenType.*;

public class Parser {
	private final List<Token> tokens;
	private int nextToBeConsumedTokenIndex = 0;

	Parser (List<Token> tokens) {
		this.tokens = tokens;
	}

	public List<Statement> parse () {
		List<Statement> statements = new ArrayList<>();

		while (isNotAtEndOfFile()) {
			statements.add(declaration());
		}

		return statements;
	}

	private Statement declaration () {
		try {
			if (matchTokenTypeWithNextToBeConsumedToken(VAR)) {
				return variableDeclaration();
			}

			return statement();
		} catch (ParseError error) {
			synchronize();
			return null;
		}

	}

	private Statement variableDeclaration () {
		Token variableName = checkForToken(IDENTIFIER, "Expect variable name.");

		Expression initializer = null;

		if (matchTokenTypeWithNextToBeConsumedToken(EQUAL)) {
			initializer = expression();
		}

		checkForToken(SEMICOLON, "Expect ';' after variable declaration");
		return new Statement.VariableStatement(variableName, initializer);
	}

	private Statement statement () {
		if (matchTokenTypeWithNextToBeConsumedToken(IF)) {
			return ifStatement();
		} else if (matchTokenTypeWithNextToBeConsumedToken(WHILE)) {
			return whileStatement();
		} else if (matchTokenTypeWithNextToBeConsumedToken(FOR)) {
			return forStatement();
		} else if (matchTokenTypeWithNextToBeConsumedToken(PRINT)) {
			return printStatement();
		} else if (matchTokenTypeWithNextToBeConsumedToken(LEFT_BRACE)) {
			return new Statement.BlockStatement(block());
		} else if (matchTokenTypeWithNextToBeConsumedToken(FUN)) {
			return function("function");
		} else if (matchTokenTypeWithNextToBeConsumedToken(RETURN)) {
			return returnStatement();
		}

		return expressionStatement();
	}

	private Statement returnStatement () {
		Token keyword = getMostRecentlyConsumedToken();

		Expression value = null;

		if (getNextToBeConsumedToken().type != SEMICOLON) {
			value = expression();
		}

		checkForToken(SEMICOLON, "Expect ';' after return value.");
		return new Statement.ReturnStatement(keyword, value);
	}

	private Statement function (String kind) {
		Token name = checkForToken(IDENTIFIER, "Expect " + kind + " name.");

		checkForToken(LEFT_PAREN, "Expect '(' after " + kind + " name.");

		List<Token> parameters = new ArrayList<>();

		if (getNextToBeConsumedToken().type != RIGHT_PAREN) {
			do {
				if (parameters.size() >= 255) {
					error(getNextToBeConsumedToken(), "Can't have more than 255 parameters");
				}
				parameters.add(checkForToken(IDENTIFIER, "Expect parameter name."));
			} while (matchTokenTypeWithNextToBeConsumedToken(COMMA));

		}

		checkForToken(RIGHT_PAREN, "Expect ')' after parameters.");
		checkForToken(LEFT_BRACE, "Expect '{' before " + kind + " body.");
		List<Statement> body = block();
		return new Statement.FunctionStatement(name, parameters, body);
	}

	private Statement forStatement () {
		checkForToken(LEFT_PAREN, "Expect '(' after 'for'.");

		Statement initializer;
		if (matchTokenTypeWithNextToBeConsumedToken(SEMICOLON)) {
			initializer = null;
		} else if (matchTokenTypeWithNextToBeConsumedToken(VAR)) {
			initializer = variableDeclaration();
		} else {
			initializer = expressionStatement();
		}

		Expression condition = null;

		if (! matchTokenTypeWithNextToBeConsumedToken(SEMICOLON)) {
			condition = expression();
		}

		checkForToken(SEMICOLON, "Expect ';' after loop condition.");

		Expression increment = null;
		if (! matchTokenTypeWithNextToBeConsumedToken(RIGHT_PAREN)) {
			increment = expression();
		}

		checkForToken(RIGHT_PAREN, "Expect ')' after for clauses.");

		Statement body = statement();

		if (increment != null) {
			body = new Statement.BlockStatement(Arrays.asList(body, new Statement.ExpressionStatement(increment)));
		}

		if (condition == null) {
			condition = new Expression.LiteralExpression(true);
		}

		body = new Statement.WhileStatement(condition, body);

		if (initializer != null) {
			body = new Statement.BlockStatement(Arrays.asList(initializer, body));
		}

		return body;
	}

	private Statement whileStatement () {
		checkForToken(LEFT_PAREN, "Expect '(' after 'while'.");
		Expression condition = expression();
		checkForToken(RIGHT_PAREN, "Expect ')' after 'while'.");
		Statement body = statement();
		return new Statement.WhileStatement(condition, body);
	}

	private Statement ifStatement () {
		checkForToken(LEFT_PAREN, "Expect '(' after 'if'");
		Expression condition = expression();
		checkForToken(RIGHT_PAREN, "Expect ')' after 'if'");

		Statement thenBranch = statement();
		Statement elseBranch = null;

		if (matchTokenTypeWithNextToBeConsumedToken(ELSE)) {
			elseBranch = statement();
		}

		return new Statement.IfStatement(condition, thenBranch, elseBranch);
	}


	private Statement expressionStatement () {
		Expression expression = expression();
		checkForToken(SEMICOLON, "Expect ';' after value.");

		return new Statement.ExpressionStatement(expression);
	}

	private Statement printStatement () {
		Expression value = expression();
		checkForToken(SEMICOLON, "Expect ';' after value.");
		return new Statement.PrintStatement(value);
	}

	private List<Statement> block () {
		List<Statement> statements = new ArrayList<>();

		while ((getNextToBeConsumedToken().type != RIGHT_BRACE) && isNotAtEndOfFile()) {
			statements.add(declaration());
		}

		checkForToken(RIGHT_BRACE, "Expect '}' after block.");
		return statements;
	}

	private Expression expression () {
		return assignment();
	}

	private Expression assignment () {
		Expression expression = or();

		if (matchTokenTypeWithNextToBeConsumedToken(EQUAL)) {
			Token equals = getMostRecentlyConsumedToken();
			Expression value = assignment();

			if (expression instanceof Expression.VariableExpression) {
				Token variableName = ((Expression.VariableExpression) expression).variableName;
				return new Expression.AssignExpression(variableName, value);
			}

			error(equals, "Invalid assignment target.");
		}

		return expression;
	}

	private Expression or () {
		Expression expression = and();

		while (matchTokenTypeWithNextToBeConsumedToken(OR)) {
			Token operator = getMostRecentlyConsumedToken();
			Expression right = and();
			expression = new Expression.LogicalExpression(expression, operator, right);
		}

		return expression;
	}

	private Expression and () {
		Expression expression = equality();

		while (matchTokenTypeWithNextToBeConsumedToken(AND)) {
			Token operator = getMostRecentlyConsumedToken();
			Expression right = equality();
			expression = new Expression.LogicalExpression(expression, operator, right);
		}

		return expression;
	}

	private Expression equality () {
		Expression expression = comparison();

		while (matchTokenTypeWithNextToBeConsumedToken(BANG_EQUAL, EQUAL_EQUAL)) {
			Token operator = getMostRecentlyConsumedToken();
			Expression rightExpression = comparison();
			expression = new Expression.BinaryExpression(expression, operator, rightExpression);
		}

		return expression;
	}

	private Expression comparison () {
		Expression expression = term();

		while (matchTokenTypeWithNextToBeConsumedToken(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
			Token operator = getMostRecentlyConsumedToken();
			Expression rightExpression = term();
			expression = new Expression.BinaryExpression(expression, operator, rightExpression);
		}

		return expression;
	}

	private Expression term () {
		Expression expression = factor();

		while (matchTokenTypeWithNextToBeConsumedToken(MINUS, PLUS)) {
			Token operator = getMostRecentlyConsumedToken();
			Expression rightExpression = factor();
			expression = new Expression.BinaryExpression(expression, operator, rightExpression);
		}

		return expression;
	}

	private Expression factor () {
		Expression expression = unary();

		while (matchTokenTypeWithNextToBeConsumedToken(STAR, SLASH)) {
			Token operator = getMostRecentlyConsumedToken();
			Expression rightExpression = unary();
			expression = new Expression.BinaryExpression(expression, operator, rightExpression);
		}

		return expression;
	}

	private Expression unary () {
		if (matchTokenTypeWithNextToBeConsumedToken(BANG, MINUS)) {
			Token operator = getMostRecentlyConsumedToken();
			Expression rightExpression = unary();
			return new Expression.UnaryExpression(operator, rightExpression);
		}

		return call();
	}

	private Expression call () {
		Expression expression = primary();

		while (true) {
			if (matchTokenTypeWithNextToBeConsumedToken(LEFT_PAREN)) {
				expression = finishCall(expression);
			} else {
				break;
			}
		}

		return expression;
	}

	private Expression finishCall (Expression callee) {
		List<Expression> arguments = new ArrayList<>();

		if (! (getNextToBeConsumedToken().type == RIGHT_PAREN) && isNotAtEndOfFile()) {
			do {
				if (arguments.size() >= 255) {
					error(getNextToBeConsumedToken(), "Can't have more than 255 arguments.");
				}
				arguments.add(expression());
			} while (matchTokenTypeWithNextToBeConsumedToken(COMMA));
		}

		Token paren = checkForToken(RIGHT_PAREN, "Expect ')' after arguments.");

		return new Expression.CallExpression(callee, paren, arguments);
	}

	private Expression primary () {
		if (matchTokenTypeWithNextToBeConsumedToken(FALSE)) {
			return new Expression.LiteralExpression(false);
		}
		if (matchTokenTypeWithNextToBeConsumedToken(TRUE)) {
			return new Expression.LiteralExpression(true);
		}
		if (matchTokenTypeWithNextToBeConsumedToken(NIL)) {
			return new Expression.LiteralExpression(null);
		}

		if (matchTokenTypeWithNextToBeConsumedToken(NUMBER, STRING)) {
			return new Expression.LiteralExpression(getMostRecentlyConsumedToken().literal);
		}

		if (matchTokenTypeWithNextToBeConsumedToken(LEFT_PAREN)) {
			Expression expression = expression();
			checkForToken(RIGHT_PAREN, "Expect ')' after expression.");
			return new Expression.GroupingExpression(expression);
		}

		if (matchTokenTypeWithNextToBeConsumedToken(IDENTIFIER)) {
			return new Expression.VariableExpression(getMostRecentlyConsumedToken());
		}

		throw error(getNextToBeConsumedToken(), "Expect expression.");
	}

	private boolean matchTokenTypeWithNextToBeConsumedToken (TokenType... types) {
		for (TokenType tokenTypeToCheck : types) {

			Token nextToBeConsumedToken = getNextToBeConsumedToken();

			if (isNotAtEndOfFile() && nextToBeConsumedToken.type == tokenTypeToCheck) {
				consumeToken();
				return true;
			}

		}

		return false;
	}

	private Token getNextToBeConsumedToken () {
		return tokens.get(nextToBeConsumedTokenIndex);
	}

	private void consumeToken () {
		if (isNotAtEndOfFile()) {
			nextToBeConsumedTokenIndex++;
		}
	}

	private boolean isNotAtEndOfFile () {
		return getNextToBeConsumedToken().type != EOF;
	}

	private Token getMostRecentlyConsumedToken () {
		return tokens.get(nextToBeConsumedTokenIndex - 1);
	}

	private Token checkForToken (TokenType type, String message) {
		Token nextToBeConsumedToken = getNextToBeConsumedToken();

		if (isNotAtEndOfFile() && nextToBeConsumedToken.type == type) {
			consumeToken();
			return getMostRecentlyConsumedToken();
		}


		throw error(getNextToBeConsumedToken(), message);
	}

	private ParseError error (Token token, String message) {
		Lox.error(token, message);

		return new ParseError();
	}

	private void synchronize () {
		consumeToken();

		while (! isNotAtEndOfFile()) {
			if (getMostRecentlyConsumedToken().type == SEMICOLON) {
				return;
			}

			switch (getNextToBeConsumedToken().type) {
				case FOR:
				case FUN:
				case IF:
				case VAR:
				case CLASS:
				case WHILE:
				case RETURN:
				case PRINT:
					return;
			}
		}
	}

	private static class ParseError extends RuntimeException {

	}
}
