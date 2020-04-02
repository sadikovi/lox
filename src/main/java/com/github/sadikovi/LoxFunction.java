package com.github.sadikovi;

import java.util.List;

class LoxFunction implements LoxCallable {
  final Stmt.Function declaration;
  final Environment closure;

  LoxFunction(Stmt.Function declaration, Environment closure) {
    this.declaration = declaration;
    this.closure = closure;
  }

  @Override
  public int arity() {
    return declaration.params.size();
  }

  @Override
  public Object call(Interpreter interpreter, List<Object> arguments) {
    Environment env = new Environment(closure);
    for (int i = 0; i < arity(); i++) {
      String param = declaration.params.get(i).lexeme;
      Object value = arguments.get(i);
      env.define(param, value);
    }

    try {
      interpreter.executeBlock(declaration.body, env);
    } catch (Interpreter.Return returnValue) {
      return returnValue.value;
    }
    return null;
  }

  @Override
  public String toString() {
    return "<fn " + declaration.name.lexeme + ">";
  }
}
