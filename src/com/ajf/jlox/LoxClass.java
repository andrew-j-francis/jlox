package com.ajf.jlox;

import java.util.List;

public class LoxClass implements LoxCallable {
	final String className;

	public LoxClass (String className) {
		this.className = className;
	}

	@Override
	public String toString () {
		return className;
	}

	@Override
	public Object call (Interpreter interpreter, List<Object> arguments) {
		LoxInstance instance = new LoxInstance(this);
		return instance;
	}

	@Override
	public int arity () {
		return 0;
	}
}
