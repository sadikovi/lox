package com.github.sadikovi;

import java.util.HashMap;
import java.util.Map;

/**
 * Interpreter environment.
 */
class Environment {
  private final Environment enclosing; // parent environment
  private final Map<String, Object> values = new HashMap<String, Object>();

  Environment() {
    this.enclosing = null;
  }

  Environment(Environment enclosing) {
    this.enclosing = enclosing;
  }

  /**
   * Defines new variable or redefines an existing one with a new value.
   */
  public void define(String name, Object value) {
    values.put(name, value);
  }

  /**
   * Returns value for a defined variable or throws a runtime error.
   */
  public Object get(Token name) {
    if (values.containsKey(name.lexeme)) {
      return values.get(name.lexeme);
    }
    if (enclosing != null) {
      return enclosing.get(name); // recursive solution is slow, this will be optimised in clox!
    }
    throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'");
  }

  /**
   * Assigns value to an existing variable.
   */
  public void assign(Token name, Object value) {
    if (values.containsKey(name.lexeme)) {
      values.put(name.lexeme, value);
      return;
    }
    if (enclosing != null) {
      enclosing.assign(name, value);
      return;
    }
    throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'");
  }
}
