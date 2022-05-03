package com.ajf.jlox;

import javax.swing.text.html.parser.AttributeList;
import java.util.HashMap;
import java.util.Map;

public class Environment {
	private final Map<String, Object> variables = new HashMap<>();
	private final Environment enclosingEnvironment;

	public Environment () {
		this.enclosingEnvironment = null;
	}

	public Environment (Environment enclosingEnvironment) {
		this.enclosingEnvironment = enclosingEnvironment;
	}


	public void define (String variableName, Object variableValue) {
		variables.put(variableName, variableValue);
	}

	public Object getVariable (Token variableToken) {
		if (variables.containsKey(variableToken.lexeme)) {
			return variables.get(variableToken.lexeme);
		} else if (enclosingEnvironment != null) {
			return enclosingEnvironment.getVariable(variableToken);
		} else {
			throw new RuntimeError(variableToken, "Undefined variable during get '" + variableToken.lexeme + "'.");
		}
	}

	public void assign (Token variableName, Object value) {
		if (variables.containsKey(variableName.lexeme)) {
			variables.put(variableName.lexeme, value);
			return;
		} else if (enclosingEnvironment != null) {
			enclosingEnvironment.assign(variableName, value);
			return;
		} else {
			throw new RuntimeError(variableName, "Undefined variable during assign '" + variableName.lexeme + "'.");
		}

	}

	public Object getAt (Integer distance, String variableName) {
		return findEnvironmentWithVariable(distance).variables.get(variableName);
	}

	private Environment findEnvironmentWithVariable (Integer distance) {
		Environment environment = this;

		for (int i = 0; i < distance; i++) {
			environment = environment.enclosingEnvironment;
		}

		return environment;
	}

	public void assignAt (Integer distance, Token variableToken, Object value) {
		Environment environment = findEnvironmentWithVariable(distance);

		environment.variables.put(variableToken.lexeme, value);
	}
}
