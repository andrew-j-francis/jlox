package com.ajf.jlox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Resolver implements Expression.Visitor<Void>, Statement.Visitor<Void> {
	private final Interpreter interpreter;
	private final Stack<Map<String, Boolean>> scopes = new Stack<>();
	private FunctionType currentFunction = FunctionType.NONE;

	public Resolver (Interpreter interpreter) {
		this.interpreter = interpreter;
	}

	@Override
	public Void visitAssignExpression (Expression.AssignExpression expression) {
		resolve(expression.value);
		resolveLocal(expression, expression.variableName);
		return null;
	}

	@Override
	public Void visitBinaryExpression (Expression.BinaryExpression expression) {
		resolve(expression.left);
		resolve(expression.right);
		return null;
	}

	@Override
	public Void visitCallExpression (Expression.CallExpression expression) {
		resolve(expression.callee);

		for (Expression argument : expression.arguments) {
			resolve(argument);
		}

		return null;
	}

	@Override
	public Void visitGetExpression (Expression.GetExpression expression) {
		resolve(expression.object);
		return null;
	}

	@Override
	public Void visitSetExpression (Expression.SetExpression expression) {
		resolve(expression.value);
		resolve(expression.object);
		return null;
	}

	@Override
	public Void visitGroupingExpression (Expression.GroupingExpression expression) {
		resolve(expression.expression);
		return null;
	}

	@Override
	public Void visitLiteralExpression (Expression.LiteralExpression expression) {
		return null;
	}

	@Override
	public Void visitLogicalExpression (Expression.LogicalExpression expression) {
		resolve(expression.left);
		resolve(expression.right);
		return null;
	}

	@Override
	public Void visitUnaryExpression (Expression.UnaryExpression expression) {
		resolve(expression.right);
		return null;
	}

	@Override
	public Void visitVariableExpression (Expression.VariableExpression expression) {
		if (! scopes.isEmpty() && scopes.peek().get(expression.variableName.lexeme) == Boolean.FALSE) {
			Lox.error(expression.variableName, "Can't read local variable in its own initializer.");
		}

		resolveLocal(expression, expression.variableName);
		return null;
	}

	@Override
	public Void visitBlockStatement (Statement.BlockStatement statement) {
		beginScope();
		resolve(statement.statements);
		endScope();
		return null;
	}

	@Override
	public Void visitClassStatement (Statement.ClassStatement statement) {
		declare(statement.className);
		define(statement.className);
		return null;
	}

	@Override
	public Void visitExpressionStatement (Statement.ExpressionStatement statement) {
		resolve(statement.expression);
		return null;
	}

	@Override
	public Void visitFunctionStatement (Statement.FunctionStatement statement) {
		declare(statement.name);
		define(statement.name);
		resolveFunction(statement, FunctionType.FUNCTION);
		return null;
	}

	@Override
	public Void visitIfStatement (Statement.IfStatement statement) {
		resolve(statement.condition);
		resolve(statement.thenBranch);

		if (statement.elseBranch != null) {
			resolve(statement.elseBranch);
		}
		return null;
	}

	@Override
	public Void visitPrintStatement (Statement.PrintStatement statement) {
		resolve(statement.expression);
		return null;
	}

	@Override
	public Void visitReturnStatement (Statement.ReturnStatement statement) {
		if (currentFunction == FunctionType.NONE) {
			Lox.error(statement.keyword, "Can't return from top-level code.");
		}

		if (statement.value != null) {
			resolve(statement.value);
		}
		return null;
	}

	@Override
	public Void visitWhileStatement (Statement.WhileStatement statement) {
		resolve(statement.condition);
		resolve(statement.body);
		return null;
	}

	@Override
	public Void visitVariableStatement (Statement.VariableStatement statement) {
		declare(statement.variableName);
		if (statement.initializer != null) {
			resolve(statement.initializer);
		}
		define(statement.variableName);
		return null;
	}

	void resolve (List<Statement> statements) {
		for (Statement statement : statements) {
			resolve(statement);
		}
	}

	private void resolve (Statement statement) {
		statement.accept(this);
	}

	private void resolve (Expression expression) {
		expression.accept(this);
	}

	private void beginScope () {
		scopes.push(new HashMap<String, Boolean>());
	}

	private void endScope () {
		scopes.pop();
	}

	private void declare (Token name) {
		if (scopes.isEmpty()) {
			return;
		}

		Map<String, Boolean> scope = scopes.peek();
		if (scope.containsKey(name.lexeme)) {
			Lox.error(name, "Variable with this name already exists in this scope");
		}
		scope.put(name.lexeme, false);
	}

	private void define (Token name) {
		if (scopes.isEmpty()) {
			return;
		}

		scopes.peek().put(name.lexeme, true);
	}

	private void resolveLocal (Expression expression, Token variableToResolve) {
		for (int i = scopes.size() - 1; i >= 0; i--) {
			if (scopes.get(i).containsKey(variableToResolve.lexeme)) {
				interpreter.resolve(expression, scopes.size() - 1 - i);
				return;
			}
		}
	}

	private void resolveFunction (Statement.FunctionStatement function, FunctionType functionType) {
		FunctionType enclosingFunction = currentFunction;
		currentFunction = functionType;

		beginScope();
		for (Token param : function.params) {
			declare(param);
			define(param);
		}

		resolve(function.body);
		endScope();
		currentFunction = enclosingFunction;
	}
}
