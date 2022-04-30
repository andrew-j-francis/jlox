package com.ajf.jlox;

import java.util.List;

abstract class Expression {
interface Visitor<R> {
 R visitAssignExpression(AssignExpression expression);
 R visitBinaryExpression(BinaryExpression expression);
 R visitCallExpression(CallExpression expression);
 R visitGroupingExpression(GroupingExpression expression);
 R visitLiteralExpression(LiteralExpression expression);
 R visitLogicalExpression(LogicalExpression expression);
 R visitUnaryExpression(UnaryExpression expression);
 R visitVariableExpression(VariableExpression expression);
}
static class AssignExpression extends Expression{
final  Token variableName;
final  Expression value;
AssignExpression( Token variableName, Expression value) {
this.variableName = variableName;
this.value = value;
}
@Override
<R>R accept(Visitor<R> visitor){
return visitor.visitAssignExpression(this);
}
}
static class BinaryExpression extends Expression{
final  Expression left;
final  Token operator;
final  Expression right;
BinaryExpression( Expression left, Token operator, Expression right) {
this.left = left;
this.operator = operator;
this.right = right;
}
@Override
<R>R accept(Visitor<R> visitor){
return visitor.visitBinaryExpression(this);
}
}
static class CallExpression extends Expression{
final  Expression callee;
final  Token paren;
final  List<Expression> arguments;
CallExpression( Expression callee, Token paren, List<Expression> arguments) {
this.callee = callee;
this.paren = paren;
this.arguments = arguments;
}
@Override
<R>R accept(Visitor<R> visitor){
return visitor.visitCallExpression(this);
}
}
static class GroupingExpression extends Expression{
final  Expression expression;
GroupingExpression( Expression expression) {
this.expression = expression;
}
@Override
<R>R accept(Visitor<R> visitor){
return visitor.visitGroupingExpression(this);
}
}
static class LiteralExpression extends Expression{
final  Object value;
LiteralExpression( Object value) {
this.value = value;
}
@Override
<R>R accept(Visitor<R> visitor){
return visitor.visitLiteralExpression(this);
}
}
static class LogicalExpression extends Expression{
final  Expression left;
final  Token operator;
final  Expression right;
LogicalExpression( Expression left, Token operator, Expression right) {
this.left = left;
this.operator = operator;
this.right = right;
}
@Override
<R>R accept(Visitor<R> visitor){
return visitor.visitLogicalExpression(this);
}
}
static class UnaryExpression extends Expression{
final  Token operator;
final  Expression right;
UnaryExpression( Token operator, Expression right) {
this.operator = operator;
this.right = right;
}
@Override
<R>R accept(Visitor<R> visitor){
return visitor.visitUnaryExpression(this);
}
}
static class VariableExpression extends Expression{
final  Token variableName;
VariableExpression( Token variableName) {
this.variableName = variableName;
}
@Override
<R>R accept(Visitor<R> visitor){
return visitor.visitVariableExpression(this);
}
}

abstract <R> R accept(Visitor<R> visitor);
}
