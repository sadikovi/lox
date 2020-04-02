package com.github.sadikovi;

import java.util.List;

/**
 * Expressions for the parser.
 */
abstract class Expr {
  static class Binary extends Expr {
    final Expr left;
    final Token operator;
    final Expr right;

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
    final Expr expression;

    Grouping(Expr expression) {
      this.expression = expression;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visit(this);
    }
  }

  static class Literal extends Expr {
    final Object value;

    Literal(Object value) {
      this.value = value;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visit(this);
    }
  }

  static class Logical extends Expr {
    final Expr left;
    final Token operator;
    final Expr right;

    Logical(Expr left, Token operator, Expr right) {
      this.left = left;
      this.operator = operator;
      this.right = right;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visit(this);
    }
  }

  static class Unary extends Expr {
    final Token operator;
    final Expr right;

    Unary(Token operator, Expr right) {
      this.operator = operator;
      this.right = right;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visit(this);
    }
  }

  static class Variable extends Expr {
    final Token name;

    Variable(Token name) {
      this.name = name;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visit(this);
    }
  }

  static class Assign extends Expr {
    final Token name;
    final Expr expression;

    Assign(Token name, Expr expression) {
      this.name = name;
      this.expression = expression;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visit(this);
    }
  }

  static class Call extends Expr {
    final Expr callee;
    final Token paren;
    final List<Expr> arguments;

    Call(Expr callee, Token paren, List<Expr> arguments) {
      this.callee = callee;
      this.paren = paren;
      this.arguments = arguments;
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
    R visit(Logical expr);
    R visit(Unary expr);
    R visit(Variable expr);
    R visit(Assign expr);
    R visit(Call expr);
  }
}
