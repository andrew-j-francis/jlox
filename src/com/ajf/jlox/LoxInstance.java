package com.ajf.jlox;

import java.util.HashMap;
import java.util.Map;

public class LoxInstance {
	private LoxClass newClass;
	private final Map<String, Object> fields = new HashMap<>();

	LoxInstance (LoxClass newClass) {
		this.newClass = newClass;
	}

	@Override
	public String toString () {
		return newClass.className + " instance";
	}

	public Object get (Token className) {
		if (fields.containsKey(className.lexeme)) {
			return fields.get(className.lexeme);
		} else {
			throw new RuntimeError(className, "Undefined property '" + className.lexeme + "'.");
		}
	}
}
