package com.github.sadikovi;

import java.util.List;

/**
 * AST printer.
 */
class AstPrinter implements Expr.Visitor<String>, Stmt.Visitor<String> {
  public String print(List<Stmt> statements) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < statements.size(); i++) {
      sb.append("[" + (i + 1) + "]: ");
      sb.append(statements.get(i).accept(this));
      sb.append("\n");
    }
    return sb.toString();
  }

  @Override
  public String visit(Stmt.Expression stmt) {
    return stmt.expression.accept(this) + ";";
  }

  @Override
  public String visit(Stmt.Print stmt) {
    return "print " + stmt.expression.accept(this) + ";";
  }

  @Override
  public String visit(Stmt.Var stmt) {
    return "var " + stmt.name.lexeme + " = " + stmt.expression.accept(this) + ";";
  }

  @Override
  public String visit(Expr.Binary expr) {
    return parenthesize(expr.operator.lexeme, expr.left, expr.right);
  }

  @Override
  public String visit(Expr.Grouping expr) {
    return parenthesize("group", expr.expression);
  }

  @Override
  public String visit(Expr.Literal expr) {
    if (expr.value == null) return "nil";
    return expr.value.toString();
  }

  @Override
  public String visit(Expr.Unary expr) {
    return parenthesize(expr.operator.lexeme, expr.right);
  }

  @Override
  public String visit(Expr.Variable expr) {
    return parenthesize(expr.name.lexeme);
  }

  private String parenthesize(String lexeme, Expr... expressions) {
    StringBuilder sb = new StringBuilder();
    sb.append("(");
    sb.append(lexeme);

    for (Expr expr : expressions) {
      sb.append(" ");
      sb.append(expr.accept(this));
    }

    sb.append(")");

    return sb.toString();
  }
}
