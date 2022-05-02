package com.ajf.jlox;

import java.util.List;

public class LoxFunction implements LoxCallable {
	private final Statement.FunctionStatement declaration;
	private final Environment closure;

	public LoxFunction (Statement.FunctionStatement declaration, Environment closure) {
		this.closure = closure;
		this.declaration = declaration;
	}

	@Override
	public Object call (Interpreter interpreter, List<Object> arguments) {
		Environment environment = new Environment(closure);

		for (int i = 0; i < declaration.params.size(); i++) {
			environment.define(declaration.params.get(i).lexeme, arguments.get(i));
		}

		try {
			interpreter.executeBlock(declaration.body, environment);
		} catch (Return returnValue) {
			return returnValue.value;
		}
		return null;
	}

	@Override
	public int arity () {
		return declaration.params.size();
	}

	@Override
	public String toString () {
		return "<fn " + declaration.name.lexeme + ">";
	}
}
