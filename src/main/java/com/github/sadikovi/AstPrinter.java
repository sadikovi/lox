package com.github.sadikovi;

/**
 * AST printer.
 */
class AstPrinter implements Expr.Visitor<String> {
  public String print(Expr expression) {
    return expression.accept(this);
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
