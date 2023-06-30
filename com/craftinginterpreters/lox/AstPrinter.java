package com.craftinginterpreters.lox;

class AstPrinter implements Expr.Visitor<String> { // Implements the visitors
  String print(Expr expr) {
    return expr.accept(this);
  }

  @Override // Visit methods for each expression type we have so far. 
  public String visitBinaryExpr(Expr.Binary expr) {
    return parenthesize(expr.operator.lexeme, expr.left, expr.right);
  }
  @Override 
  public String visitGroupingExpr(Expr.Grouping expr) {
    return parenthesize("group", expr.expression);

  }
  @Override 
  public String visitLiteralExpr(Expr.Literal expr) {
    if (expr.value == null) return "nil";
    return expr.value.toString();
  }
  @Override 
  public String visitUnaryExpr(Expr.Unary expr) {
    return parenthesize(expr.operator.lexeme, expr.right);

  }
}
