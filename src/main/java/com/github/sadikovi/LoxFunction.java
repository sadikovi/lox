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

  LoxFunction(Token name, List<Token> params, List<Stmt> body, Environment closure) {
    this.name = name; // can be null for anonymous functions
    this.params = params;
    this.body = body;
    this.closure = closure;
  }

  /** Returns true if this function is a lambda function */
  public boolean isLambda() {
    return name == null;
  }

  public LoxFunction bind(LoxInstance instance) {
    Environment env = new Environment(closure);
    env.define("this", instance);
    return new LoxFunction(name, params, body, env);
  }

  @Override
  public int arity() {
    return params.size();
  }

  @Override
  public Object call(Interpreter interpreter, List<Object> arguments) {
    Environment env = new Environment(closure);
    for (int i = 0; i < params.size(); i++) {
      String param = params.get(i).lexeme;
      Object value = arguments.get(i);
      env.define(param, value);
    }

    try {
      interpreter.executeBlock(body, env);
    } catch (Interpreter.Return returnValue) {
      return returnValue.value;
    }
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
    sb.append(" (");
    for (int i = 0; i < params.size(); i++) {
      sb.append(params.get(i).lexeme);
      if (i < params.size() - 1) {
        sb.append(", ");
      }
    }
    sb.append(")>");
    return sb.toString();
  }
}
