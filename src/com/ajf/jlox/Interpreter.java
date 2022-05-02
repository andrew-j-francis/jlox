package com.ajf.jlox;

import java.util.ArrayList;
import java.util.List;

public class Interpreter implements Expression.Visitor<Object>, Statement.Visitor<Void> {
	final Environment globals = new Environment();
	private Environment environment = globals;

	Interpreter () {
		globals.define("clock", new LoxCallable() {
			@Override
			public Object call (Interpreter interpreter, List<Object> arguments) {
				return (double) System.currentTimeMillis() / 1000.0;
			}

			@Override
			public int arity () {
				return 0;
			}

			@Override
			public String toString () {
				return "<native fn>";
			}
		});
	}


	@Override
	public Object visitBinaryExpression (Expression.BinaryExpression expression) {
		Object left = evaluateExpression(expression.left);
		Object right = evaluateExpression(expression.right);

		switch (expression.operator.type) {
			case MINUS:
				verifyOperand(expression.operator, right);
				return (Double) left - (Double) right;
			case SLASH:
				verifyOperands(expression.operator, left, right);
				return (Double) left / (Double) right;
			case PLUS:
				if (left instanceof Double && right instanceof Double) {
					return (Double) left + (Double) right;
				} else if (left instanceof String && right instanceof String) {
					return (String) left + (String) right;
				}

				throw new RuntimeError(expression.operator, "Operands must be two numbers or two strings");
			case STAR:
				verifyOperands(expression.operator, left, right);
				return (Double) left * (Double) right;
			case GREATER:
				verifyOperands(expression.operator, left, right);
				return (Double) left > (Double) right;
			case LESS:
				verifyOperands(expression.operator, left, right);
				return (Double) left < (Double) right;
			case GREATER_EQUAL:
				verifyOperands(expression.operator, left, right);
				return (Double) left >= (Double) right;
			case LESS_EQUAL:
				verifyOperands(expression.operator, left, right);
				return (Double) left <= (Double) right;
			case BANG_EQUAL:
				return ! isEqual(left, right);
			case EQUAL_EQUAL:
				return isEqual(left, right);
			default:
				return null;
		}
	}

	@Override
	public Object visitCallExpression (Expression.CallExpression expression) {
		Object callee = evaluateExpression(expression.callee);

		List<Object> arguments = new ArrayList<>();
		for (Expression arg : expression.arguments) {
			arguments.add(evaluateExpression(arg));
		}

		if (! (callee instanceof LoxCallable)) {
			throw new RuntimeError(expression.paren, "Can only call functions and classes");
		}

		LoxCallable function = (LoxCallable) callee;

		if (arguments.size() != function.arity()) {
			throw new RuntimeError(expression.paren,
								   "Expected " + function.arity() + " arguments. Received " + arguments.size() + " arguments.");
		}

		return function.call(this, arguments);
	}

	@Override
	public Object visitGroupingExpression (Expression.GroupingExpression expression) {
		return evaluateExpression(expression.expression);
	}

	@Override
	public Object visitLiteralExpression (Expression.LiteralExpression expression) {
		return expression.value;
	}

	@Override
	public Object visitLogicalExpression (Expression.LogicalExpression expression) {
		Object evaluatedLeftExpression = evaluateExpression(expression.left);

		if (expression.operator.type == TokenType.OR) {
			if (isTruthy(evaluatedLeftExpression)) {
				return evaluatedLeftExpression;
			}
		} else {
			if (! isTruthy(evaluatedLeftExpression)) {
				return evaluatedLeftExpression;
			}
		}

		return evaluateExpression(expression.right);
	}

	@Override
	public Object visitUnaryExpression (Expression.UnaryExpression expression) {
		Object evaluatedRightExpression = evaluateExpression(expression.right);

		switch (expression.operator.type) {
			case MINUS:
				return - (double) evaluatedRightExpression;
			case BANG:
				return ! isTruthy(evaluatedRightExpression);
		}
		throw new IllegalStateException("Unexpected value: " + expression.operator);

	}

