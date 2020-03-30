package com.github.sadikovi;

import java.util.List;

/**
 * Statement.
 */
abstract class Stmt {
  static class Block extends Stmt {
    final List<Stmt> statements;

    Block(List<Stmt> statements) {
      this.statements = statements;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visit(this);
    }
  }

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

  static class If extends Stmt {
    final Expr condition;
    final Stmt thenBranch;
    final Stmt elseBranch;

    If(Expr condition, Stmt thenBranch, Stmt elseBranch) {
      this.condition = condition;
      this.thenBranch = thenBranch;
      this.elseBranch = elseBranch;
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

  static class While extends Stmt {
    final Expr condition;
    final Stmt body;

    While(Expr condition, Stmt body) {
      this.condition = condition;
      this.body = body;
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
    R visit(Block stmt);
    R visit(Expression stmt);
    R visit(If stmt);
    R visit(Print stmt);
    R visit(While stmt);
    R visit(Var stmt);
  }
}
