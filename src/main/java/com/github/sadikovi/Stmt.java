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

  static class Break extends Stmt {
    Break() { }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visit(this);
    }
  }

  static class Class extends Stmt {
    final Token name;
    final Expr.Variable superclass;
    final List<Function> methods;
    final List<Function> classMethods;

    Class(
        Token name,
        Expr.Variable superclass,
        List<Function> methods,
        List<Function> classMethods) {
      this.name = name;
      this.superclass = superclass;
      this.methods = methods;
      this.classMethods = classMethods;
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

  static class Function extends Stmt {
    final Token name;
    final List<Token> params;
    final List<Stmt> body;

    Function(Token name, List<Token> params, List<Stmt> body) {
      this.name = name;
      this.params = params;
      this.body = body;
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

  static class Return extends Stmt {
    final Token keyword;
    final Expr value;

    Return(Token keyword, Expr value) {
      this.keyword = keyword;
      this.value = value;
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
    R visit(Break stmt);
    R visit(Class stmt);
    R visit(Expression stmt);
    R visit(Function stmt);
    R visit(If stmt);
    R visit(Print stmt);
    R visit(Return stmt);
    R visit(While stmt);
    R visit(Var stmt);
  }
}