	@Override
	public Object visitVariableExpression (Expression.VariableExpression expression) {
		return environment.getVariable(expression.variableName);
	}

	private boolean isEqual (Object left, Object right) {
		if (left == null && right == null) {
			return true;
		} else if (left == null) {
			return false;
		} else {
			return left.equals(right);
		}

	}

	private void verifyOperand (Token operator, Object operand) {
		if (operand instanceof Double) {
			return;
		}

		throw new RuntimeError(operator, "Operand must be a number");
	}

	private void verifyOperands (Token operator, Object rightOperand, Object leftOperand) {
		if (rightOperand instanceof Double && leftOperand instanceof Double) {
			return;
		}

		throw new RuntimeError(operator, "Operands must be a number");
	}

	private boolean isTruthy (Object objectToEvaluate) {
		if (objectToEvaluate == null) {
			return false;
		} else if (objectToEvaluate instanceof Boolean) {
			return (Boolean) objectToEvaluate;
		} else {
			return true;
		}
	}

	private Object evaluateExpression (Expression expressionToEvaluate) {
		return expressionToEvaluate.accept(this);
	}

	public void interpret (List<Statement> statements) {
		try {
			for (Statement statement : statements) {
				execute(statement);
			}
		} catch (RuntimeError error) {
			Lox.runtimeError(error);
		}
	}

	private void execute (Statement statement) {
		statement.accept(this);
	}

	private String stringify (Object value) {
		if (value == null) {
			return "nil";
		} else if (value instanceof Double) {
			String text = value.toString();

			if (text.endsWith(".0")) {
				text = text.substring(0, text.length() - 2);
			}

			return text;
		} else {
			return value.toString();
		}

	}

	@Override
	public Void visitBlockStatement (Statement.BlockStatement statement) {
		executeBlock(statement.statements, new Environment(environment));
		return null;
	}

	public void executeBlock (List<Statement> statements, Environment environment) {
		Environment previousEnvironment = this.environment;

		try {
			this.environment = environment;
			for (Statement statement : statements) {
				execute(statement);
			}

		} finally {
			this.environment = previousEnvironment;
		}
	}

	@Override
	public Void visitExpressionStatement (Statement.ExpressionStatement statement) {
		evaluateExpression(statement.expression);
		return null;
	}

	@Override
	public Void visitFunctionStatement (Statement.FunctionStatement statement) {
		LoxFunction function = new LoxFunction(statement);
		environment.define(statement.name.lexeme, function);
		return null;
	}

	@Override
	public Void visitIfStatement (Statement.IfStatement statement) {
		if (isTruthy(evaluateExpression(statement.condition))) {
			execute(statement.thenBranch);
		} else if (statement.elseBranch != null) {
			execute(statement.elseBranch);
		}
		return null;
	}

	@Override
	public Void visitPrintStatement (Statement.PrintStatement statement) {
		Object value = evaluateExpression(statement.expression);
		System.out.println(stringify(value));
		return null;
	}

	@Override
	public Void visitReturnStatement (Statement.ReturnStatement statement) {
		Object value = null;
		if (statement.value != null) {
			value = evaluateExpression(statement.value);
		}

		throw new Return(value);
	}

	@Override
	public Void visitWhileStatement (Statement.WhileStatement statement) {
		while (isTruthy(evaluateExpression(statement.condition))) {
			execute(statement.body);
		}
		return null;
	}

	@Override
	public Void visitVariableStatement (Statement.VariableStatement statement) {
		Object value = null;
		if (statement.initializer != null) {
			value = evaluateExpression(statement.initializer);
		}

		environment.define(statement.variableName.lexeme, value);
		return null;
	}

	@Override
	public Object visitAssignExpression (Expression.AssignExpression expression) {
		Object value = evaluateExpression(expression.value);
		environment.assign(expression.variableName, value);
		return value;
	}
}

