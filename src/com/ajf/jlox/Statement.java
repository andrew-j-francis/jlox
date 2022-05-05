package com.ajf.jlox;

import java.util.List;

abstract class Statement {
interface Visitor<R> {
 R visitBlockStatement(BlockStatement statement);
 R visitClassStatement(ClassStatement statement);
 R visitExpressionStatement(ExpressionStatement statement);
 R visitFunctionStatement(FunctionStatement statement);
 R visitIfStatement(IfStatement statement);
 R visitPrintStatement(PrintStatement statement);
 R visitReturnStatement(ReturnStatement statement);
 R visitWhileStatement(WhileStatement statement);
 R visitVariableStatement(VariableStatement statement);
}
static class BlockStatement extends Statement{
final  List<Statement> statements;
BlockStatement( List<Statement> statements) {
this.statements = statements;
}
@Override
<R>R accept(Visitor<R> visitor){
return visitor.visitBlockStatement(this);
}
}
static class ClassStatement extends Statement{
final  Token className;
final  List<Statement.FunctionStatement> methods;
ClassStatement( Token className, List<Statement.FunctionStatement> methods) {
this.className = className;
this.methods = methods;
}
@Override
<R>R accept(Visitor<R> visitor){
return visitor.visitClassStatement(this);
}
}
static class ExpressionStatement extends Statement{
final  Expression expression;
ExpressionStatement( Expression expression) {
this.expression = expression;
}
@Override
<R>R accept(Visitor<R> visitor){
return visitor.visitExpressionStatement(this);
}
}
static class FunctionStatement extends Statement{
final  Token name;
final  List<Token> params;
final List<Statement> body;
FunctionStatement( Token name, List<Token> params,List<Statement> body) {
this.name = name;
this.params = params;
this.body = body;
}
@Override
<R>R accept(Visitor<R> visitor){
return visitor.visitFunctionStatement(this);
}
}
static class IfStatement extends Statement{
final  Expression condition;
final  Statement thenBranch;
final  Statement elseBranch;
IfStatement( Expression condition, Statement thenBranch, Statement elseBranch) {
this.condition = condition;
this.thenBranch = thenBranch;
this.elseBranch = elseBranch;
}
@Override
<R>R accept(Visitor<R> visitor){
return visitor.visitIfStatement(this);
}
}
static class PrintStatement extends Statement{
final  Expression expression;
PrintStatement( Expression expression) {
this.expression = expression;
}
@Override
<R>R accept(Visitor<R> visitor){
return visitor.visitPrintStatement(this);
}
}
static class ReturnStatement extends Statement{
final  Token keyword;
final  Expression value;
ReturnStatement( Token keyword, Expression value) {
this.keyword = keyword;
this.value = value;
}
@Override
<R>R accept(Visitor<R> visitor){
return visitor.visitReturnStatement(this);
}
}
static class WhileStatement extends Statement{
final  Expression condition;
final  Statement body;
WhileStatement( Expression condition, Statement body) {
this.condition = condition;
this.body = body;
}
@Override
<R>R accept(Visitor<R> visitor){
return visitor.visitWhileStatement(this);
}
}
static class VariableStatement extends Statement{
final  Token variableName;
final  Expression initializer;
VariableStatement( Token variableName, Expression initializer) {
this.variableName = variableName;
this.initializer = initializer;
}
@Override
<R>R accept(Visitor<R> visitor){
return visitor.visitVariableStatement(this);
}
}

abstract <R> R accept(Visitor<R> visitor);
}
