package com.github.sadikovi;

import com.github.sadikovi.TokenType.*;

/**
 * Evaluates expressions.
 */
class Interpreter implements Expr.Visitor<Object> {
  public void interpret(Expr expr) {
    try {
      Object value = expr.accept(this);
      System.out.println(stringify(value));
    } catch (RuntimeError error) {
      Lox.runtimeError(error);
    }
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
  public Object visit(Expr.Grouping expr) {
    return eval(expr.expression);
  }

  @Override
  public Object visit(Expr.Literal expr) {
    return expr.value;
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
}