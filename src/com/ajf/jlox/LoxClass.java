package com.ajf.jlox;

import java.util.List;
import java.util.Map;

public class LoxClass implements LoxCallable {
	final String className;
	private final Map<String, LoxFunction> methods;

	public LoxClass (String className) {
		this.className = className;
		this.methods = null;
	}

	public LoxClass (String className, Map<String, LoxFunction> methods) {
		this.className = className;
		this.methods = methods;
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

	public LoxFunction findMethod (String methodName) {
		if (methods.containsKey(methodName)) {
			return methods.get(methodName);
		} else {
			return null;
		}
	}
}
