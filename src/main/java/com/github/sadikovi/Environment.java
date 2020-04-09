package com.github.sadikovi;

import java.util.HashMap;
import java.util.Map;

/**
 * Interpreter environment.
 */
class Environment {
  // Sentinel value to mark variables as uninitialised
  private static final Object NO_INIT = new Object();

  final Environment enclosing; // parent environment
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
   * Defines new variable without initialising:
   * var a;
   */
  public void define(String name) {
    values.put(name, NO_INIT);
  }

  /**
   * Returns value for a defined variable or throws a runtime error.
   */
  public Object get(Token name) {
    if (values.containsKey(name.lexeme)) {
      Object value = values.get(name.lexeme);
      if (value != NO_INIT) return value;
      throw new RuntimeError(name, "Variable '" + name.lexeme + "' is not initialised");
    }
    if (enclosing != null) {
      return enclosing.get(name); // recursive solution is slow, this will be optimised in clox!
    }
    throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'");
  }

  /**
   * Returns value for a defined variable at distance or throws a runtime error.
   */
  public Object getAt(String name, int distance) {
    return ancestor(distance).values.get(name);
  }

  public Object getAt(Token name, int distance) {
    return getAt(name.lexeme, distance);
  }

  private Environment ancestor(int distance) {
    Environment env = this;
    for (int i = 0; i < distance; i++) {
      env = env.enclosing;
    }
    return env;
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

  /**
   * Assigns value to an existing variable at distance.
   */
  public void assignAt(Token name, Object value, int distance) {
    ancestor(distance).values.put(name.lexeme, value);
  }
}
