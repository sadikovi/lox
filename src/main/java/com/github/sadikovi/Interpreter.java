package com.github.sadikovi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.sadikovi.TokenType.*;

/**
 * Evaluates expressions.
 */
class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
  // Represents the current environment (global or for a current block)
  final Environment globals = new Environment();
  private Environment env = globals;
  private final Map<Expr, Integer> locals = new HashMap<Expr, Integer>();

  Interpreter() {
    globals.define("clock", new LoxCallable() {
      @Override
      public int arity() {
        return 0;
      }

      @Override
      public Object call(Interpreter interpreter, List<Object> arguments) {
        return (double) System.currentTimeMillis() / 1000.0;
      }

      @Override
      public String toString() {
        return "<native fn>";
      }
    });
  }

  public void interpret(List<Stmt> statements, boolean printExpressions) {
    try {
      for (Stmt statement : statements) {
        if (printExpressions && statement instanceof Stmt.Expression) {
          // this is mostly for REPL, so it can print expressions without requiring "print".
          new Stmt.Print(((Stmt.Expression) statement).expression).accept(this);
        } else {
          statement.accept(this);
        }
      }
    } catch (RuntimeError error) {
      Lox.runtimeError(error);
    }
  }

  @Override
  public Void visit(Stmt.Block stmt) {
    executeBlock(stmt.statements, new Environment(env));
    return null;
  }

  @Override
  public Void visit(Stmt.Break stmt) {
    throw new Break();
  }

  @Override
  public Void visit(Stmt.Class stmt) {
    env.define(stmt.name.lexeme, null);
    LoxClass klass = new LoxClass(stmt.name.lexeme);
    env.assign(stmt.name, klass);
    return null;
  }

  @Override
  public Void visit(Stmt.Expression stmt) {
    eval(stmt.expression);
    return null;
  }

  @Override
  public Void visit(Stmt.Function stmt) {
    LoxFunction function = new LoxFunction(stmt.name, stmt.params, stmt.body, env);
    env.define(stmt.name.lexeme, function);
    return null;
  }

  @Override
  public Void visit(Stmt.If stmt) {
    if (isTruthy(eval(stmt.condition))) {
      stmt.thenBranch.accept(this);
    } else if (stmt.elseBranch != null) {
      stmt.elseBranch.accept(this);
    }
    return null;
  }

  @Override
  public Void visit(Stmt.Print stmt) {
    Object value = eval(stmt.expression);
    System.out.println(stringify(value));
    return null;
  }

  @Override
  public Void visit(Stmt.Return stmt) {
    Object value = null;
    if (stmt.value != null) {
      value = eval(stmt.value);
    }
    throw new Return(value);
  }

  @Override
  public Void visit(Stmt.While stmt) {
    while (isTruthy(eval(stmt.condition))) {
      try {
        stmt.body.accept(this);
      } catch (Break err) {
        // stop while loop
        break;
      }
    }
    return null;
  }

  @Override
  public Void visit(Stmt.Var stmt) {
    if (stmt.expression != null) {
      env.define(stmt.name.lexeme, eval(stmt.expression));
    } else {
      env.define(stmt.name.lexeme);
    }
    return null;
  }

  @Override
  public Object visit(Expr.Assign expr) {
    Object value = eval(expr.expression);

    Integer distance = locals.get(expr);
    if (distance != null) {
      env.assignAt(expr.name, value, distance);
    } else {
      globals.assign(expr.name, value);
    }
    return value;
  }

  @Override
  public Object visit(Expr.Binary expr) {
    Object left = eval(expr.left);
    Object right = eval(expr.right);
    Token token = expr.operator;

    switch (token.type) {
      case GREATER:
        return getNumber(token, left) > getNumber(token, right);
      case GREATER_EQUAL:
        return getNumber(token, left) >= getNumber(token, right);
      case LESS:
        return getNumber(token, left) < getNumber(token, right);
      case LESS_EQUAL:
        return getNumber(token, left) <= getNumber(token, right);
      case EQUAL_EQUAL:
        return isEqual(left, right);
      case BANG_EQUAL:
        return !isEqual(left, right);
      case PLUS:
        if (isNumber(left) && isNumber(right)) {
          return getNumber(token, left) + getNumber(token, right);
        }
        // If one of the operands is a string concatenate, also handles nil
        if (isString(left)) {
          return getString(token, left) + stringify(right);
        } else if (isString(right)) {
          return stringify(left) + getString(token, right);
        }
        throw new RuntimeError(token, "Both operands must be numbers or strings");
      case MINUS:
        return getNumber(token, left) - getNumber(token, right);
      case STAR:
        // If the left operand is a string and the right one is an integer,
        // we concatenate strings as many times as right operand.
        // We don't support null values
        if (isString(left) && isNumber(right)) {
          if (hasFraction(right)) {
            throw new RuntimeError(token, "Can't multiply by a floating-point number");
          }
          StringBuilder sb = new StringBuilder();
          for (int i = 0; i < (int) getNumber(token, right); i++) {
            sb.append(getString(token, left));
          }
          return sb.toString();
        }
        return getNumber(token, left) * getNumber(token, right);
      case SLASH:
        double lval = getNumber(token, left);
        double rval = getNumber(token, right);
        if (rval == 0) throw new RuntimeError(token, "Division by zero");
        return  lval / rval;
      default:
        throw new RuntimeError(token, "Unreachable");
    }
  }

  @Override
  public Object visit(Expr.Call expr) {
    Object callee = eval(expr.callee);

    List<Object> arguments = new ArrayList<Object>();
    for (Expr argument : expr.arguments) {
      arguments.add(eval(argument));
    }

    if (!(callee instanceof LoxCallable)) {
      throw new RuntimeError(expr.paren, "Can only call functions and classes");
    }

    LoxCallable function = (LoxCallable) callee;

    if (function.arity() != arguments.size()) {
      throw new RuntimeError(expr.paren,
        "Expected " + function.arity() + " arguments, got " + arguments.size());
    }

    return function.call(this, arguments);
  }

  @Override
  public Object visit(Expr.Get expr) {
    Object object = eval(expr.object);
    if (object instanceof LoxInstance) {
      return ((LoxInstance) object).get(expr.name);
    }
    throw new RuntimeError(expr.name, "Only instances have properties");
  }

  @Override
  public Object visit(Expr.Grouping expr) {
    return eval(expr.expression);
  }

  @Override
  public Object visit(Expr.Lambda expr) {
    return new LoxFunction(null, expr.params, expr.body, env);
  }

  @Override
  public Object visit(Expr.Literal expr) {
    return expr.value;
  }

  @Override
  public Object visit(Expr.Logical expr) {
    Object left = eval(expr.left);
    if (expr.operator.type == TokenType.OR) {
      if (isTruthy(left)) return left;
    } else {
      if (!isTruthy(left)) return left;
    }
    return eval(expr.right);
  }

  @Override
  public Void visit(Expr.Set expr) {
    Object object = eval(expr.object);

    if (!(object instanceof LoxInstance)) {
      throw new RuntimeError(expr.name, "Only instances have fields");
    }

    Object value = eval(expr.value);

    ((LoxInstance) object).set(expr.name, value);

    return null;
  }

  @Override
  public Object visit(Expr.Unary expr) {
    Object result = eval(expr.right);
    Token token = expr.operator;

    switch (token.type) {
      case BANG:
        return !isTruthy(result);
      case MINUS:
        return -getNumber(token, result);
      case PLUS:
        return getNumber(token, result);
      default:
        throw new RuntimeError(token, "Unreachable");
    }
  }

  @Override
  public Object visit(Expr.Variable expr) {
    return lookupVariable(expr.name, expr);
  }

  /** Executes list of statements in provided environment */
  void executeBlock(List<Stmt> statements, Environment environment) {
    Environment parent = env;
    env = environment;
    try {
      for (Stmt statement : statements) {
        statement.accept(this);
      }
    } finally {
      env = parent;
    }
  }

  void resolve(Expr expr, int depth) {
    locals.put(expr, depth);
  }

  private Object lookupVariable(Token name, Expr expr) {
    Integer distance = locals.get(expr);
    if (distance != null) {
      return env.getAt(name, distance);
    } else {
      return globals.get(name);
    }
  }

  /** Evaluates expression */
  private Object eval(Expr expr) {
    return expr.accept(this);
  }

  /** Converts object into string */
  private String stringify(Object value) {
    if (value == null) return "nil";
    String val = value.toString();
    if (isNumber(value) && val.endsWith(".0")) {
      return val.substring(0, val.length() - 2);
    }
    return val;
  }

  /** Follows Ruby's rule regarding true/false */
  private boolean isTruthy(Object result) {
    if (result == null) return false;
    if (result instanceof Boolean) return (boolean) result;
    return true;
  }

  /** Returns true if objects are equal */
  private boolean isEqual(Object left, Object right) {
    if (left == null && right == null) return true;
    if (left == null) return false;
    return left.equals(right);
  }

  /** Returns true if the object is a number */
  private boolean isNumber(Object obj) {
    return obj instanceof Double;
  }

  /** Returns true if the object is a number and has fractional part */
  private boolean hasFraction(Object obj) {
    if (isNumber(obj)) return ((Double) obj).intValue() != (Double) obj;
    return false;
  }

  /** Returns true if the object is a string */
  private boolean isString(Object obj) {
    return obj instanceof String;
  }

  /** Casts value to number or throws an exception */
  private double getNumber(Token token, Object obj) {
    if (isNumber(obj)) return (double) obj;
    throw new RuntimeError(token, "Operand must be a number");
  }

  /** Casts value to string or throws an exception */
  private String getString(Token token, Object obj) {
    if (isString(obj)) return (String) obj;
    throw new RuntimeError(token, "Operand must be a string");
  }

  /** Exception to indicate break in the loop */
  static class Break extends RuntimeException {
    Break() {
      super(null, null, false, false);
    }
  }

  /** Exception to indicate return in the function */
  static class Return extends RuntimeException {
    final Object value;

    Return(Object value) {
      super(null, null, false, false);
      this.value = value;
    }
  }
}
