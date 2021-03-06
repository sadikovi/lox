package com.github.sadikovi;

import java.util.List;

/**
 * Represents function in Lox.
 * If name is null, then the function is a lambda function.
 */
class LoxFunction implements LoxCallable {
  final Token name;
  final List<Token> params;
  final List<Stmt> body;
  final Environment closure;
  final boolean isInitializer;

  LoxFunction(
      Token name,
      List<Token> params,
      List<Stmt> body,
      Environment closure,
      boolean isInitializer) {
    this.name = name; // can be null for anonymous functions
    this.params = params; // can be null for getters
    this.body = body;
    this.closure = closure;
    this.isInitializer = isInitializer;
  }

  /** Returns true if this function is a lambda function */
  public boolean isLambda() {
    return name == null;
  }

  /** Returns true if this function is a getter function */
  public boolean isGetter() {
    return params == null;
  }

  public LoxFunction bind(LoxInstance instance) {
    Environment env = new Environment(closure);
    env.define("this", instance);
    return new LoxFunction(name, params, body, env, isInitializer);
  }

  @Override
  public int arity() {
    return (params == null) ? 0 : params.size();
  }

  @Override
  public Object call(Interpreter interpreter, List<Object> arguments) {
    Environment env = new Environment(closure);
    if (params != null) {
      for (int i = 0; i < params.size(); i++) {
        String param = params.get(i).lexeme;
        Object value = arguments.get(i);
        env.define(param, value);
      }
    }

    try {
      interpreter.executeBlock(body, env);
    } catch (Interpreter.Return returnValue) {
      if (isInitializer) return closure.getAt("this", 0);
      return returnValue.value;
    }

    if (isInitializer) return closure.getAt("this", 0);

    return null;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    if (isLambda()) {
      sb.append("<anonymous fn");
    } else {
      sb.append("<fn " + name.lexeme);
    }

    if (isGetter()) {
      sb.append(" # getter>");
    } else {
      sb.append(" (");
      for (int i = 0; i < params.size(); i++) {
        sb.append(params.get(i).lexeme);
        if (i < params.size() - 1) {
          sb.append(", ");
        }
      }
      sb.append(")>");
    }

    return sb.toString();
  }
}
