package com.github.sadikovi;

/**
 * Expressions for the parser.
 */
abstract class Expr {
  static class Binary extends Expr {
    private final Expr left;
    private final Token operator;
    private final Expr right;

    Binary(Expr left, Token operator, Expr right) {
      this.left = left;
      this.operator = operator;
      this.right = right;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visit(this);
    }
  }

  static class Grouping extends Expr {
    private final Expr expression;

    Grouping(Expr expression) {
      this.expression = expression;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visit(this);
    }
  }

  static class Literal extends Expr {
    private final Object value;

    Literal(Object value) {
      this.value = value;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visit(this);
    }
  }

  static class Unary extends Expr {
    private final Token operator;
    private final Expr right;

    Unary(Token operator, Expr right) {
      this.operator = operator;
      this.right = right;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visit(this);
    }
  }

  public abstract <R> R accept(Visitor<R> visitor);

  /**
   * Visitor to traverse the nodes of Expr.
   */
  interface Visitor<R> {
    R visit(Binary expr);
    R visit(Grouping expr);
    R visit(Literal expr);
    R visit(Unary expr);
  }
}
