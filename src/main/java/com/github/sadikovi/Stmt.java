package com.github.sadikovi;

/**
 * Statement.
 */
abstract class Stmt {
  static class Expression extends Stmt {
    final Expr expression;

    Expression(Expr expression) {
      this.expression = expression;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visit(this);
    }
  }

  static class Print extends Stmt {
    final Expr expression;

    Print(Expr expression) {
      this.expression = expression;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visit(this);
    }
  }

  static class Var extends Stmt {
    final Token name;
    final Expr expression;

    Var(Token name, Expr expression) {
      this.name = name;
      this.expression = expression;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visit(this);
    }
  }

  public abstract <R> R accept(Visitor<R> visitor);

  /** Visitor to traverse statements */
  interface Visitor<R> {
    R visit(Expression stmt);
    R visit(Print stmt);
    R visit(Var stmt);
  }
}
