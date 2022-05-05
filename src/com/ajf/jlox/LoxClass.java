package com.ajf.jlox;

public class LoxClass {
	final String className;

	public LoxClass (String className) {
		this.className = className;
	}

	@Override
	public String toString () {
		return className;
	}
}
