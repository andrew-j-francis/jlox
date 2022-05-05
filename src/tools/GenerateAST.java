package tools;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

public class GenerateAST {
	public static void main (String[] args) throws IOException {
		if (args.length != 1) {
			System.err.println("Usage: generate ast <output directory>");
			System.exit(64);
		}

		String outputDir = args[0];

		//Expressions
		defineAST(outputDir, "Expression", Arrays.asList(
				"AssignExpression : Token variableName, Expression value",
				"BinaryExpression : Expression left, Token operator, Expression right",
				"CallExpression : Expression callee, Token paren, List<Expression> arguments",
				"GetExpression : Expression object, Token className",
				"GroupingExpression : Expression expression",
				"LiteralExpression : Object value",
				"LogicalExpression : Expression left, Token operator, Expression right",
				"UnaryExpression : Token operator, Expression right",
				"VariableExpression : Token variableName"
		));

		//Statements
		defineAST(outputDir, "Statement", Arrays.asList(
				"BlockStatement: List<Statement> statements",
				"ClassStatement : Token className, List<Statement.FunctionStatement> methods",
				"ExpressionStatement : Expression expression",
				"FunctionStatement : Token name, List<Token> params,List<Statement> body",
				"IfStatement : Expression condition, Statement thenBranch, Statement elseBranch",
				"PrintStatement : Expression expression",
				"ReturnStatement: Token keyword, Expression value",
				"WhileStatement : Expression condition, Statement body",
				"VariableStatement : Token variableName, Expression initializer"
		));
	}

	private static void defineAST (String outputDir, String className,
								   List<String> types) throws IOException {
		String path = outputDir + "/" + className + ".java";

		PrintWriter writer = new PrintWriter(path, "UTF-8");
		writer.println("package com.ajf.jlox;");
		writer.println();
		writer.println("import java.util.List;");
		writer.println();

		writer.println("abstract class " + className + " {");

		defineVisitor(writer, className, types);

		for (String type : types) {
			String subClassName = type.split(":")[0].trim();
			String subClassFields = type.split(":")[1];
			createSubclass(writer, className, subClassName, subClassFields);
		}
		writer.println();
		writer.println("abstract <R> R accept(Visitor<R> visitor);");

		writer.println("}");

		writer.close();
	}

	private static void defineVisitor (PrintWriter writer, String className, List<String> types) {
		writer.println("interface Visitor<R> {");

		for (String type : types) {
			String typeName = type.split(":")[0].trim();

			writer.println(" R visit" + typeName + "(" + typeName + " " + className.toLowerCase() + ");");
		}

		writer.println("}");
	}

	private static void createSubclass (PrintWriter writer, String className, String subClassName,
										String subClassFields) {
		String[] fields = subClassFields.split(",");

		//class start
		writer.println("static class " + subClassName + " extends " + className + "{");

		//declare class vars
		for (String field : fields) {
			field.trim();
			writer.println("final " + field + ";");
		}

		//constructor start
		writer.println(subClassName + "(" + subClassFields + ") {");

		//Initialize Variables in Constructor
		for (String field : fields) {
			field = field.trim();
			String fieldName = field.split(" ")[1];
			writer.println("this." + fieldName + " = " + fieldName + ";");
		}
		writer.println("}");

		//create accept method
		writer.println("@Override");
		writer.println("<R>R accept(Visitor<R> visitor){");
		writer.println("return visitor.visit" + subClassName + "(this);");
		writer.println("}");

		writer.println("}");

	}
}
