package com.ajf.jlox;

public class ASTPrinter {
/*
	String printAST (Expression expression) {
		return expression.accept(this);
	}


	@Override
	public String visitAssignExpression (Expression.Assign expression) {
		return null;
	}

	@Override
	public String visitBinaryExpression (Expression.Binary expression) {
		return parenthesize(expression.operator.lexeme, expression.left, expression.right);
	}

	@Override
	public String visitGroupingExpression (Expression.Grouping expression) {
		return parenthesize("grp", expression.expression);
	}

	@Override
	public String visitLiteralExpression (Expression.Literal expression) {
		if (expression.value == null) {
			return "nil";
		}

		return expression.value.toString();
	}

	@Override
	public String visitUnaryExpression (Expression.Unary expression) {
		return parenthesize(expression.operator.lexeme, expression.right);
	}

	@Override
	public String visitVariableExpression (Expression.Variable expression) {
		return null;
	}

	private String parenthesize (String name, Expression... expressions) {
		StringBuilder stringBuilder = new StringBuilder();

		stringBuilder.append("(" + name);

		for (Expression exp : expressions) {
			stringBuilder.append(" " + exp.accept(this));
		}

		stringBuilder.append(")");

		return stringBuilder.toString();
	}
*/
}
