package com.ajf.jlox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {
	static boolean hadError;
	static boolean hadRuntimeError;
	private static final Interpreter interpreter = new Interpreter();

	public static void main (String[] args) throws IOException {
		if (args.length > 1) {
			System.out.println("Usage: jlox [script]");
			System.exit(64);
		} else if (args.length == 1) {
			runFile(args[0]);
		} else {
			runPrompt();
		}
	}

	private static void runPrompt () throws IOException {
		InputStreamReader input = new InputStreamReader(System.in);
		BufferedReader reader = new BufferedReader(input);

		for (; ; ) {
			System.out.print("-> ");
			String line = reader.readLine();

			if (line == null) {
				break;
			}

			run(line);
			hadError = false;
			hadRuntimeError = false;
		}
	}

	private static void runFile (String path) throws IOException {
		byte[] bytes = Files.readAllBytes(Paths.get(path));
		run(new String(bytes, Charset.defaultCharset()));

		if (hadError) {
			System.exit(65);
		} else if (hadRuntimeError) {
			System.exit(70);
		}
	}

	private static void run (String source) {
		Scanner scanner = new Scanner(source);
		List<Token> tokens = scanner.scanTokens();

		Parser parser = new Parser(tokens);
		List<Statement> statements = parser.parse();

		if (hadError) {
			return;
		}

		Resolver resolver = new Resolver(interpreter);

		if (hadError) {
			return;
		}

		resolver.resolve(statements);

		interpreter.interpret(statements);

	}

	static void error (int line, String message) {
		report(line, "", message);
	}

	static void error (Token token, String message) {
		report(token.line, "at '" + token + "' ", message);
	}

	static void runtimeError (RuntimeError error) {
		System.err.println(error.getMessage() + "\n[line " + error.token.line + "]");
		hadRuntimeError = true;
	}

	private static void report (int line, String where, String message) {
		System.err.println("[line " + line + "] Error " + where + ": " + message);

		hadError = true;

	}


}
