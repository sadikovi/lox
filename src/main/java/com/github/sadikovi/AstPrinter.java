package com.github.sadikovi;

import java.util.List;

/**
 * AST printer.
 */
class AstPrinter implements Expr.Visitor<String>, Stmt.Visitor<String> {
  public String print(List<Stmt> statements) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < statements.size(); i++) {
      sb.append("[" + (i + 1) + "]: \n");
      sb.append(statements.get(i).accept(this));
      sb.append("\n");
    }
    return sb.toString();
  }

  @Override
  public String visit(Stmt.Block stmt) {
    StringBuilder sb = new StringBuilder();
    sb.append("{\n");
    for (int i = 0; i < stmt.statements.size(); i++) {
      String[] enclosed = stmt.statements.get(i).accept(this).split("\n");
      for (String line : enclosed) {
        sb.append(" " + line);
        sb.append("\n");
      }
    }
    sb.append("}");
    return sb.toString();
  }

  @Override
  public String visit(Stmt.Expression stmt) {
    return stmt.expression.accept(this);
  }

  @Override
  public String visit(Stmt.Function stmt) {
    StringBuilder sb = new StringBuilder();
    sb.append("fun " + stmt.name.lexeme);
    sb.append("(");
    for (int i = 0; i < stmt.params.size(); i++) {
      sb.append(stmt.params.get(i).lexeme);
      if (i < stmt.params.size() - 1) {
        sb.append(", ");
      }
    }
    sb.append(")");
    return sb.toString();
  }

  @Override
  public String visit(Stmt.If stmt) {
    StringBuilder sb = new StringBuilder();
    sb.append("if (");
    sb.append(stmt.condition.accept(this));
    sb.append(") ");
    sb.append(stmt.thenBranch.accept(this));
    if (stmt.elseBranch != null) {
      sb.append(" else ");
      sb.append(stmt.elseBranch.accept(this));
    }
    return sb.toString();
  }

  @Override
  public String visit(Stmt.Print stmt) {
    return "print " + stmt.expression.accept(this) + ";";
  }

  @Override
  public String visit(Stmt.Return stmt) {
    return "return " + stmt.value.accept(this) + ";";
  }

  @Override
  public String visit(Stmt.While stmt) {
    StringBuilder sb = new StringBuilder();
    sb.append("while ");
    sb.append(stmt.condition.accept(this));
    sb.append(" ");
    sb.append(stmt.body.accept(this));
    return sb.toString();
  }

  @Override
  public String visit(Stmt.Break stmt) {
    return "break;";
  }

  @Override
  public String visit(Stmt.Var stmt) {
    StringBuilder sb = new StringBuilder();
    sb.append("var " + stmt.name.lexeme);
    if (stmt.expression != null) {
      sb.append(" = " + stmt.expression.accept(this));
    }
    sb.append(";");
    return sb.toString();
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
  public String visit(Expr.Logical expr) {
    return "logical";
  }

  @Override
  public String visit(Expr.Unary expr) {
    return parenthesize(expr.operator.lexeme, expr.right);
  }

  @Override
  public String visit(Expr.Variable expr) {
    return parenthesize(expr.name.lexeme);
  }

  @Override
  public String visit(Expr.Assign expr) {
    return expr.name.lexeme + " = " + expr.expression.accept(this);
  }

  @Override
  public String visit(Expr.Call expr) {
    StringBuilder sb = new StringBuilder();
    sb.append("<function call> " + expr.callee.accept(this));
    sb.append("(");
    for (int i = 0; i < expr.arguments.size(); i++) {
      Expr argument = expr.arguments.get(i);
      sb.append(argument.accept(this));
      if (i < expr.arguments.size() - 1) {
        sb.append(", ");
      }
    }
    sb.append(")");
    return sb.toString();
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
